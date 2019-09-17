package com.github.neppord.rewrite.parser;

public class ParseException extends Exception {
    public final String message;

    public ParseException(String message) {
        super(message);
        this.message = message;
    }
}
