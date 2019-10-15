package com.github.neppord.rewrite;

import com.github.neppord.rewrite.parser.ParseException;
import org.junit.jupiter.api.Test;

class RewriterTest {

    @Test
    void simpleRewrite() throws ParseException {
        new Rewriter("", "").rewrite("");
    }
}