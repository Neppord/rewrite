package com.github.neppord.rewrite.parser;


import java.util.Map;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.EMPTY_MAP;

public interface Parser<V> {
    Parser<CharSequence> whitespace = regexp("\\s+");
    Parser<CharSequence> leftParenthesis = regexp("\\(");
    Parser<CharSequence> rightParenthesis = regexp("\\)");
    Parser<CharSequence> leftBracket = regexp("\\[");
    Parser<CharSequence> rightBracket = regexp("\\]");
    Parser<CharSequence> variable = regexp("\\$\\{\\{[A-Za-z_]+}}")
        .map(v -> v.subSequence(3, v.length() - 2));
    Parser<Parser<Map<String, String>>> readTemplate =
        c -> new Result<>(value(EMPTY_MAP), c);

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
        final Parser<String> anything = regexp(".").map(CharSequence::toString);
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
            return map(result.value).parse(result.c);
        };
    }
}
