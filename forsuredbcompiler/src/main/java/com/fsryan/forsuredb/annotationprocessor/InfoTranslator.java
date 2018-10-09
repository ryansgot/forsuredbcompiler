package com.fsryan.forsuredb.annotationprocessor;

import com.fsryan.forsuredb.annotationprocessor.util.APLog;
import com.fsryan.forsuredb.annotationprocessor.util.AnnotationTranslatorFactory;
import com.fsryan.forsuredb.annotations.*;
import com.fsryan.forsuredb.api.FSDocStoreGetApi;
import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.TableInfo;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * <p>Transform from various {@link javax.lang.model.element.Element Element}
 * instances into the info objects used to describe the
 */
class InfoTranslator {

    static TableInfo.BuilderCompat toTableInfoBuilder(TypeElement te) {
        return TableInfo.builder()
                .tableName(tableNameOf(te))
                .qualifiedClassName(te.getQualifiedName().toString())
                .docStoreParameterization(docStoreParameterizationOf(te))
                .primaryKey(primaryKeyFrom(te))
                .primaryKeyOnConflict(primaryKeyOnConflictFrom(te))
                .staticDataAsset(staticDataAssetOf(te));
    }

    static String docStoreParameterizationOf(TypeElement intf) {
        TypeMirror docStoreMirror = intf.getInterfaces().stream()
                .filter(typeMirror -> typeMirror.toString().startsWith(FSDocStoreGetApi.class.getName()))
                .findFirst().orElse(null);
        if (docStoreMirror == null) {
            return null;
        }
        return Stream.of(docStoreMirror)
                .map(typeMirror -> (DeclaredType) typeMirror)
                .map(declaredType -> {
                    List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
                    return typeArguments == null || typeArguments.isEmpty()
                            ? Object.class.getSimpleName()
                            : typeArguments.get(0).toString();
                })
                .findFirst()
                .orElse(null);
    }

    static Set<String> primaryKeyFrom(TypeElement intf) {
        FSPrimaryKey primaryKey = intf.getAnnotation(FSPrimaryKey.class);
        return primaryKey == null || primaryKey.value().length == 0   // <-- do not allow user to specify no primary key
                ? Collections.singleton(TableInfo.DEFAULT_PRIMARY_KEY_COLUMN)
                : new HashSet<>(Arrays.asList(primaryKey.value()));
    }

    static String primaryKeyOnConflictFrom(TypeElement intf) {
        FSPrimaryKey primaryKey = intf.getAnnotation(FSPrimaryKey.class);
        return primaryKey == null ? "" : primaryKey.onConflict();
    }

    static String staticDataAssetOf(TypeElement intf) {
        FSStaticData staticData = intf.getAnnotation(FSStaticData.class);
        return staticData == null ? null : staticData.value();
    }

    static String tableNameOf(TypeElement intf) {
        FSTable table = intf.getAnnotation(FSTable.class);
        return table == null ? intf.getSimpleName().toString() : table.value();
    }

    static ColumnInfo.Builder toColumnInfoBuilder(ExecutableElement ee) {
        throwIfDefaultInvalid(ee);

        ColumnInfo.Builder builder = defaultsFromElement(ee);
        ee.getAnnotationMirrors().forEach(am -> appendAnnotationInfo(builder, am, ee.getReturnType()));
        return builder;
    }

    static ColumnInfo.Builder defaultsFromElement(ExecutableElement ee) {
        ColumnInfo.Builder ret = ColumnInfo.builder()
                .methodName(ee.getSimpleName().toString())
                .qualifiedType(ee.getReturnType().toString());

        // Being a primitive, a null value would cause an NPE when deserializing
        if (ee.getReturnType().getKind().isPrimitive()) {
            FSDefault fsDefault = ee.getAnnotation(FSDefault.class);
            if (fsDefault == null) {
                APLog.w("InfoTranslator", "default value of \"0\" implied for: " + ee.getSimpleName());
                ret.defaultValue("0");
            }
        }
        return ret;
    }

    static String columnNameOf(ExecutableElement ee) {
        for (AnnotationMirror annotationMirror : ee.getAnnotationMirrors()) {
            if (annotationMirror.getAnnotationType().toString().equals(FSColumn.class.getName())) {
                return AnnotationTranslatorFactory.inst()
                        .create(annotationMirror)
                        .property("value")
                        .asString();
            }
        }
        return ee.getSimpleName().toString();
    }

    static void validateForeignKeyDeclaration(ExecutableElement ee) {
        if (ee.getAnnotation(FSForeignKey.class) != null && ee.getAnnotation(ForeignKey.class) != null) {
            throw new IllegalStateException(FSForeignKey.class + " is incompatible with " + ForeignKey.class + ". " + ForeignKey.class + " is deprecated. Use " + FSForeignKey.class + ".");
        }
    }

    static boolean containsForeignKey(ExecutableElement ee) {
        FSForeignKey fsfk = ee.getAnnotation(FSForeignKey.class);
        ForeignKey legacyForeignKey = ee.getAnnotation(ForeignKey.class);
        return fsfk != null || legacyForeignKey != null;
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
            builder.defaultValue(ensureCorrectDefaultValue(at.property("value").asString(), returnType));
        }
    }

    private static String ensureCorrectDefaultValue(String value, TypeMirror returnType) {
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

    private static void throwIfDefaultInvalid(ExecutableElement ee) {
        FSDefault fsDefault = ee.getAnnotation(FSDefault.class);
        if (fsDefault == null) {
            return;
        }

        TypeMirror returnType = ee.getReturnType();
        try {
            switch (returnType.toString()) {
                case "boolean":
                case "java.lang.Boolean":
                    throwIfBooleanDefaultInvalid(ee, fsDefault.value());
                    break;
                case "byte":
                case "java.lang.Byte":
                    Byte.parseByte(fsDefault.value());
                    break;
                case "int":
                case "java.lang.Integer":
                    Integer.parseInt(fsDefault.value());
                    break;
                case "long":
                case "java.lang.Long":
                    Long.parseLong(fsDefault.value());
                    break;
                case "short":
                case "java.lang.Short":
                    Short.parseShort(fsDefault.value());
                    break;
                case "java.math.BigInteger":
                    new BigInteger(fsDefault.value());
                    break;
                case "float":
                case "java.lang.Float":
                    float parsedFloat = Float.parseFloat(fsDefault.value());
                    if (parsedFloat == Float.NEGATIVE_INFINITY || parsedFloat == Float.POSITIVE_INFINITY) {
                        throwInvalidDefault(ee, fsDefault.value());
                    }
                    break;
                case "double":
                case "java.lang.Double":
                    double parsedDouble = Double.parseDouble(fsDefault.value());
                    if (parsedDouble == Double.NEGATIVE_INFINITY || parsedDouble == Double.POSITIVE_INFINITY) {
                        throwInvalidDefault(ee, fsDefault.value());
                    }
                    break;
                case "java.math.BigDecimal":
                    new BigDecimal(fsDefault.value());
                    break;
                case "char":
                case "java.lang.Character":
                    throw new IllegalArgumentException("type Character not supported");
            }
        } catch (NumberFormatException nfe) {
            throwInvalidDefault(ee, fsDefault.value(), nfe);
        }

        if (returnType.getKind() == TypeKind.ARRAY) {
            if (!returnType.toString().equals(byte[].class.getCanonicalName())) {
                throw new IllegalArgumentException("Array type not supported: " + returnType);
            }
            if (!Pattern.compile("([0-9a-fA-f]{2})+").matcher(fsDefault.value()).matches()) {
                throwInvalidDefault(ee, fsDefault.value());
            }
        }
    }

    private static void throwIfBooleanDefaultInvalid(ExecutableElement ee, String defaultValue) {
        if (Boolean.TRUE.toString().equalsIgnoreCase(defaultValue)
                || Boolean.FALSE.toString().equalsIgnoreCase(defaultValue)
                || String.valueOf(0).equals(defaultValue)
                || String.valueOf(1).equals(defaultValue)) {
            return;
        }
        throwInvalidDefault(ee, defaultValue);
    }

    private static void throwInvalidDefault(ExecutableElement ee, String defaultValue) {
        throwInvalidDefault(ee, defaultValue, null);
    }

    private static void throwInvalidDefault(ExecutableElement ee, String defaultValue, Exception cause) {
        throw new RuntimeException(
                String.format("%s value for %s invalid default: %s", ee.getReturnType(), ee.getSimpleName(), defaultValue),
                cause
        );
    }
}
