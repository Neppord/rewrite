package com.github.neppord.rewrite.parser;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.github.neppord.rewrite.parser.Rewrite.rewrite;
import static com.github.neppord.rewrite.parser.Rewrite.variableContent;
import static java.util.Collections.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RewriteTest {

    @Test
    public void writeTemplate() throws ParseException {
        assertEquals("hello world", Rewrite.writeTemplate.parse("hello world").value.apply(emptyMap()));
        assertEquals(
            "hello world",
            Rewrite.writeTemplate.parse("hello ${{subject}}").value.apply(singletonMap("subject", "world"))
        );
    }

    @Test
    public void variableContent() throws ParseException {
        assertEquals("{}", variableContent.parse("{}").value);
        assertEquals("{}{}", variableContent.parse("{}{}").value);
        assertEquals("[]", variableContent.parse("[]").value);
        assertEquals("()", variableContent.parse("()").value);
        assertEquals("[\"hello world\"]", variableContent.parse("[\"hello world\"]").value);
    }

    @Test
    public void readTemplate() throws ParseException {
        final Parser<Map<String, String>> hello_world = Rewrite.readTemplate.parse("hello world").value;
        assertEquals(EMPTY_MAP, hello_world.parse("hello world").value);

        final Parser<Map<String, String>> hello = Rewrite.readTemplate.parse("hello ${{subject}}").value;
        assertEquals(singletonMap("subject", "world"), hello.parse("hello world").value);

        final Parser<Map<String, String>> name = Rewrite.readTemplate.parse("${{firstname}} ${{lastname}}").value;
        final Map<String, String> expected = Map.of(
            "firstname", "Samuel",
            "lastname", "Ytterbrink"
            );
        assertEquals(expected, name.parse("Samuel Ytterbrink").value);
        // handle whitespace
        assertEquals(expected, name.parse("Samuel  Ytterbrink").value);

        final Parser<Map<String, String>> jsonSeed =
            Rewrite.readTemplate.parse("{\"seed\": ${{seed}}}").value;

        String jsonWithString = "{\"seed\": \"as23sdkdf\"}";
        assertEquals(
            singletonMap("seed", "\"as23sdkdf\""),
            jsonSeed.parse(jsonWithString).value
        );
    }

    @Test
    public void testRewrite() throws ParseException {
        assertEquals(
            "{\"seed\": \"sable1234\"}",
            rewrite(
                "{\"seed\": ${{seed}}}",
                "{\"seed\": \"sable1234\"}"
            ).parse("{\"seed\": \"as23}sdkdf\"}").value
        );
        assertEquals(
            "second first",
            rewrite(
                "first ${{second}}",
                "${{second}} first"
            ).parse("first second").value
        );
        // assertEquals(" " + expected, transformation.parse(" " + input).value);
    }
}