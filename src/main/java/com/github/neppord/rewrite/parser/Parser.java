package com.github.neppord.rewrite.parser;


import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Parser<V> {
    Parser<CharSequence> whitespace = regexp("\\s+");

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
            return new Result<CharSequence>(value, rest);
        };
    }
}
