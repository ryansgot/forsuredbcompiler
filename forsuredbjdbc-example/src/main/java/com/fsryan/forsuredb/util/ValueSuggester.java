package com.fsryan.forsuredb.util;

import java.util.Date;

public interface ValueSuggester {
    Integer intSuggestion(String columnName);
    Long longSuggestion(String columnName);
    Float floatSuggestion(String columnName);
    Double doubleSuggestion(String columnName);
    String stringSuggestion(boolean limitedToHex, String columnName);
    Date dateSuggestion(String columnName);
}
