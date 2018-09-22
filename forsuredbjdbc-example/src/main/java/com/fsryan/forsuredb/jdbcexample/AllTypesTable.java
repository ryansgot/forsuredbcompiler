package com.fsryan.forsuredb.jdbcexample;

import com.fsryan.forsuredb.annotations.FSColumn;
import com.fsryan.forsuredb.annotations.FSDefault;
import com.fsryan.forsuredb.annotations.FSStaticData;
import com.fsryan.forsuredb.annotations.FSTable;
import com.fsryan.forsuredb.annotations.Index;
import com.fsryan.forsuredb.api.FSGetApi;
import com.fsryan.forsuredb.api.Retriever;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

@FSTable("all_types")
@FSStaticData("all_types_static_data.xml")
public interface AllTypesTable extends FSGetApi {
    @FSColumn("int_column") @FSDefault("42") @Index int intColumn(Retriever retriever);
    @FSColumn("integer_wrapper_column") @FSDefault("1792") Integer integerWrapperColumn(Retriever retriever);
    @FSColumn("long_column") long longColumn(Retriever retriever);
    @FSColumn("long_wrapper_column") Long longWrapperColumn(Retriever retriever);
    @FSColumn("float_column") float floatColumn(Retriever retriever);
    @FSColumn("float_wrapper_column") Float floatWrapperColumn(Retriever retriever);
    @FSColumn("double_column") double doubleColumn(Retriever retriever);
    @FSColumn("double_wrapper_column") Double doubleWrapperColumn(Retriever retriever);
    @FSColumn("byte_array_column") @FSDefault("abcd1234") byte[] byteArrayColumn(Retriever retriever);
    @FSColumn("string_column") @FSDefault("Some String with a '") String stringColumn(Retriever retriever);
    @FSColumn("big_integer_column") BigInteger bigIntegerColumn(Retriever retriever);
    @FSColumn("big_decimal_column") BigDecimal bigDecimalColumn(Retriever retriever);
    @FSColumn("date_column") Date dateColumn(Retriever retriever);
    @FSColumn("boolean_column") boolean booleanColumn(Retriever retriever);
    @FSColumn("boolean_wrapper_column") Boolean booleanWrapperColumn(Retriever retriever);
}
