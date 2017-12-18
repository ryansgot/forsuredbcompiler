package com.fsryan.forsuredb.jdbcexample.util;

import org.beryx.textio.TextIO;

import java.math.BigDecimal;
import java.math.BigInteger;

import static com.fsryan.forsuredb.jdbcexample.util.StringUtil.hexStringToByteArray;

public class RecordModelInputter extends RecordInputter<RecordModel> {

    private RecordModelInputter(TextIO textIO, ValueSuggester valueSuggester) {
        super(textIO, valueSuggester);
    }

    public static RecordModelInputter withRandomSuggestions(TextIO textIO) {
        return new RecordModelInputter(textIO, new RandomValueSuggester());
    }

    public static RecordModelInputter withoutSuggestions(TextIO textIO) {
        return new RecordModelInputter(textIO, new NullValueSuggester());
    }

    @Override
    public final RecordModel createRecord() {
        RecordModel ret = new RecordModel();
        ret.setIntColumn(readIntColumn("int_column"));
        ret.setIntegerWrapperColumn(readIntColumn("integer_wrapper_column"));
        ret.setLongColumn(readLongColumn("long_column"));
        ret.setLongWrapperColumn(readLongColumn("long_wrapper_column"));
        ret.setFloatColumn(readFloatColumn("float_column"));
        ret.setFloatWrapperColumn(readFloatColumn("float_wrapper_column"));
        ret.setDoubleColumn(readDoubleColumn("double_column"));
        ret.setDoubleWrapperColumn(readDoubleColumn("double_wrapper_column"));
        ret.setByteArrayColumn(hexStringToByteArray(readStringColumn(true, "byte_array_column")));
        ret.setStringColumn(readStringColumn(false, "string_column"));
        ret.setBigIntegerColumn(new BigInteger(Long.toString(readLongColumn("big_integer_column"))));
        ret.setBigDecimalColumn(new BigDecimal(Double.toString(readDoubleColumn("big_decimal_column"))));
        ret.setDateColumn(readDateColumn("date_column"));
        return ret;
    }
}
