package com.github.neppord.rewrite.parser;


import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.*;

public interface Parser<V> {
    Parser<CharSequence> whitespace = regexp("\\s+");
    Parser<CharSequence> leftParenthesis = regexp("\\(");
    Parser<CharSequence> rightParenthesis = regexp("\\)");
    Parser<CharSequence> leftSquigglyParenthesis = literal("{");
    Parser<CharSequence> rightSquigglyParenthesis = literal("}");
    Parser<CharSequence> leftBracket = regexp("\\[");
    Parser<CharSequence> rightBracket = regexp("\\]");
    Parser<CharSequence> doubleQuote = literal("\"");

    Parser<CharSequence> anything = regexp(".");

    Parser<CharSequence> stringLiteral=
        doubleQuote.map(CharSequence::toString).apply(
            some(
                s1 -> s2 ->  s1 + s2,
                literal("\\\"")
                    .or(regexp("[^\"]"))
                    .map(CharSequence::toString),
                ""
            ).apply(doubleQuote.map(s1 -> s2 -> s3 ->s1 + s2 + s3))
        );

    Parser<CharSequence> variable = regexp("\\$\\{\\{[A-Za-z_]+}}")
        .map(v -> v.subSequence(3, v.length() - 2));

    Parser<CharSequence> variableContent =
        stringLiteral.or(regexp("\\w+"));

    Parser<Parser<Map<String,String>>> readVariable =
        variable.map(
            key -> variableContent.map(
                value -> singletonMap(key.toString(), value.toString())
            )
        );

    Parser<Parser<Map<String,String>>> readLiteral =
        anything.map(s -> literal(s).map(s2 -> emptyMap()));
    Parser<Parser<Map<String,String>>> readWhitespace =
        whitespace.map(s -> whitespace.map(w -> emptyMap()));

    Parser<Parser<Map<String, String>>> readTemplate =
        many(
            p1 -> p2 -> p2.apply(p1.map(Parser::mergeMaps)),
            readWhitespace.or(readVariable).or(readLiteral)
        );

    static Function<Map<String, String>, Map<String, String>> mergeMaps(Map<String, String> m1) {
        return m2 -> {
            HashMap<String, String> ret = new HashMap<>();
            ret.putAll(m1);
            ret.putAll(m2);
            return ret;
        };
    }

    static <U> Parser<U> some(Function<U, Function<U, U>> f, Parser<U> parser, U inNone) {
        return many(f, parser).or(value(inNone));
    }

    static <U> Parser<U> many(Function<U, Function<U, U>> f, Parser<U> parser) {
        return c -> {
             try {
                 return many(f, parser).apply(parser.map(f)).parse(c);
             } catch (ParseException e) {
                 return parser.parse(c);
             }
        };
    }

    static Parser<CharSequence> literal(CharSequence word) {
        return c -> {
            if (word.length() <= c.length() && c.subSequence(0, word.length()).equals(word)) {
                return new Result<>(word, c.subSequence(word.length(), c.length()));
            } else {
                int lookahead = Math.min(c.length(), word.length());
                CharSequence found = c.subSequence(0, lookahead);
                final String message = "Expected '" + word +"' found '" + found + "'";
                throw new ParseException(message);
            }
        };
    }

    static <U >Parser<U> value(U value) {
        return c -> new Result<>(value, c);
    }

    static Parser<CharSequence> rewrite(CharSequence readTemplate, CharSequence writeTemplate) {
        return c -> new Result<>(writeTemplate, "");
    }

    Parser<Function<Map<String, String>, String>> writeTemplate = c -> new Result<>(m -> {
        try {
            return makeWriteTemplate(m).parse(c).value;
        } catch (ParseException e) {
            throw new RuntimeException(e.message);
        }
    }, "");

    static Parser<String> makeWriteTemplate(Map<String, String> variables) {
        final Parser<CharSequence> templateVariable =
            variable.map(CharSequence::toString);
        final Parser<String> anything = Parser.anything.map(CharSequence::toString);
        final Parser<String> variableOrAnything = templateVariable.map(variables::get).or(anything);
        return c -> {
            try {
                final Parser<String> concat =
                    makeWriteTemplate(variables)
                        .apply(variableOrAnything
                            .apply(value(s -> s2 -> s + s2)));
                return concat.parse(c);
            } catch (ParseException e) {
                return variableOrAnything.parse(c);
            }
        };
    }

    Result<V> parse(CharSequence c) throws ParseException;
    static Parser<CharSequence> regexp(String re) {
        Pattern p = Pattern.compile("^" + re);
        return c -> {
            Matcher matcher = p.matcher(c);
            if (!matcher.find()) {
                int lookahead = Math.min(c.length(), 10);
                CharSequence found = c.subSequence(0, lookahead);
                final String message = "Expected '" + re +"' found '" + found + "'";
                throw new ParseException(message);
            }
            MatchResult result = matcher.toMatchResult();
            final int start = result.start();
            final int end = result.end();
            CharSequence value = c.subSequence(start, end);
            CharSequence rest = c.subSequence(end, c.length());
            return new Result<>(value, rest);
        };
    }

    default <U> Parser<U> map(Function<V,U> f) {
        return c -> this.parse(c).map(f);
    }

    default Parser<V> or(Parser<V> alternative) {
        return c -> {
            try {
                return parse(c);
            } catch (ParseException e) {
                return alternative.parse(c);
            }
        };
    }

    default <U> Parser<U> apply(Parser<Function<V, U>> other) {
        return c -> {
            Result<Function<V, U>> result = other.parse(c);
            return map(result.value).parse(result.rest);
        };
    }
}
