package com.fsryan.forsuredb.test.assertions;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public abstract class AssertCollection {

    public static <K, V> void assertMapEquals(Map<K, V> expected, Map<K, V> actual) {
        if (handledNullPossiblity(expected, actual)) {
            return;
        }

        Map<K, V> excess = new HashMap<>(actual);
        Map<K, V> missing = new HashMap<>();
        Map<K, V> unequal = new HashMap<>();
        for (Map.Entry<K, V> entry : expected.entrySet()) {
            K key = entry.getKey();
            V actualV = excess.remove(key);
            V expectedV = entry.getValue();
            if (actualV == null) {
                missing.put(key, expectedV);
            } else if (!expectedV.equals(actualV)) {
                unequal.put(key, actualV);
            }
        }

        if (missing.size() == 0 && unequal.size() == 0 && excess.size() == 0) {
            return;
        }

        String message = "\nexpected: " + expected + "\n" +
                "excess:   " + excess + "\n" +
                "missing:  " + missing + "\n" +
                "unequal:  " + unequal;
        fail(message);
    }

    private static boolean handledNullPossiblity(Object expected, Object actual) {
        if (expected == null) {
            assertNull(actual);
            return true;
        }
        if (actual == null) {
            throw new NullPointerException("expected non null: " + expected);
        }
        return false;
    }
}
