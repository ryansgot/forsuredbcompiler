package com.fsryan.forsuredb.annotationprocessor.generator.code;

import com.fsryan.forsuredb.annotationprocessor.util.APLog;
import com.fsryan.forsuredb.info.ColumnInfo;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.lang.reflect.Type;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;

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

    public static TypeName typeNameOf(ColumnInfo column) {
        return isPrimitive(column)
                ? primitiveTypeOf(column)
                : isByteArrayColumn(column)
                ? ArrayTypeName.of(byte.class)
                : ClassName.bestGuess(column.qualifiedType());
    }

    public static boolean isByteArrayColumn(ColumnInfo column) {
        return column.qualifiedType().equals("byte[]");
    }

    public static TypeName primitiveTypeOf(ColumnInfo column) {
        return primitiveTypeOf(column.qualifiedType());
    }

    public static TypeName primitiveTypeOf(String qualifiedType) {
        switch (qualifiedType) {
            case "byte": return TypeName.BYTE;
            case "boolean": return TypeName.BOOLEAN;
            case "short": return TypeName.SHORT;
            case "int": return TypeName.INT;
            case "long": return TypeName.LONG;
            case "double": return TypeName.DOUBLE;
            case "float": return TypeName.FLOAT;
            case "char": return TypeName.CHAR;
            default:
                throw new IllegalArgumentException(qualifiedType + " is not a primitive type");
        }
    }

    public static boolean isPrimitive(ColumnInfo columnInfo) {
        return isPrimitive(columnInfo.qualifiedType());
    }

    public static boolean isPrimitive(String qualifiedType) {
        switch (qualifiedType) {
            case "byte":    // intentionally fall through
            case "boolean":    // intentionally fall through
            case "short":    // intentionally fall through
            case "int":    // intentionally fall through
            case "long":    // intentionally fall through
            case "double":    // intentionally fall through
            case "float":    // intentionally fall through
            case "char":
                return true;
            default:
                return false;
        }
    }

    public static String simpleClassNameFrom(String fqClassName) {
        if (fqClassName == null || fqClassName.isEmpty()) {
            return Object.class.getSimpleName();
        }

        final String[] split = fqClassName.split("\\.");
        return split[split.length - 1];
    }

    public static String packageNameFrom(String fqClassName) {
        if (fqClassName == null || fqClassName.isEmpty()) {
            fqClassName = Object.class.getName();
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
            case "java.lang.Character":
                return 'a';
            case "byte":
            case "java.lang.Byte":
                return "(byte) 35";
            case "byte[]":
                return "new byte[] {(byte) 35, (byte) 36}";
            case "boolean":
            case "java.lang.Boolean":
                return true;
            case "short":
            case "java.lang.Short":
                return 3;
            case "int":
            case "java.lang.Integer":
                return 65536;
            case "long":
            case "java.lang.Long":
                return "23545494583L";
            case "float":
            case "java.lang.Float":
                return "74.5F";
            case "double":
            case "java.lang.Double":
                return "75.5D";
            case "java.util.Date":
                return "new Date()";
            case "java.math.BigDecimal":
                return "BigDecimal.ONE";
            case "java.math.BigInteger":
                return "BigInteger.ONE";
            case "java.lang.String":
                return "A String";
        }

        throw new IllegalStateException("Unsupported type: " + qualifiedType);
    }

    public static Type arrayTypeFromName(String fqTypeName) {
        return typeFromName(fqTypeName, true);
    }

    public static Type typeFromName(String fqTypeName) {
        return typeFromName(fqTypeName, false);
    }

    public static Type typeFromName(String fqTypeName, boolean array) {
        if (isNullOrEmpty(fqTypeName)) {
            return array ? Object[].class : Object.class;
        }
        switch (fqTypeName) {
            case "char":
                return array ? char[].class : char.class;
            case "byte":
                return array ? byte[].class : byte.class;
            case "byte[]":
                return array ? byte[][].class : byte[].class;
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

        return array ? Object[].class : Object.class;
    }

    public static String primitiveToWrapperName(String fqTypeName) {
        String name = primitiveToWrapperNameMap.get(fqTypeName);
        return name == null ? simpleClassNameFrom(fqTypeName) : name;
    }
}
