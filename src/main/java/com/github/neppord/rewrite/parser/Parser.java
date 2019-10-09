package com.github.neppord.rewrite.parser;


import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.neppord.rewrite.parser.Functional.fix;
import static java.util.Collections.*;

public interface Parser<V> {

    static <V> Parser<V> laizy(Supplier<Parser<V>> supplier) {
        return c -> supplier.get().parse(c);
    }

    static Function<Map<String, String>, Map<String, String>> mergeMaps(Map<String, String> m1) {
        return m2 -> {
            HashMap<String, String> ret = new HashMap<>();
            ret.putAll(m1);
            ret.putAll(m2);
            return ret;
        };
    }

    static <U> Parser<U> some(Function<U, Function<U, U>> f, Parser<U> parser, U inNone) {
        return many(f, parser).or(value(inNone));
    }

    static <U> Parser<U> many(Function<U, Function<U, U>> f, Parser<U> parser) {
        return c -> {
             try {
                 return many(f, parser).apply(parser.map(f)).parse(c);
             } catch (ParseException e) {
                 return parser.parse(c);
             }
        };
    }

    static <U >Parser<U> value(U value) {
        return c -> new Result<>(value, c);
    }

    Result<V> parse(CharSequence c) throws ParseException;

    default <U> Parser<U> map(Function<V,U> f) {
        return c -> this.parse(c).map(f);
    }

    default Parser<V> or(Parser<V> alternative) {
        return c -> {
            try {
                return parse(c);
            } catch (ParseException e) {
                return alternative.parse(c);
            }
        };
    }

    default <U> Parser<U> apply(Parser<Function<V, U>> other) {
        return c -> {
            Result<Function<V, U>> result = other.parse(c);
            return map(result.value).parse(result.rest);
        };
    }

    default <L , R, U> Parser<U> surroundWith(Function<L, Function<V, Function<R, U>>> concat, Parser<L> left, Parser<R> right) {
        return right.apply(apply(left.map(concat)));
    }
}
