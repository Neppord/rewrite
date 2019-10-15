package com.github.neppord.rewrite.parser;

import java.util.Map;
import java.util.function.Function;

import static com.github.neppord.rewrite.parser.Functional.fix;
import static com.github.neppord.rewrite.parser.Parser.many;
import static com.github.neppord.rewrite.parser.Parser.some;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

public interface Rewrite {
    Parser<CharSequence> variableContent = some(Functional::concat2,fix(
         vc ->
             Literals.rightSquigglyParenthesis.apply(
                 Parser.laizy(vc).or(Literals.nothing).apply(
                    Literals.leftSquigglyParenthesis.map(Functional::concat3)
                 )
             ).or(
                 Literals.rightBracket.apply(
                     Parser.laizy(vc).or(Literals.nothing).apply(
                         Literals.leftBracket.map(Functional::concat3)
                     )
                 )
             ).or(
                 Literals.rightParenthesis.apply(
                     Parser.laizy(vc).or(Literals.nothing).apply(
                         Literals.leftParenthesis.map(Functional::concat3)
                     )
                 )
             )
                 .or(Literals.word)
                 .or(Literals.stringLiteral)
    ), "");

    Parser<Parser<Map<String,String>>> readVariable =
        Literals.variable.map(
            key -> variableContent.map(
                value -> singletonMap(key.toString(), value.toString())
            )
        );
    Parser<Parser<Map<String,String>>> readLiteral =
        Literals.anything.map(s -> Literals.literal(s).map(s2 -> emptyMap()));
    Parser<Parser<Map<String,String>>> readWhitespace =
        Literals.whitespace.map(s -> Literals.whitespace.map(w -> emptyMap()));
    Parser<Parser<Map<String, String>>> readTemplate =
        many(
            p1 -> p2 -> p2.apply(p1.map(Parser::mergeMaps)),
            readWhitespace.or(readVariable).or(readLiteral)
        ).or(
            Literals.nothing.map(
                empty1 -> Literals.nothing.map(empty2 -> emptyMap())
            )
        );
    Parser<Function<Map<String, String>, String>> writeTemplate = c -> new Result<>(m -> {
        try {
            return makeWriteTemplate(m).parse(c).value;
        } catch (ParseException e) {
            throw new RuntimeException(e.message);
        }
    }, "");

    static Parser<String> rewrite(CharSequence readTemplate, CharSequence writeTemplate) {
        final Parser<Map<String, String>> readParser;
        try {
            readParser = Rewrite.readTemplate.parse(readTemplate).value;
        } catch (ParseException e) {
            throw new RuntimeException(
                "Failed to parse read template:\n\t" + e.message,
                e
            );
        }
        return c -> {
            Map<String, String> variables = readParser.parse(c).value;
            return Rewrite.makeWriteTemplate(variables).parse(writeTemplate);
        };
    }

    static Parser<String> makeWriteTemplate(Map<String, String> variables) {
        final Parser<CharSequence> templateVariable =
            Literals.variable.map(CharSequence::toString);
        final Parser<String> anything = Literals.anything.map(CharSequence::toString);
        final Parser<String> variableOrAnything = templateVariable.map(variables::get).or(anything);
        return c -> {
            try {
                final Parser<String> concat =
                    makeWriteTemplate(variables)
                        .apply(variableOrAnything
                            .apply(Parser.value(s -> s2 -> s + s2)));
                return concat.parse(c);
            } catch (ParseException e) {
                try {
                    return variableOrAnything.parse(c);
                } catch (ParseException ex) {
                    return Literals.nothing.parse(c).map(CharSequence::toString);
                }
            }
        };
    }
}
