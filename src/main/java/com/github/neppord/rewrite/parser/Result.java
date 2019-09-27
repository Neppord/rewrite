package com.github.neppord.rewrite.parser;

import java.util.function.Function;

public class Result<V> {
    public final V value;
    public final CharSequence rest;
    public Result(V value, CharSequence rest) {
        this.value = value;
        this.rest = rest;
    }

    public <U> Result<U> map(Function<V, U> f) {
        return new Result<>(f.apply(value), rest);
    }
}
