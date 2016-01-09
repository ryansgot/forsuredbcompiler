package com.forsuredb.annotationprocessor.generator.code;

import com.forsuredb.annotationprocessor.util.APLog;

import java.lang.reflect.Type;

// TODO: remove the need for this class by updating the ColumnInfo model to retain TypeMirror
public class TypeUtil {

    private static final String LOG_TAG = TypeUtil.class.getSimpleName();

    public static Object exampleValue(String qualifiedType) {
        switch (qualifiedType) {
            case "char":
                return 'a';
            case "byte":
                return "(byte) 35";
            case "byte[]":
                return "new byte[] {(byte) 35, (byte) 36}";
            case "boolean":
                return true;
            case "short":
                return 3;
            case "int":
                return 65536;
            case "long":
                return "23545494583L";
            case "float":
                return "74.5F";
            case "double":
                return "75.5D";
            case "java.util.Date":
                return "new Date()";
            case "java.math.BigDecimal":
                return "BigDecimal.ONE";
        }

        throw new IllegalStateException("Unsupported type: " + qualifiedType);
    }

    public static Type fromFQTypeName(String fqTypeName) {
        switch (fqTypeName) {
            case "char":
                return char.class;
            case "byte":
                return byte.class;
            case "byte[]":
                return byte[].class;
            case "boolean":
                return boolean.class;
            case "short":
                return short.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            default:
                try {
                    return Class.forName(fqTypeName);
                } catch (ClassNotFoundException cnfe) {
                    APLog.e(LOG_TAG, "could not find type for class: " + fqTypeName);
                }
        }

        throw new IllegalStateException("could not find type for class: " + fqTypeName);
    }
}
