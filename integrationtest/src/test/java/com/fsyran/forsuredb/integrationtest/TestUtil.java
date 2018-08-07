package com.fsyran.forsuredb.integrationtest;

import com.fsryan.forsuredb.api.Retriever;
import com.fsryan.forsuredb.integrationtest.singletable.AllTypesTable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class TestUtil {

    public static final ThreadLocal<DateFormat> DATE_FORMAT
            = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));

    public interface Unpacker<T> {
        T unpack(AttemptedSavePair<AllTypesTable.Record> asr);
    }

    public static double SMALL_DOUBLE = 0.0000000001D;
    public static double SMALL_FLOAT = 0.0000000001F;


    /**
     * <p>Same algorithm as sqlite (memcmp())
     */
    public static final Comparator<byte[]> MEMCMP_COMPARATOR = (left, right) -> {
        int minLength = Math.min(left.length, right.length);
        for (int i = 0; i < minLength; i++) {
            int diff = (left[i] & 0xFF) - (right[i] & 0xFF);
            if (diff != 0) {
                return diff;
            }
        }
        return left.length - right.length;
    };

    // copied from: https://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java
    public static byte[] bytesFromHex(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }

    public static <T> List<T> retrieveToList(Retriever r, Function<Retriever, T> retrieverUnpacker) {
        try {
            if (!r.moveToFirst()) {
                return Collections.emptyList();
            }
            List<T> ret = new ArrayList<>();
            do {
                ret.add(retrieverUnpacker.apply(r));
            } while (r.moveToNext());

            return ret;
        } finally {
            r.close();
        }
    }

    public static int randomInt() {
        return ThreadLocalRandom.current().nextInt();
    }

    public static int randomInt(int bound) {
        return ThreadLocalRandom.current().nextInt(bound);
    }

    public static long randomLong() {
        return ThreadLocalRandom.current().nextLong();
    }

    public static float randomFloat() {
        return ThreadLocalRandom.current().nextFloat() * ThreadLocalRandom.current().nextInt();
    }

    public static double randomDouble() {
        return ThreadLocalRandom.current().nextDouble() * ThreadLocalRandom.current().nextLong();
    }

    public static Date randomDate() {
        return new Date(ThreadLocalRandom.current().nextLong(0, new Date().getTime()));
    }

    public static <T extends Comparable<T>> Pair<T, T> randomRange(Supplier<T> supplier) {
        T t1 = supplier.get();
        T t2 = supplier.get();
        while (t1.equals(t2)) {
            t2 = supplier.get();
        }
        return t1.compareTo(t2) > 0 ? new Pair<>(t2, t1) : new Pair<>(t1, t2);
    }

    public static Pair<byte[], byte[]> randomByteArrayRange(int lowSizeLimit, int highSizeLimit) {
        byte[] ba1 = new byte[ThreadLocalRandom.current().nextInt(lowSizeLimit, highSizeLimit)];
        byte[] ba2 = new byte[ThreadLocalRandom.current().nextInt(lowSizeLimit, highSizeLimit)];
        ThreadLocalRandom.current().nextBytes(ba1);
        ThreadLocalRandom.current().nextBytes(ba2);
        while (Arrays.equals(ba1, ba2)) {
            ThreadLocalRandom.current().nextBytes(ba2);
        }
        return MEMCMP_COMPARATOR.compare(ba1, ba2) < 0 ? new Pair<>(ba1, ba2) : new Pair<>(ba2, ba1);
    }

    public static <T extends Comparable<T>> Predicate<AttemptedSavePair<AllTypesTable.Record>> isBetween(Pair<T, T> range, boolean lowerInclusive, boolean upperInclusive, Unpacker<T> unpacker) {
        return asp -> {
            int lowCompare = range.first.compareTo(unpacker.unpack(asp));
            if (lowCompare > 0 || (!lowerInclusive && lowCompare == 0)) {
                return false;
            }
            int highCompare = range.second.compareTo(unpacker.unpack(asp));
            return highCompare >= 0 && (upperInclusive || highCompare != 0);
        };
    }
}
