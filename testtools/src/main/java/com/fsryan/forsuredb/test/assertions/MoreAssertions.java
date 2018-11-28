package com.fsryan.forsuredb.test.assertions;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.fail;

public class MoreAssertions {

    public static <T> void assertCallThrowsException(T arg, Function<T, ?> fThatThrows) {
        try {
            fThatThrows.apply(arg);
        } catch (Exception e) {
            return;
        }
        fail("Should have thrown exception but didn't: " + fThatThrows);
    }
}
