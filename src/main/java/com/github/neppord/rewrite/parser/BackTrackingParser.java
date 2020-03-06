package com.github.neppord.rewrite.parser;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface BackTrackingParser<V> {

    public Stream<Result<V>> parse(CharSequence c);

    default BackTrackingParser<V> or(BackTrackingParser<V> other) {
        return c -> {
            final Stream<Result<V>> stream = this.parse(c);
            final Stream<Result<V>> stream1 = other.parse(c);
            return Stream.concat(stream, stream1);
        };
    }

    default <U> BackTrackingParser<U> map(Function<V, U> f) {
        return c -> this.parse(c).map(r -> r.map(f));
    }

    default Parser<V> toParser() {
        return c -> {
            final Optional<Result<V>> first = this.parse(c).findFirst();
            if (first.isPresent()) {
                return first.get();
            }
            throw new ParseException("No solutions found");
        };
    }
}
