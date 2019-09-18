package com.github.neppord.rewrite.parser;

import java.util.function.Function;

public class Result<V> {
    public final V value;
    public final CharSequence c;
    public Result(V value, CharSequence c) {
        this.value = value;
        this.c = c;
    }

    public <U> Result<U> map(Function<V, U> f) {
        return new Result<>(f.apply(value), c);
    }
}
