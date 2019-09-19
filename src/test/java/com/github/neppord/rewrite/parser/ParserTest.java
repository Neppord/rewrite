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
    @Test
    public void parenthesis() throws ParseException {
        assertEquals("(", Parser.leftParenthesis.parse("(").value);
        assertEquals(")", Parser.rightParenthesis.parse(")").value);
        assertEquals("[", Parser.leftBracket.parse("[").value);
        assertEquals("]", Parser.rightBracket.parse("]").value);
    }
}