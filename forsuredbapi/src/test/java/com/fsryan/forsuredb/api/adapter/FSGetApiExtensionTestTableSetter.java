package com.fsryan.forsuredb.api.adapter;

import com.fsryan.forsuredb.annotations.FSColumn;
import com.fsryan.forsuredb.api.FSSaveApi;

import java.math.BigDecimal;
import java.util.Date;

/*package*/ interface FSGetApiExtensionTestTableSetter extends FSSaveApi<String> {

    String TABLE_NAME = "forsuredb_test_table";

    /**
     * <p>
     *   Set the value of the _id column to be updated
     * </p>
     */
    @FSColumn("_id")
    FSGetApiExtensionTestTableSetter id(long id);

    /**
     * <p>
     *   Set the value of the big_decimal_column column to be updated
     * </p>
     */
    @FSColumn("big_decimal_column")
    FSGetApiExtensionTestTableSetter bigDecimalColumn(BigDecimal bigDecimalColumn);

    /**
     * <p>
     *   Set the value of the boolean_column column to be updated
     * </p>
     */
    @FSColumn("boolean_column")
    FSGetApiExtensionTestTableSetter booleanColumn(boolean booleanColumn);

    /**
     * <p>
     *   Set the value of the boolean_wrapper_column column to be updated
     * </p>
     */
    @FSColumn("boolean_wrapper_column")
    FSGetApiExtensionTestTableSetter booleanWrapperColumn(Boolean booleanWrapperColumn);

    /**
     * <p>
     *   Set the value of the boolean_wrapper_column column to be updated
     * </p>
     */
    @FSColumn("boolean_wrapper_column")
    FSGetApiExtensionTestTableSetter byteArrayColumn(byte[] byteArrayColumn);

    /**
     * <p>
     *   Set the value of the date_column column to be updated
     * </p>
     */
    @FSColumn("date_column")
    FSGetApiExtensionTestTableSetter dateColumn(Date dateColumn);

    /**
     * <p>
     *   Set the value of the deleted column to be updated
     * </p>
     */
    @FSColumn("deleted")
    FSGetApiExtensionTestTableSetter deleted(boolean deleted);

    /**
     * <p>
     *   Set the value of the double_column column to be updated
     * </p>
     */
    @FSColumn("double_column")
    FSGetApiExtensionTestTableSetter doubleColumn(double doubleColumn);

    /**
     * <p>
     *   Set the value of the double_wrapper_column column to be updated
     * </p>
     */
    @FSColumn("double_wrapper_column")
    FSGetApiExtensionTestTableSetter doubleWrapperColumn(Double doubleWrapperColumn);

    /**
     * <p>
     *   Set the value of the int_column column to be updated
     * </p>
     */
    @FSColumn("int_column")
    FSGetApiExtensionTestTableSetter intColumn(int intColumn);

    /**
     * <p>
     *   Set the value of the integer_wrapper_column column to be updated
     * </p>
     */
    @FSColumn("integer_wrapper_column")
    FSGetApiExtensionTestTableSetter integerWrapperColumn(Integer integerWrapperColumn);

    /**
     * <p>
     *   Set the value of the long_column column to be updated
     * </p>
     */
    @FSColumn("long_column")
    FSGetApiExtensionTestTableSetter longColumn(long longColumn);

    /**
     * <p>
     *   Set the value of the long_wrapper_column column to be updated
     * </p>
     */
    @FSColumn("long_wrapper_column")
    FSGetApiExtensionTestTableSetter longWrapperColumn(Long longWrapperColumn);

    /**
     * <p>
     *   Set the value of the long_wrapper_column column to be updated
     * </p>
     */
    @FSColumn("string_column")
    FSGetApiExtensionTestTableSetter stringColumn(String stringColumn);
}
