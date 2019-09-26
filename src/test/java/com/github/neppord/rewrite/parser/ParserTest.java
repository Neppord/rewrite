package com.github.neppord.rewrite.parser;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Function;

import static com.github.neppord.rewrite.parser.Parser.regexp;
import static com.github.neppord.rewrite.parser.Parser.variable;
import static java.util.Collections.EMPTY_MAP;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void variable() throws ParseException {
        assertEquals("${{seed}}", variable.parse("${{seed}}").value);
    }

    @Test
    void writeTemplate() throws ParseException {
        assertEquals("hello world", Parser.writeTemplate.parse("hello world").value.apply(EMPTY_MAP));
        assertEquals(
            "hello world",
            Parser.writeTemplate.parse("hello ${{subject}}").value.apply(singletonMap("subject", "world"))
        );
    }

    @Test
    void readTemplate() throws ParseException {
        final Parser<Map<String, String>> hello_world = Parser.readTemplate.parse("hello world").value;
        assertEquals(EMPTY_MAP, hello_world.parse("hello world").value);
    }

    @Test
    void rewrite() throws ParseException {
        String input = "{\"seed\": \"as23}sdkdf\"}";
        String expected = "{\"seed\": \"sable1234\" }";
        Parser<CharSequence> transformation = Parser.rewrite(
            "{\"seed\": ${{seed}} }",
            expected
            );
        assertEquals(expected, transformation.parse(input).value);
        // assertEquals(" " + expected, transformation.parse(" " + input).value);
    }
}