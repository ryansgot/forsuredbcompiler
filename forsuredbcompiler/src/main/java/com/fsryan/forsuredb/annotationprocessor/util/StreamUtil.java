package com.fsryan.forsuredb.annotationprocessor.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collector;

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
}
