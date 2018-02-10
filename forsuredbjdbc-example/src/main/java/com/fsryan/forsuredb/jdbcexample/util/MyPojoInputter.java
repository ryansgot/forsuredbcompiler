package com.fsryan.forsuredb.jdbcexample.util;

import com.fsryan.forsuredb.jdbcexample.MyPojo;
import org.beryx.textio.TextIO;

public class MyPojoInputter extends RecordInputter<MyPojo> {

    private final MyPojoComposedPojoInputter composedInputter;

    private MyPojoInputter(TextIO textIO, ValueSuggester valueSuggester, MyPojoComposedPojoInputter composedInputter) {
        super(textIO, valueSuggester);
        this.composedInputter = composedInputter;
    }

    public static MyPojoInputter withRandomSuggestions(TextIO textIO) {
        return new MyPojoInputter(
                textIO,
                new RandomValueSuggester(),
                MyPojoComposedPojoInputter.withRandomSuggestions(textIO)
        );
    }

    public static MyPojoInputter withoutSuggestions(TextIO textIO) {
        return new MyPojoInputter(
                textIO,
                new NullValueSuggester(),
                MyPojoComposedPojoInputter.withoutSuggestions(textIO)
        );
    }

    @Override
    public MyPojo createRecord() {
        MyPojo record = new MyPojo();
        record.setAwesomeInt(readIntColumn("awesomeInt"));
        record.setAwesomeString(readStringColumn(false, "awesomeString"));
        record.setComposedPojo(composedInputter.createRecord());
        return record;
    }
}
