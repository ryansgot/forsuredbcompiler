package com.fsryan.forsuredb.info;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.fsryan.forsuredb.test.tools.CollectionUtil.setOf;

public abstract class Fixtures {

    private static final Map<Class, String> defaultColNameMap = new HashMap<>();
    private static final Map<Class, String> defaultMethodNameMap = new HashMap<>();
    static {
        defaultColNameMap.put(BigDecimal.class, "big_decimal_col");
        defaultColNameMap.put(BigInteger.class, "big_integer_col");
        defaultColNameMap.put(boolean.class, "boolean_col");
        defaultColNameMap.put(Boolean.class, "boolean_wrapper_col");
        defaultColNameMap.put(Date.class, "date_col");
        defaultColNameMap.put(double.class, "double_col");
        defaultColNameMap.put(Double.class, "double_wrapper_col");
        defaultColNameMap.put(float.class, "float_col");
        defaultColNameMap.put(Float.class, "float_wrapper_col");
        defaultColNameMap.put(int.class, "int_col");
        defaultColNameMap.put(Integer.class, "int_wrapper_col");
        defaultColNameMap.put(long.class, "long_col");
        defaultColNameMap.put(Long.class, "long_wrapper_col");
        defaultColNameMap.put(String.class, "string_col");

        defaultMethodNameMap.put(BigDecimal.class, "bigDecimalCol");
        defaultMethodNameMap.put(BigInteger.class, "bigIntegerCol");
        defaultMethodNameMap.put(boolean.class, "booleanCol");
        defaultMethodNameMap.put(Boolean.class, "booleanWrapperCol");
        defaultMethodNameMap.put(Date.class, "dateCol");
        defaultMethodNameMap.put(double.class, "doubleCol");
        defaultMethodNameMap.put(Double.class, "doubleWrapperCol");
        defaultMethodNameMap.put(float.class, "floatCol");
        defaultMethodNameMap.put(Float.class, "floatWrapperCol");
        defaultMethodNameMap.put(int.class, "intCol");
        defaultMethodNameMap.put(Integer.class, "intWrapperCol");
        defaultMethodNameMap.put(long.class, "longCol");
        defaultMethodNameMap.put(Long.class, "longWrapperCol");
        defaultMethodNameMap.put(String.class, "stringCol");
    }

    /**
     * @param tableName the name of the table
     * @param columns the additional columns ot add
     * @return a {@link TableInfo.Builder} set up with the default columns and
     * all of the columns passed in. The primary key is the default primary
     * key. It uses the table name passed in as well as the default qualified
     * class name.
     * @see TableInfoUtil#tableFQClassName(String)
     * @see TableInfo#DEFAULT_PRIMARY_KEY_COLUMN
     */
    public static TableInfo.Builder tableBuilder(String tableName, ColumnInfo... columns) {
        return TableInfo.builder()
                .tableName(tableName)
                .qualifiedClassName(TableInfoUtil.tableFQClassName(tableName))
                .resetPrimaryKey(setOf(TableInfo.DEFAULT_PRIMARY_KEY_COLUMN))
                .addColumn(idCol())
                .addColumn(createdCol())
                .addColumn(deletedCol())
                .addColumn(modifiedCol())
                .addAllColumns(Arrays.asList(columns));
    }

    // ColumnInfo

    public static ColumnInfo idCol() {
        return findDefaultColumn("_id");
    }

    public static ColumnInfo createdCol() {
        return findDefaultColumn("created");
    }

    public static ColumnInfo deletedCol() {
        return findDefaultColumn("deleted");
    }

    public static ColumnInfo modifiedCol() {
        return findDefaultColumn("modified");
    }

    public static ColumnInfo.Builder booleanCol() {
        return colBuilder(boolean.class);
    }

    public static ColumnInfo.Builder bigDecimalCol() {
        return colBuilder(BigDecimal.class);
    }

    public static ColumnInfo.Builder bigIntegerCol() {
        return colBuilder(BigInteger.class);
    }

    public static ColumnInfo.Builder dateCol() {
        return colBuilder(Date.class);
    }

    public static ColumnInfo.Builder doubleCol() {
        return colBuilder(double.class);
    }

    public static ColumnInfo.Builder intCol() {
        return colBuilder(int.class);
    }

    public static ColumnInfo.Builder longCol() {
        return colBuilder(long.class);
    }

    public static ColumnInfo.Builder stringCol() {
        return colBuilder(String.class);
    }

    /**
     * <p>Returns
     * @param cls
     * @return a {@link ColumnInfo.Builder} of for a column of the type passed
     * in with the default name for that type. The method name is also the
     * default method name for that type.
     * @see #defaultColNameMap
     * @see #defaultMethodNameMap
     */
    public static ColumnInfo.Builder colBuilder(Class cls) {
        return ColumnInfo.builder()
                .columnName(colNameByType(cls))
                .qualifiedType(cls.getName())
                .orderable(true)
                .searchable(true)
                .methodName(colMethodNameByType(cls));
    }

    public static String colNameByType(Class cls) {
        String ret = defaultColNameMap.get(cls);
        if (ret == null) {
            throw new IllegalArgumentException("No column for qualified type: " + cls);
        }
        return ret;
    }

    private static String colMethodNameByType(Class cls) {
        String ret = defaultMethodNameMap.get(cls);
        if (ret == null) {
            throw new IllegalArgumentException("No column method name for qualified type: " + cls);
        }
        return ret;
    }

    private static ColumnInfo findDefaultColumn(String colName) {
        ColumnInfo ret = TableInfo.defaultColumns().get(colName);
        if (ret == null) {
            throw new IllegalStateException("could not find '" + colName + "' column on TableInfo.defaultColumns()");
        }
        return ret;
    }

    // ForeignKeyInfo

    public static ForeignKeyInfo idCascadeFKI(@Nonnull String foreignKeyTableName) {
        return cascadeFKI(foreignKeyTableName, "_id").build();
    }

    public static ForeignKeyInfo.Builder cascadeFKI(@Nonnull String foreignKeyTableName, @Nonnull String foreignColumnName) {
        return ForeignKeyInfo.builder().updateAction("CASCADE")
                .deleteAction("CASCADE")
                .columnName(foreignColumnName)
                .tableName(foreignKeyTableName);
    }

    public static ForeignKeyInfo idNoActionFKI(@Nonnull String foreignKeyTableName) {
        return noActionFKI(foreignKeyTableName, "_id").build();
    }

    public static ForeignKeyInfo.Builder noActionFKI(@Nonnull String foreignKeyTableName, @Nonnull String foreignColumnName) {
        return ForeignKeyInfo.builder().updateAction("NO ACTION")
                .deleteAction("NO ACTION")
                .columnName(foreignColumnName)
                .tableName(foreignKeyTableName);
    }

    public static ForeignKeyInfo idSetNullFKI(@Nonnull String foreignKeyTableName) {
        return setNullFKI(foreignKeyTableName, "_id").build();
    }

    public static ForeignKeyInfo.Builder setNullFKI(@Nonnull String foreignKeyTableName, @Nonnull String foreignColumnName) {
        return ForeignKeyInfo.builder().updateAction("SET NULL")
                .deleteAction("SET NULL")
                .columnName(foreignColumnName)
                .tableName(foreignKeyTableName);
    }

    public static ForeignKeyInfo idSetDefaultFKI(@Nonnull String foreignKeyTableName) {
        return setDefaultFKI(foreignKeyTableName, "_id").build();
    }

    public static ForeignKeyInfo.Builder setDefaultFKI(@Nonnull String foreignKeyTableName, @Nonnull String foreignColumnName) {
        return ForeignKeyInfo.builder().updateAction("SET DEFAULT")
                .deleteAction("SET DEFAULT")
                .columnName(foreignColumnName)
                .tableName(foreignKeyTableName);
    }

    public static ForeignKeyInfo idRestrictFKI(@Nonnull String foreignKeyTableName) {
        return restrictFKI(foreignKeyTableName, "_id").build();
    }

    public static ForeignKeyInfo.Builder restrictFKI(@Nonnull String foreignKeyTableName, @Nonnull String foreignColumnName) {
        return ForeignKeyInfo.builder().updateAction("RESTRICT")
                .deleteAction("RESTRICT")
                .columnName(foreignColumnName)
                .tableName(foreignKeyTableName);
    }
}