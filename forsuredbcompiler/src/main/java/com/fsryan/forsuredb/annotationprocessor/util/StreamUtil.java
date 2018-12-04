package com.fsryan.forsuredb.annotationprocessor.util;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collector;
import java.util.stream.Stream;

public abstract class StreamUtil {

    public static <K, T> Collector<T, Map<K, T>, Map<K, T>> mapCollector(BiConsumer<Map<K,T>, T> accumulator) {
        return Collector.of(
                HashMap::new,
                accumulator,
                (Map<K, T> m1, Map<K, T> m2) -> {
                    Map<K, T> ret = new HashMap<>(m1.size() + m2.size());
                    ret.putAll(m1);
                    ret.putAll(m2);
                    return ret;
                }
        );
    }

    @SafeVarargs
    @Nonnull
    public static <T> Stream<T> concatAll(Stream<T>... streams) {
        if (streams == null) {
            throw new IllegalArgumentException("null input not allowed for streams");
        }
        if (streams.length == 0) {
            return Stream.empty();
        }

        Stream<T> ret = streams[0];
        for (int i = 1; i < streams.length; i++) {
            ret = Stream.concat(ret, streams[i]);
        }
        return ret;
    }
}
