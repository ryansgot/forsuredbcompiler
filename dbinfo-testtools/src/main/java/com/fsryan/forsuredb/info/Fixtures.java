package com.fsryan.forsuredb.info;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    public static String tableFQClassName(@Nonnull String tableName) {
        return tableName + ".qclass.name";
    }

    public static TableInfo.BuilderCompat tableBuilder(String tableName, ColumnInfo... columns) {
        TableInfo.BuilderCompat ret = TableInfo.builder()
                .tableName(tableName)
                .qualifiedClassName(tableFQClassName(tableName));
        if (columns != null && columns.length > 0) {
            Arrays.stream(columns).forEach(ret::addToColumns);
        }
        return ret;
    }

    public static TableInfo.BuilderCompat tableBuilderWithDefaultColumns(String name, ColumnInfo... columns) {
        TableInfo.BuilderCompat ret = tableBuilder(name, columns);
        TableInfo.defaultColumns().values().forEach(ret::addToColumns);
        return ret;
    }

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
}
