package com.github.neppord.rewrite.parser;

import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BackTrackingParserTest {

    @Test
    void or() {
        final BackTrackingParser<String> empty = c -> Stream.empty();
        final BackTrackingParser<String> one =
            c -> Stream.of(new Result<>("Hello world", c));
        assertEquals(
            "Hello world",
            empty.or(one).parse("").findFirst().get().value
        );
        assertEquals(
            "Hello world",
            one.or(empty).parse("").findFirst().get().value
        );
    }

    @Test
    void map() {
        final BackTrackingParser<String> one =
            c -> Stream.of(new Result<>("1", c));
        final Result<Integer> result =
            one.map(Integer::parseInt).parse("").findFirst().get();
        assertEquals(1, result.value);
    }

    @Test
    void toParser() throws ParseException {
        final BackTrackingParser<Integer> one =
            c -> Stream.of(new Result<>(1, c));
        final Parser<Integer> parser = one.toParser();
        assertEquals(1, parser.parse("").value);
    }
}