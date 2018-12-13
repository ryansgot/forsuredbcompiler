package com.fsryan.forsuredb.sqlitelib.diff;

import javax.annotation.Nonnull;

abstract class TypeUtil {

    @Nonnull
    static String sqlTypeOf(@Nonnull String fqType) {
        switch (fqType) {
            case "java.math.BigDecimal":
            case "java.math.BigInteger":
            case "java.lang.String":
                return "TEXT";
            case "boolean":
            case "int":
            case "long":
            case "java.lang.Boolean":
            case "java.lang.Integer":
            case "java.lang.Long":
                return "INTEGER";
            case "double":
            case "float":
            case "java.lang.Double":
            case "java.lang.Float":
                return "REAL";
            case "java.util.Date":
                return "DATETIME";
            case "byte[]":
                return "BLOB";
        }
        throw new IllegalArgumentException("Unsupported type: " + fqType);
    }
}
