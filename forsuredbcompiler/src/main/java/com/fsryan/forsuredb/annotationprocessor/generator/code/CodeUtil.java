package com.fsryan.forsuredb.annotationprocessor.generator.code;

import com.fsryan.forsuredb.annotationprocessor.util.APLog;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

import java.lang.reflect.Type;
import java.util.Map;

/*package*/ public class CodeUtil {

    private static final Map<String, String> primitiveToWrapperNameMap = new ImmutableMap.Builder<String, String>()
            .put(char.class.getName(), Character.class.getSimpleName())
            .put(byte.class.getName(), Byte.class.getSimpleName())
            .put(boolean.class.getName(), Boolean.class.getSimpleName())
            .put(short.class.getName(), Short.class.getSimpleName())
            .put(int.class.getName(), Integer.class.getSimpleName())
            .put(long.class.getName(), Long.class.getSimpleName())
            .put(float.class.getName(), Float.class.getSimpleName())
            .put(double.class.getName(), Double.class.getSimpleName())
            .build();

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

        int firstNonEmptyIndex = 0;
        while (firstNonEmptyIndex < parts.length && parts[firstNonEmptyIndex].isEmpty()) {
            firstNonEmptyIndex++;
        }

        if (firstNonEmptyIndex == parts.length) {
            return "";
        }

        if (firstCharToUpper) {
            parts[firstNonEmptyIndex] = parts[firstNonEmptyIndex].substring(0, 1).toUpperCase()
                    + parts[firstNonEmptyIndex].substring(1, parts[firstNonEmptyIndex].length());
        } else {
            parts[firstNonEmptyIndex] = parts[firstNonEmptyIndex].substring(0, 1).toLowerCase()
                    + parts[firstNonEmptyIndex].substring(1, parts[firstNonEmptyIndex].length());
        }

        for (int i = firstNonEmptyIndex + 1; i < parts.length; i++) {
            if (parts[i].isEmpty()) {
                continue;
            }
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
            case "java.lang.String":
                return "A String";
        }

        throw new IllegalStateException("Unsupported type: " + qualifiedType);
    }

    public static Type arrayTypeFromName(String fqTypeName) {
        return typeFromName(fqTypeName, true);
    }

    // TODO: remove the need for this method by updating the ColumnInfo model to retain TypeMirror
    public static Type typeFromName(String fqTypeName) {
        return typeFromName(fqTypeName, false);
    }

    private static Type typeFromName(String fqTypeName, boolean array) {
        switch (fqTypeName) {
            case "char":
                return array ? char[].class : char.class;
            case "byte":
                return array ? byte[].class : byte.class;
            case "byte[]":
                return byte[].class;
            case "boolean":
                return array ? boolean[].class : boolean.class;
            case "short":
                return array ? short[].class : short.class;
            case "int":
                return array ? int[].class : int.class;
            case "long":
                return array ? long[].class : long.class;
            case "float":
                return array ? float[].class : float.class;
            case "double":
                return array ? double[].class : double.class;
        }

        try {
            return array ? Class.forName("[L" + fqTypeName + ";") : Class.forName(fqTypeName);
        } catch (ClassNotFoundException cnfe) {
            APLog.e(LOG_TAG, "could not find type for class: " + fqTypeName);
        }

        throw new IllegalStateException("could not find type for class: " + fqTypeName);
    }

    public static String primitiveToWrapperName(String fqTypeName) {
        String name = primitiveToWrapperNameMap.get(fqTypeName);
        return name == null ? simpleClassNameFrom(fqTypeName) : name;
    }
}
