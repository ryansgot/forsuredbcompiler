package com.forsuredb.annotationprocessor.generator.code;

import com.forsuredb.annotationprocessor.util.APLog;
import com.google.common.base.Joiner;

import java.lang.reflect.Type;

/*package*/ class CodeUtil {

    private static final String LOG_TAG = CodeUtil.class.getSimpleName();

    public static String simpleClassNameFrom(String fqClassName) {
        if (fqClassName == null) {
            return null;
        } else if (fqClassName.isEmpty()) {
            return "";
        }

        final String[] split = fqClassName.split("\\.");
        return split[split.length - 1];
    }

    public static String packageNameFrom(String fqClassName) {
        if (fqClassName == null) {
            return null;
        } else if (fqClassName.isEmpty()) {
            return "";
        }

        String[] split = fqClassName.split("\\.");
        if (split.length == 1) {
            return "";
        }

        StringBuilder ret = new StringBuilder(split[0]);
        for (int i = 1; i < split.length - 1; i++) {
            ret.append(".").append(split[i]);
        }
        return ret.toString();
    }

    public static String snakeToCamel(String snakeCaseString) {
        return snakeToCamel(snakeCaseString, false);
    }

    public static String snakeToCamel(String snakeCaseString, boolean firstCharToUpper) {
        if (snakeCaseString == null) {
            return null;
        } else if (snakeCaseString.isEmpty()) {
            return "";
        }

        String[] parts = snakeCaseString.split("_");

        if (firstCharToUpper) {
            parts[0] = parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1, parts[0].length());
        } else {
            parts[0] = parts[0].substring(0, 1).toLowerCase() + parts[0].substring(1, parts[0].length());
        }

        for (int i = 1; i < parts.length; i++) {
            parts[i] = parts[i].substring(0, 1).toUpperCase() + parts[i].substring(1, parts[i].length());
        }

        return Joiner.on("").join(parts);
    }

    // TODO: remove the need for this method by updating the ColumnInfo model to retain TypeMirror
    public static Object javaExampleOf(String qualifiedType) {
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

    // TODO: remove the need for this method by updating the ColumnInfo model to retain TypeMirror
    public static Type typeFromName(String fqTypeName) {
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
