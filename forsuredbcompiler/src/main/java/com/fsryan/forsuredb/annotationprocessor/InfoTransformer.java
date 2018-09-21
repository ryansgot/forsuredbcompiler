package com.fsryan.forsuredb.annotationprocessor;

import com.fsryan.forsuredb.annotationprocessor.util.APLog;
import com.fsryan.forsuredb.annotationprocessor.util.AnnotationTranslatorFactory;
import com.fsryan.forsuredb.annotations.*;
import com.fsryan.forsuredb.info.ColumnInfo;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.regex.Pattern;

/**
 * <p>Transform from various {@link javax.lang.model.element.Element Element}
 * instances into the info objects used to describe the
 */
class InfoTransformer {

    static ColumnInfo.Builder transform(ExecutableElement ee) {
        throwIfDefaultInvalid(ee);

        ColumnInfo.Builder builder = defaultsFromElement(ee);
        ee.getAnnotationMirrors().forEach(am -> appendAnnotationInfo(builder, am, ee.getReturnType()));
        return builder;
    }

    private static void throwIfDefaultInvalid(ExecutableElement ee) {
        FSDefault fsDefault = ee.getAnnotation(FSDefault.class);
        if (fsDefault == null) {
            return;
        }

        TypeMirror returnType = ee.getReturnType();
        if (returnType.getKind().isPrimitive()) {
            switch (returnType.getKind()) {
                case BOOLEAN:
                    throwIfBooleanDefaultInvalid(ee.getSimpleName().toString(), fsDefault);
                    break;
                case BYTE:
                case INT:
                case LONG:
                case SHORT:
                    throwIfNumberInvalid(ee.getSimpleName().toString(), fsDefault);
                    break;
                case FLOAT:
                case DOUBLE:
                    throwIfFloatingPointNumberInvalid(ee.getSimpleName().toString(), fsDefault);
                    break;
                case CHAR:
                    throw new IllegalArgumentException("type char not supported");

            }
        }

        if (returnType.getKind() == TypeKind.DECLARED) {
            switch (returnType.toString()) {
                case "java.lang.Boolean":
                    throwIfBooleanDefaultInvalid(ee.getSimpleName().toString(), fsDefault);
                    break;
                case "java.lang.Byte":
                case "java.lang.Integer":
                case "java.lang.Long":
                case "java.lang.Short":
                case "java.math.BigInteger":
                    throwIfNumberInvalid(ee.getSimpleName().toString(), fsDefault);
                    break;
                case "java.lang.Float":
                case "java.lang.Double":
                case "java.math.BigDecimal":
                    throwIfFloatingPointNumberInvalid(ee.getSimpleName().toString(), fsDefault);
                    break;
                case "java.lang.Character":
                    throw new IllegalArgumentException("type Character not supported");
            }
        }

        if (returnType.getKind() == TypeKind.ARRAY) {
            if (!returnType.toString().equals(byte[].class.getCanonicalName())) {
                throw new IllegalArgumentException("Array type not supported: " + returnType);
            }
            if (!Pattern.compile("([0-9a-fA-f]{2})+").matcher(fsDefault.value()).matches()) {
                throw new RuntimeException("invalid byte array hexadecimal (must be even number of hex digits): " + fsDefault.value());
            }
        }
    }

    static ColumnInfo.Builder defaultsFromElement(ExecutableElement ee) {
        ColumnInfo.Builder ret = ColumnInfo.builder()
                .methodName(ee.getSimpleName().toString())
                .qualifiedType(ee.getReturnType().toString());

        // Being a primitive, a null value wouldn't do.
        if (ee.getReturnType().getKind().isPrimitive()) {
            FSDefault fsDefault = ee.getAnnotation(FSDefault.class);
            if (fsDefault == null) {
                APLog.i("RYAN", "adding 0 as default value of column with method name: " + ee.getSimpleName().toString());
                ret.defaultValue("0");
            }
        }
        return ret;
    }

    private static void appendAnnotationInfo(ColumnInfo.Builder builder, AnnotationMirror am, TypeMirror returnType) {
        AnnotationTranslatorFactory.AnnotationTranslator at = AnnotationTranslatorFactory.inst().create(am);
        String annotationClass = am.getAnnotationType().toString();

        if (annotationClass.equals(FSColumn.class.getName())) {
            builder.columnName(at.property("value").as(String.class))
                    .searchable(at.property("searchable").castSafe(true))
                    .orderable(at.property("orderable").castSafe(true))
                    // TODO: error check and return appropriate exception instead of NPE
                    .valueAccess(at.property("documentValueAccess").asListOf(String.class));
        } else if (annotationClass.equals(ForeignKey.class.getName())) {
            APLog.w(ProcessingContext.class.getSimpleName(), "Ignoring legacy " + ForeignKey.class.getSimpleName() + "; this info will be picked up at the table level.");
        } else if (annotationClass.equals(FSPrimaryKey.class.getName())) {
            builder.primaryKey(true);
        } else if (annotationClass.equals(Unique.class.getName())) {
            builder.unique(true);
            if (at.property("index").as(boolean.class)) {
                builder.index(true);
            }
        } else if (annotationClass.equals(Index.class.getName())) {
            builder.index(true);
            if (at.property("unique").as(boolean.class)) {
                builder.unique(true);
            }
        } else if (annotationClass.equals(FSDefault.class.getName())) {
            builder.defaultValue(ensureCorrectDefault(at.property("value").asString(), returnType));
        }
    }

    private static void throwIfBooleanDefaultInvalid(String name, FSDefault fsDefault) {
        if (Boolean.TRUE.toString().equalsIgnoreCase(fsDefault.value())
                || Boolean.FALSE.toString().equalsIgnoreCase(fsDefault.value())
                || String.valueOf(0).equals(fsDefault.value())
                || String.valueOf(1).equals(fsDefault.value())) {
            return;
        }
        throw new RuntimeException("Boolean value for " + name + " invalid default: " + fsDefault.value());
    }

    private static void throwIfNumberInvalid(String name, FSDefault fsDefault) {
        if (Pattern.compile("^([0-9]|[1-9][0-9]*)$").matcher(fsDefault.value()).matches()) {
            return;
        }
        throw new RuntimeException("Decimal number for '" + name + "' invalid default: " + fsDefault.value());
    }

    private static void throwIfFloatingPointNumberInvalid(String name, FSDefault fsDefault) {
        if (Pattern.compile("^(([0-9]?|[1-9][0-9]*)(\\.[0-9]+)?)$").matcher(fsDefault.value()).matches()) {
            return;
        }
        throw new RuntimeException("Decimal number for '" + name + "' invalid default: " + fsDefault.value());
    }

    private static String ensureCorrectDefault(String value, TypeMirror returnType) {
        if (returnType.getKind() == TypeKind.BOOLEAN || Boolean.class.getName().equals(returnType.toString())) {
            if ("true".equalsIgnoreCase(value)) {
                return "1";
            }
            if ("false".equalsIgnoreCase(value)) {
                return "0";
            }
        }
        return value;
    }
}
