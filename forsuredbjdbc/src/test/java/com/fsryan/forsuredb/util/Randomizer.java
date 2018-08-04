package com.fsryan.forsuredb.util;

import com.fsryan.forsuredb.api.TypedRecordContainer;

import java.lang.reflect.Type;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;

public class Randomizer {

    private static final Type[] supportedTypes = new Type[] {
            int.class,
            long.class,
            float.class,
            double.class,
            String.class,
            byte[].class
    };

    public static TypedRecordContainer createRandomTRC() {
        return createRandomTRC(new Random());
    }

    public static TypedRecordContainer createRandomTRC(Random r) {
        int size = r.nextInt(32) + 1;
        Type[] types = new Type[r.nextInt(supportedTypes.length) + 1];
        for (int i = 0; i < types.length; i++) {
            types[i] = supportedTypes[r.nextInt(supportedTypes.length)];
        }
        return createRandomTRC(size, types);
    }

    public static TypedRecordContainer createRandomStringTRC() {
        return createRandomTRC(new Random(), String.class);
    }

    public static TypedRecordContainer createRandomStringTRC(int size) {
        return createRandomTRC(size, String.class);
    }

    public static TypedRecordContainer createRandomIntTRC() {
        return createRandomTRC(new Random(), int.class);
    }

    public static TypedRecordContainer createRandomIntTRC(int size) {
        return createRandomTRC(size, int.class);
    }

    public static TypedRecordContainer createRandomLongTRC() {
        return createRandomTRC(new Random(), long.class);
    }

    public static TypedRecordContainer createRandomLongTRC(int size) {
        return createRandomTRC(size, long.class);
    }

    public static TypedRecordContainer createRandomFloatTRC(int size) {
        return createRandomTRC(size, float.class);
    }

    public static TypedRecordContainer createRandomFloatTRC() {
        return createRandomTRC(new Random(), float.class);
    }

    public static TypedRecordContainer createRandomDoubleTRC() {
        return createRandomTRC(new Random(), double.class);
    }

    public static TypedRecordContainer createRandomDoubleTRC(int size) {
        return createRandomTRC(size, double.class);
    }

    public static TypedRecordContainer createRandomByteArrayTRC() {
        return createRandomTRC(new Random(), byte[].class);
    }

    public static TypedRecordContainer createRandomByteArrayTRC(int size) {
        return createRandomTRC(size, byte[].class);
    }

    public static TypedRecordContainer createRandomTRC(int size, Type... types) {
        return createRandomTRC(new Random(), size, types);
    }

    public static TypedRecordContainer createRandomTRC(Random r, Type... types) {
        return createRandomTRC(r, r.nextInt(32) + 1, types);
    }

    public static TypedRecordContainer createRandomTRC(Random r, int size, Type... types) {
        if (types.length == 0) {
            throw new IllegalArgumentException("must specify at least one type");
        }
        TypedRecordContainer ret = new TypedRecordContainer();

        IntStream.range(0, size).forEach(i -> {
            String key = "col" + (i + 1);
            Type t  = types[r.nextInt(types.length)];
            if (t.equals(String.class)) {
                ret.put(key, randomString());
            } else if (t.equals(int.class) || t.equals(Integer.class)) {
                ret.put(key, r.nextInt());
            } else if (t.equals(long.class) || t.equals(Long.class)) {
                ret.put(key, r.nextLong());
            } else if (t.equals(float.class) || t.equals(Float.class)) {
                ret.put(key, randomFloat(r));
            } else if (t.equals(double.class) || t.equals(Double.class)) {
                ret.put(key, randomDouble(r));
            } else if (t.equals(byte[].class)) {
                ret.put(key, randomByteArray(r));
            } else {
                throw new IllegalArgumentException("Unsuppported type: " + t);
            }
        });
        return ret;
    }

    public static byte[] randomByteArray() {
        return randomByteArray(32);
    }

    public static byte[] randomByteArray(int lengthLimit) {
        return randomByteArray(new Random(), lengthLimit);
    }

    public static byte[] randomByteArray(Random r) {
        return randomByteArray(r, 32);
    }

    public static byte[] randomByteArray(Random r, int lengthLimit) {
        byte[] ret = new byte[r.nextInt(lengthLimit) + 1];
        r.nextBytes(ret);
        return ret;
    }

    public static String randomString() {
        return randomString(32);
    }

    public static String randomString(Random r) {
        return randomString(r, 32);
    }

    public static String randomString(int lengthLimit) {
        return randomString(new Random(), lengthLimit);
    }

    public static String randomString(Random r, int lengthLimit) {
        int length = r.nextInt(32) + 1;
        return new RandomString(length, r).nextString();
    }

    public static float randomFloat() {
        return randomFloat(new Random());
    }

    public static float randomFloat(Random r) {
        return r.nextFloat() * (r.nextInt() >> 16);
    }

    public static double randomDouble() {
        return randomDouble(new Random());
    }

    public static double randomDouble(Random r) {
        return r.nextDouble() * r.nextInt();
    }

    private static class RandomString {

        public static final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        public static final String lower = upper.toLowerCase();
        public static final String digits = "0123456789";
        public static final String alphanum = upper + lower + digits;

        private final Random random;
        private final char[] symbols;
        private final char[] buf;

        public RandomString(int length, Random random, String symbols) {
            if (length < 1) {
                throw new IllegalArgumentException();
            }
            if (symbols.length() < 2) {
                throw new IllegalArgumentException();
            }
            this.random = Objects.requireNonNull(random);
            this.symbols = symbols.toCharArray();
            this.buf = new char[length];
        }

        public RandomString(int length, Random random) {
            this(length, random, alphanum);
        }

        public RandomString(int length) {
            this(length, new SecureRandom());
        }

        public RandomString() {
            this(21);
        }

        public String nextString() {
            for (int idx = 0; idx < buf.length; ++idx)
                buf[idx] = symbols[random.nextInt(symbols.length)];
            return new String(buf);
        }
    }
}
