package com.github.neppord.rewrite.parser;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Functional {
    static <V> V fix(Function<Supplier<V>, V> f) {
        Supplier<V> t = () -> fix(f);
        return f.apply(t);
    }

    static Function<CharSequence, Function<CharSequence, CharSequence>> concat3(CharSequence first) {
        return second -> third -> first.toString() + second + third;
    }

    static Function<CharSequence, CharSequence> concat2(CharSequence first) {
        return second -> first.toString() + second;
    }
}
