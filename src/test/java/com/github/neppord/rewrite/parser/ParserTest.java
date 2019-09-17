package com.github.neppord.rewrite.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    @Test
    public void whitespace() throws ParseException {
        assertEquals(" ", Parser.whitespace.parse(" ").value);
        assertThrows(
            ParseException.class,
            () -> Parser.whitespace.parse("v ")
        );
    }
}