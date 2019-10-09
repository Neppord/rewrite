package com.github.neppord.rewrite.parser;

import java.util.Map;
import java.util.function.Function;

import static com.github.neppord.rewrite.parser.Functional.fix;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

public interface Rewrite {
    Parser<CharSequence> variableContent = fix(
         vc ->
             Literals.rightSquigglyParenthesis.apply(
                 Parser.laizy(vc).apply(
                    Literals.leftSquigglyParenthesis.map(Functional::concat3)
                 )
             ).or(
                 Literals.rightBracket.apply(
                     Parser.laizy(vc).apply(
                         Literals.leftBracket.map(Functional::concat3)
                     )
                 )
             ).or(
                 Literals.rightParenthesis.apply(
                     Parser.laizy(vc).apply(
                         Literals.leftParenthesis.map(Functional::concat3)
                     )
                 )
             )
                 .or(Literals.word)
                 .or(Literals.stringLiteral)
                 .or(Literals.nothing)
    );
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
        Parser.many(
            p1 -> p2 -> p2.apply(p1.map(Parser::mergeMaps)),
            readWhitespace.or(readVariable).or(readLiteral)
        );
    Parser<Function<Map<String, String>, String>> writeTemplate = c -> new Result<>(m -> {
        try {
            return makeWriteTemplate(m).parse(c).value;
        } catch (ParseException e) {
            throw new RuntimeException(e.message);
        }
    }, "");

    static Parser<CharSequence> rewrite(CharSequence readTemplate, CharSequence writeTemplate) {
        return c -> new Result<>(writeTemplate, "");
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
                return variableOrAnything.parse(c);
            }
        };
    }
}
