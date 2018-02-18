package com.fsryan.forsuredb.integrationtest;

import java.util.concurrent.ThreadLocalRandom;

public class RandomString {

    public static final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String lower = upper.toLowerCase();
    public static final String digits = "0123456789";
    public static final String alphanum = upper + lower + digits;

    private final char[] symbols;
    private final char[] buf;

    public RandomString(int length, String symbols) {
        if (length < 1) {
            throw new IllegalArgumentException();
        }
        if (symbols.length() < 2) {
            throw new IllegalArgumentException();
        }
        this.symbols = symbols.toCharArray();
        this.buf = new char[length];
    }

    public RandomString(int length) {
        this(length, alphanum);
    }

    public RandomString() {
        this(21);
    }

    public String nextString() {
        for (int idx = 0; idx < buf.length; ++idx) {
            buf[idx] = symbols[ThreadLocalRandom.current().nextInt(symbols.length)];
        }
        return new String(buf);
    }
}
