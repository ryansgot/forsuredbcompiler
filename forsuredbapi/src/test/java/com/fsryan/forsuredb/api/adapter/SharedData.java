package com.fsryan.forsuredb.api.adapter;

import com.fsryan.forsuredb.api.FSSaveApi;
import com.fsryan.forsuredb.api.sqlgeneration.Sql;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*package*/ class SharedData {

    public static final Date DATE = new Date();
    public static final String DATE_STRING = Sql.generator().formatDate(DATE);

    public static final BiMap<String, String> COLUMN_NAME_TO_METHOD_NAME_BI_MAP = new ImmutableBiMap.Builder<String, String>()
            .put("_id", "id")
            .put("big_decimal_column", "bigDecimalColumn")
            .put("boolean_column", "booleanColumn")
            .put("boolean_wrapper_column", "booleanWrapperColumn")
            .put("byte_array_column", "byteArrayColumn")
            .put("date_column", "dateColumn")
            .put("deleted", "deleted")
            .put("double_column", "doubleColumn")
            .put("double_wrapper_column", "doubleWrapperColumn")
            .put("int_column", "intColumn")
            .put("integer_wrapper_column", "integerWrapperColumn")
            .put("long_column", "longColumn")
            .put("long_wrapper_column", "longWrapperColumn")
            .put("string_column", "stringColumn")
            .build();

    public static final Map<Method, ColumnDescriptor> columnTypeMap(Class<? extends FSSaveApi<?>> saveApiClass) {
        Map<Method, ColumnDescriptor> ret = new HashMap<>();
        Map<String, String> methodNameToColumnNameMap = COLUMN_NAME_TO_METHOD_NAME_BI_MAP.inverse();
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
