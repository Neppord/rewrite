package com.github.neppord.rewrite.parser;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Functional {
    static <V> V fix(Function<Supplier<V>, V> f) {
        Supplier<V> t = () -> fix(f);
        return f.apply(t);
    }
}
