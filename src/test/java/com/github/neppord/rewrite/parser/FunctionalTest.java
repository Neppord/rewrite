package com.github.neppord.rewrite.parser;

import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static com.github.neppord.rewrite.parser.Functional.fix;
import static org.junit.jupiter.api.Assertions.*;

class FunctionalTest {
    @Test
    void testFix() {
        assertEquals("", fix(x -> ""));
        final Function<Integer, Integer> count =
            fix(f -> x ->
                x < 10 ? f.get().apply(x + 1) : x
            );
        assertEquals(10, count.apply(0));
    }
}