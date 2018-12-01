package com.fsryan.forsuredb.info;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ColumnInfoUtil {

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

    public static String colNameByType(Class cls) {
        String ret = defaultColNameMap.get(cls);
        if (ret == null) {
            throw new IllegalArgumentException("No column for qualified type: " + cls);
        }
        return ret;
    }

    public static String colMethodNameByType(Class cls) {
        String ret = defaultMethodNameMap.get(cls);
        if (ret == null) {
            throw new IllegalArgumentException("No column method name for qualified type: " + cls);
        }
        return ret;
    }

    public static ColumnInfo findDefaultColumn(String colMethodName) {
        ColumnInfo ret = TableInfo.defaultColumns().get(colMethodName);
        if (ret == null) {
            throw new IllegalStateException("could not find column with method name '" + colMethodName + "' on TableInfo.defaultColumns()");
        }
        return ret;
    }

    public static ColumnInfo idCol() {
        return findDefaultColumn("id");
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
}
