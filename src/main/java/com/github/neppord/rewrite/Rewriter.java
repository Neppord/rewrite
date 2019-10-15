package com.github.neppord.rewrite;

import com.github.neppord.rewrite.parser.ParseException;
import com.github.neppord.rewrite.parser.Parser;
import com.github.neppord.rewrite.parser.Rewrite;

public class Rewriter {
    private final Parser<String> parser;

    public Rewriter(CharSequence read, CharSequence write) {
        this.parser = Rewrite.rewrite(read, write);
    }

    public String rewrite(CharSequence input) throws ParseException {
        return parser.parse(input).value;
    }
}
