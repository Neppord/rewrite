package com.github.neppord.rewrite.parser;

import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static com.github.neppord.rewrite.parser.Parser.regexp;
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

    @Test
    public void literal() throws ParseException {
        final Parser<CharSequence> wordParser = Parser.literal("word");
        assertEquals("word", wordParser.parse("word").value);
        assertEquals("", wordParser.parse("word").c);
        assertEquals("s", wordParser.parse("words").c);
        assertThrows(
            ParseException.class,
            () -> wordParser.parse("wor")
        );

        Parser<CharSequence> phraseParser = Parser.literal("phrase");
        assertEquals("phrase", phraseParser.parse("phrase").value);
    }

    @Test
    void or() throws ParseException {
        Parser<CharSequence> parser = Parser.whitespace.or(Parser.rightParenthesis);
        assertEquals(" ", parser.parse(" ").value);
        assertEquals(")", parser.parse(")").value);
    }

    @Test
    void value() throws ParseException {
        Parser<CharSequence> parser = Parser.value("word");
        assertEquals("word", parser.parse(" ").value);
    }

    @Test
    void apply() throws ParseException {
        Parser<Function<Integer, Integer>> addOne = Parser.value(x -> x +1);
        Parser<Integer> one = Parser.value(1);
        assertEquals(2, one.apply(addOne).parse("").value);

        Parser<Integer> number = regexp("\\d")
            .map(CharSequence::toString)
            .map(Integer::parseInt);
        Parser<CharSequence> operator = Parser.literal("-");
        Parser<Integer> subtraction =
            number.apply(
                operator.apply(
                    number.apply(
                        Parser.value(x -> minus -> y -> x - y)
                    )
                )
            );
        assertEquals(3 , subtraction.parse("4-1").value);
    }
}