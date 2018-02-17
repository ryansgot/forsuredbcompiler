package com.fsyran.forsuredb.integrationtest;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

public abstract class TestUtil {

    public static double SMALL_DOUBLE = 0.0000000001D;
    public static double SMALL_FLOAT = 0.0000000001F;


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
}
