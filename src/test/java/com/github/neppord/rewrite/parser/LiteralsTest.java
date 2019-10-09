package com.github.neppord.rewrite.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LiteralsTest {


    @Test
    public void whitespace() throws ParseException {
        assertEquals(" ", Literals.whitespace.parse(" ").value);
        assertThrows(
            ParseException.class,
            () -> Literals.whitespace.parse("v ")
        );
    }

    @Test
    public void parenthesis() throws ParseException {
        assertEquals("(", Literals.leftParenthesis.parse("(").value);
        assertEquals(")", Literals.rightParenthesis.parse(")").value);
        assertEquals("{", Literals.leftSquigglyParenthesis.parse("{").value);
        assertEquals("}", Literals.rightSquigglyParenthesis.parse("}").value);
        assertEquals("[", Literals.leftBracket.parse("[").value);
        assertEquals("]", Literals.rightBracket.parse("]").value);
    }

    @Test
    public void stringLiteral() throws ParseException {
        assertEquals("\"\"", Literals.stringLiteral.parse("\"\"").value);
        assertEquals("\"a\"", Literals.stringLiteral.parse("\"a\"").value);
        assertEquals("\"\\\"\"", Literals.stringLiteral.parse("\"\\\"\"").value);
    }

    @Test
    public void literal() throws ParseException {
        final Parser<CharSequence> wordParser = Literals.literal("word");
        assertEquals("word", wordParser.parse("word").value);
        assertEquals("", wordParser.parse("word").rest);
        assertEquals("s", wordParser.parse("words").rest);
        assertThrows(
            ParseException.class,
            () -> wordParser.parse("wor")
        );

        Parser<CharSequence> phraseParser = Literals.literal("phrase");
        assertEquals("phrase", phraseParser.parse("phrase").value);
    }

    @Test
    public void variable() throws ParseException {
        assertEquals("seed", Literals.variable.parse("${{seed}}").value);
    }
}