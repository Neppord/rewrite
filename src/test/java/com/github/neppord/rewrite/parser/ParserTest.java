package com.github.neppord.rewrite.parser;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Function;

import static com.github.neppord.rewrite.parser.Rewrite.variableContent;
import static java.util.Collections.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ParserTest {

    @Test
    void many() throws ParseException {
        final Parser<Integer> number = Literals.regexp("\\d")
            .map(CharSequence::toString)
            .map(Integer::parseInt);
        final Parser<Integer> positiveNumber = Literals.regexp("\\+\\d")
            .map(CharSequence::toString)
            .map(Integer::parseInt);
        final Parser<Integer> rest = Parser.many((Integer x) -> (Integer y) -> x + y, positiveNumber);
        final Parser<Integer> expr = rest.apply(number.map(x -> y -> x + y));
        assertEquals(10, expr.parse("1+2+3+4").value);
    }

    @Test
    void or() throws ParseException {
        Parser<CharSequence> parser = Literals.whitespace.or(Literals.rightParenthesis);
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

        Parser<Integer> number = Literals.regexp("\\d")
            .map(CharSequence::toString)
            .map(Integer::parseInt);
        Parser<CharSequence> operator = Literals.literal("-");
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
    void surroundWith() throws ParseException {
        Function<CharSequence, Function<CharSequence, Function<CharSequence, CharSequence>>>  concat=
            s1 -> s2 -> s3 -> s1.toString() + s2 + s3;
        Parser<CharSequence> parser =
            Literals.literal("a").surroundWith(
                concat,
                Literals.leftSquigglyParenthesis,
                Literals.rightSquigglyParenthesis
            );
        assertEquals("{a}", parser.parse("{a}").value);
    }

}