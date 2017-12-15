package com.fsryan.forsuredb.util;

import java.util.Date;
import java.util.Random;

public class RandomValueSuggester implements ValueSuggester {
    private final Random r;

    public RandomValueSuggester() {
        this.r = new Random();
    }
    @Override
    public Integer intSuggestion(String columnName) {
        return r.nextInt();
    }

    @Override
    public Long longSuggestion(String columnName) {
        return r.nextLong();
    }

    @Override
    public Float floatSuggestion(String columnName) {
        return r.nextFloat();
    }

    @Override
    public Double doubleSuggestion(String columnName) {
        return r.nextDouble();
    }

    @Override
    public String stringSuggestion(boolean limitedToHex, String columnName) {
        // TODO: randomize the non-hex string
        return limitedToHex ? Long.toHexString(r.nextLong()) : "Hello, World!";
    }

    @Override
    public Date dateSuggestion(String columnName) {
        return new Date(r.nextLong());
    }
}
