package com.fsryan.forsuredb.test.assertions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public abstract class AssertCollection {

    public static <K, V> void assertMapEquals(String desc, Map<K, V> expected, Map<K, V> actual) {
        if (handledNullPossiblity(desc, expected, actual)) {
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
        fail(failPrepend(desc) + message);
    }

//    public static <T> void assertSetEquals(Set<T> expected, Set<T> actual) {
//        assertSetEquals(null, expected, actual);
//    }

    public static <T> void assertSetEquals(String desc, Set<T> expected, Set<T> actual) {
        if (handledNullPossiblity(desc, expected, actual)) {
            return;
        }

        Set<T> excess = new HashSet<>(actual);
        Set<T> missing = new HashSet<>();
        for (T expectedItem : expected) {
            if (!excess.remove(expectedItem)) {
                missing.add(expectedItem);
            }
        }

        if (missing.size() == 0 && excess.size() == 0) {
            return;
        }

        String message = "\nexpected: " + expected + "\n" +
                "excess:   " + excess + "\n" +
                "missing:  " + missing;
        fail(failPrepend(desc) + message);
    }

    private static boolean handledNullPossiblity(String desc, Object expected, Object actual) {
        if (expected == null) {
            assertNull(actual);
            return true;
        }
        if (actual == null) {
            throw new NullPointerException(failPrepend(desc) + "expected non null: " + expected);
        }
        return false;
    }

    private static String failPrepend(String desc) {
        return desc == null ? "" : desc + "\n";
    }
}
