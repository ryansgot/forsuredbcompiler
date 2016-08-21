package com.fsryan.forsuredb.api.adapter;

import com.fsryan.forsuredb.annotations.FSColumn;
import com.fsryan.forsuredb.api.FSDocStoreSaveApi;

import java.math.BigDecimal;
import java.util.Date;

public interface FSDocStoreGetApiExtensionTestTableSetter extends FSDocStoreSaveApi<String, DocStoreTestBase> {
    String TABLE_NAME = "forsuredb_doc_store_test";

    /**
     * <p>
     *   Set the value of the _id column to be updated
     * </p>
     */
    @FSColumn("_id")
    FSDocStoreGetApiExtensionTestTableSetter id(long id);

    /**
     * <p>
     *   Set the value of the big_decimal_column column to be updated
     * </p>
     */
    @FSColumn("big_decimal_column")
    FSDocStoreGetApiExtensionTestTableSetter bigDecimalColumn(BigDecimal bigDecimalColumn);

    /**
     * <p>
     *   Set the value of the boolean_column column to be updated
     * </p>
     */
    @FSColumn("boolean_column")
    FSDocStoreGetApiExtensionTestTableSetter booleanColumn(boolean booleanColumn);

    /**
     * <p>
     *   Set the value of the boolean_wrapper_column column to be updated
     * </p>
     */
    @FSColumn("boolean_wrapper_column")
    FSDocStoreGetApiExtensionTestTableSetter booleanWrapperColumn(Boolean booleanWrapperColumn);

    /**
     * <p>
     *   Set the value of the date_column column to be updated
     * </p>
     */
    @FSColumn("date_column")
    FSDocStoreGetApiExtensionTestTableSetter dateColumn(Date dateColumn);

    /**
     * <p>
     *   Set the value of the deleted column to be updated
     * </p>
     */
    @FSColumn("deleted")
    FSDocStoreGetApiExtensionTestTableSetter deleted(boolean deleted);

    /**
     * <p>
     *   Set the value of the double_column column to be updated
     * </p>
     */
    @FSColumn("double_column")
    FSDocStoreGetApiExtensionTestTableSetter doubleColumn(double doubleColumn);

    /**
     * <p>
     *   Set the value of the double_wrapper_column column to be updated
     * </p>
     */
    @FSColumn("double_wrapper_column")
    FSDocStoreGetApiExtensionTestTableSetter doubleWrapperColumn(Double doubleWrapperColumn);

    /**
     * <p>
     *   Set the value of the int_column column to be updated
     * </p>
     */
    @FSColumn("int_column")
    FSDocStoreGetApiExtensionTestTableSetter intColumn(int intColumn);

    /**
     * <p>
     *   Set the value of the integer_wrapper_column column to be updated
     * </p>
     */
    @FSColumn("integer_wrapper_column")
    FSDocStoreGetApiExtensionTestTableSetter integerWrapperColumn(Integer integerWrapperColumn);

    /**
     * <p>
     *   Set the value of the long_column column to be updated
     * </p>
     */
    @FSColumn("long_column")
    FSDocStoreGetApiExtensionTestTableSetter longColumn(long longColumn);

    /**
     * <p>
     *   Set the value of the long_wrapper_column column to be updated
     * </p>
     */
    @FSColumn("long_wrapper_column")
    FSDocStoreGetApiExtensionTestTableSetter longWrapperColumn(Long longWrapperColumn);

    /**
     * <p>
     *   Set the value of the string_column column to be updated
     * </p>
     */
    @FSColumn("string_column")
    FSDocStoreGetApiExtensionTestTableSetter stringColumn(String stringColumn);
}
