package com.fsryan.forsuredb.api.adapter;

import com.fsryan.forsuredb.api.FSSaveApi;
import com.fsryan.forsuredb.api.sqlgeneration.Sql;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.fsryan.forsuredb.api.CollectionUtil.mapOf;

/*package*/ class SharedData {

    public static final Date DATE = new Date();
    public static final String DATE_STRING = Sql.generator().formatDate(DATE);

    public static final Map<String, String> METHOD_NAME_TO_COLUMN_NAME_MAP = mapOf(
            "id", "_id",
            "bigIntegerColumn", "big_integer_column",
            "bigDecimalColumn", "big_decimal_column",
            "booleanColumn", "boolean_column",
            "booleanWrapperColumn", "boolean_wrapper_column",
            "byteArrayColumn", "byte_array_column",
            "dateColumn", "date_column",
            "deleted", "deleted",
            "doubleColumn", "double_column",
            "doubleWrapperColumn", "double_wrapper_column",
            "intColumn", "int_column",
            "integerWrapperColumn", "integer_wrapper_column",
            "longColumn", "long_column",
            "longWrapperColumn", "long_wrapper_column",
            "stringColumn", "string_column",
            "floatColumn", "float_column",
            "floatWrapperColumn", "float_wrapper_column"
    );

    public static final Map<Method, ColumnDescriptor> columnTypeMap(Class<? extends FSSaveApi<?>> saveApiClass) {
        Map<Method, ColumnDescriptor> ret = new HashMap<>();
        Map<String, String> methodNameToColumnNameMap = METHOD_NAME_TO_COLUMN_NAME_MAP;
        if (methodNameToColumnNameMap != null) {
            for (Method m : saveApiClass.getDeclaredMethods()) {
                String columnName = methodNameToColumnNameMap.get(m.getName());
                if (columnName == null) {
                    continue;
                }
                ret.put(m, new ColumnDescriptor(columnName, m.getGenericParameterTypes()[0]));
            }
        }
        return ret;
    }
}
