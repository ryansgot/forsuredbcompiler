package com.fsryan.forsuredb.util;

import org.beryx.textio.TextIO;

import java.text.ParseException;
import java.util.Date;

import static com.fsryan.forsuredb.jdbcexample.ExampleApp.DATE_FORMAT;
import static com.fsryan.forsuredb.jdbcexample.ExampleApp.DATE_FORMAT_STRING;

public abstract class RecordInputter<T> {

    private final TextIO textIO;
    private final ValueSuggester valueSuggester;

    RecordInputter(TextIO textIO, ValueSuggester valueSuggester) {
        this.textIO = textIO;
        this.valueSuggester = valueSuggester;
    }

    public abstract T createRecord();

    protected Integer readIntColumn(String columnName) {
        return textIO.newIntInputReader()
                .withDefaultValue(valueSuggester.intSuggestion(columnName))
                .read(columnName);
    }

    protected Long readLongColumn(String columnName) {
        return textIO.newLongInputReader()
                .withDefaultValue(valueSuggester.longSuggestion(columnName))
                .read(columnName);
    }

    protected Float readFloatColumn(String columnName) {
        return textIO.newFloatInputReader()
                .withDefaultValue(valueSuggester.floatSuggestion(columnName))
                .read(columnName);
    }

    protected Double readDoubleColumn(String columnName) {
        return textIO.newDoubleInputReader()
                .withDefaultValue(valueSuggester.doubleSuggestion(columnName))
                .read(columnName);
    }

    protected String readStringColumn(boolean forceHex, String columnName) {
        return textIO.newStringInputReader()
                .withDefaultValue(valueSuggester.stringSuggestion(forceHex, columnName))
                .read(columnName);
    }

    protected Date readDateColumn(String columnName) {
        try {
            return DATE_FORMAT.parse(textIO.newStringInputReader()
                    .withDefaultValue(DATE_FORMAT.format(valueSuggester.dateSuggestion(columnName)))
                    .read(columnName + " (" + DATE_FORMAT_STRING + ")"));
        } catch (ParseException pe) {
            textIO.getTextTerminal().println("Invalid input format. Must use format:" + DATE_FORMAT_STRING);
        }
        return readDateColumn(columnName);
    }
}
