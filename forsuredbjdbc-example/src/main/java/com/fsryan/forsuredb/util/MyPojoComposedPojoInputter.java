package com.fsryan.forsuredb.util;

import com.fsryan.forsuredb.jdbcexample.MyPojo;
import org.beryx.textio.TextIO;

public class MyPojoComposedPojoInputter extends RecordInputter<MyPojo.ComposedPojo> {

    private MyPojoComposedPojoInputter(TextIO textIO, ValueSuggester valueSuggester) {
        super(textIO, valueSuggester);
    }

    public static MyPojoComposedPojoInputter withRandomSuggestions(TextIO textIO) {
        return new MyPojoComposedPojoInputter(textIO, new RandomValueSuggester());
    }

    public static MyPojoComposedPojoInputter withoutSuggestions(TextIO textIO) {
        return new MyPojoComposedPojoInputter(textIO, new NullValueSuggester());
    }

    @Override
    public MyPojo.ComposedPojo createRecord() {
        MyPojo.ComposedPojo record = new MyPojo.ComposedPojo();
        record.setComposedInt(readIntColumn("composedInt"));
        record.setComposedString(readStringColumn(false, "composedString"));
        return record;
    }
}
