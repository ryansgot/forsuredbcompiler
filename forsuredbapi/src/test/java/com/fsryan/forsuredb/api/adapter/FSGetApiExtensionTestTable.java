package com.fsryan.forsuredb.api.adapter;

import com.fsryan.forsuredb.annotations.FSColumn;
import com.fsryan.forsuredb.annotations.FSTable;
import com.fsryan.forsuredb.api.FSGetApi;
import com.fsryan.forsuredb.api.Retriever;

import java.math.BigDecimal;

@FSTable("forsuredb_test_table")
/*package*/ interface FSGetApiExtensionTestTable extends FSGetApi {
    @FSColumn("big_decimal_column") BigDecimal bigDecimalColumn(Retriever retriever);
    @FSColumn("boolean_column") boolean booleanColumn(Retriever retriever);
    @FSColumn("boolean_wrapper_column") Boolean booleanWrapperColumn(Retriever retriever);
    @FSColumn("byte_array_column") byte[] byteArrayColumn(Retriever retriever);
    @FSColumn("double_column") double doubleColumn(Retriever retriever);
    @FSColumn("double_wrapper_column") Double doubleWrapperColumn(Retriever retriever);
    @FSColumn("int_column") int intColumn(Retriever retriever);
    @FSColumn("integer_wrapper_column") Integer integerWrapperColumn(Retriever retriever);
    @FSColumn("long_column") long longColumn(Retriever retriever);
    @FSColumn("long_wrapper_column") Long longWrapperColumn(Retriever retriever);
    @FSColumn("string_column") String stringColumn(Retriever retriever);
}
