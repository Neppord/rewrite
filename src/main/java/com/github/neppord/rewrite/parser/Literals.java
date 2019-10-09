package com.github.neppord.rewrite.parser;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Literals {
    Parser<CharSequence> whitespace = regexp("\\s+");
    Parser<CharSequence> leftParenthesis = literal("(");
    Parser<CharSequence> rightParenthesis = literal(")");
    Parser<CharSequence> leftSquigglyParenthesis = literal("{");
    Parser<CharSequence> rightSquigglyParenthesis = literal("}");
    Parser<CharSequence> leftBracket = literal("[");
    Parser<CharSequence> rightBracket = literal("]");
    Parser<CharSequence> doubleQuote = literal("\"");
    Parser<CharSequence> stringLiteral=
        doubleQuote.map(CharSequence::toString).apply(
            Parser.some(
                s1 -> s2 ->  s1 + s2,
                literal("\\\"")
                    .or(regexp("[^\"]"))
                    .map(CharSequence::toString),
                ""
            ).apply(doubleQuote.map(s1 -> s2 -> s3 ->s1 + s2 + s3))
        );
    Parser<CharSequence> word = regexp("\\w+");
    Parser<CharSequence> anything = regexp(".");
    Parser<CharSequence> nothing = literal("");
    Parser<CharSequence> variable = regexp("\\$\\{\\{[A-Za-z_]+}}")
        .map(v -> v.subSequence(3, v.length() - 2));

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
}
