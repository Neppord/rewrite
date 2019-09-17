package com.github.neppord.rewrite.parser;

public class Result<V> {
    public final V value;
    public final CharSequence c;
    public Result(V value, CharSequence c) {
        this.value = value;
        this.c = c;
    }
}
