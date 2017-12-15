package com.fsryan.forsuredb.jdbcexample.util;

import java.util.Date;

public class NullValueSuggester implements ValueSuggester {

    @Override
    public Integer intSuggestion(String columnName) {
        return null;
    }

    @Override
    public Long longSuggestion(String columnName) {
        return null;
    }

    @Override
    public Float floatSuggestion(String columnName) {
        return null;
    }

    @Override
    public Double doubleSuggestion(String columnName) {
        return null;
    }

    @Override
    public String stringSuggestion(boolean limitedToHex, String columnName) {
        return null;
    }

    @Override
    public Date dateSuggestion(String columnName) {
        return null;
    }
}
