package com.fsryan.forsuredb.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionUtil {

    public static <K, V> Map<K, V> mapOf() {
        return new HashMap<>();
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1) {
        Map<K, V> ret = mapOf();
        ret.put(k1, v1);
        return ret;
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
        Map<K, V> ret = mapOf(k1, v1);
        ret.put(k2, v2);
        return ret;
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K, V> ret = mapOf(k1, v1, k2, v2);
        ret.put(k3, v3);
        return ret;
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        Map<K, V> ret = mapOf(k1, v1, k2, v2, k3, v3);
        ret.put(k4, v4);
        return ret;
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        Map<K, V> ret = mapOf(k1, v1, k2, v2, k3, v3, k4, v4);
        ret.put(k5, v5);
        return ret;
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
        Map<K, V> ret = mapOf(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
        ret.put(k6, v6);
        return ret;
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6,
                                         K k7, V v7) {
        Map<K, V> ret = mapOf(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6);
        ret.put(k7, v7);
        return ret;
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6,
                                         K k7, V v7, K k8, V v8) {
        Map<K, V> ret = mapOf(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7);
        ret.put(k8, v8);
        return ret;
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6,
                                         K k7, V v7, K k8, V v8, K k9, V v9) {
        Map<K, V> ret = mapOf(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8);
        ret.put(k9, v9);
        return ret;
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6,
                                         K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
        Map<K, V> ret = mapOf(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9);
        ret.put(k10, v10);
        return ret;
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6,
                                         K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10, K k11, V v11) {
        Map<K, V> ret = mapOf(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10);
        ret.put(k11, v11);
        return ret;
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6,
                                         K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10, K k11, V v11, K k12, V v12) {
        Map<K, V> ret = mapOf(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10, k11, v11);
        ret.put(k12, v12);
        return ret;
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6,
                                         K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10, K k11, V v11, K k12, V v12,
                                         K k13, V v13) {
        Map<K, V> ret = mapOf(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10, k11, v11, k12, v12);
        ret.put(k13, v13);
        return ret;
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6,
                                         K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10, K k11, V v11, K k12, V v12,
                                         K k13, V v13, K k14, V v14) {
        Map<K, V> ret = mapOf(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10, k11, v11, k12, v12, k13, v13);
        ret.put(k14, v14);
        return ret;
    }

    public static <T> ArrayList<T> arrayListOf(T... ts) {
        ArrayList<T> ret = new ArrayList<>(ts.length);
        for (T t : ts) {
            ret.add(t);
        }
        return ret;
    }
}
