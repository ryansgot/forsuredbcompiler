package com.fsryan.forsuredb.util;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@AutoValue
public abstract class TestTypeMirror implements TypeMirror {

    @AutoValue.Builder
    public static abstract class Builder {
        public abstract Builder setFakeName(String name);
        public abstract Builder setKind(TypeKind typeKind);
        public abstract Builder setAnnotationMirrors(@Nullable List<? extends AnnotationMirror> annotationMirrors);
        public abstract Builder setFakedAnnotations(@Nullable List<Annotation> annotations);
        public abstract TestTypeMirror build();
    }

    public static TestTypeMirror primitiveBoolean() {
        return withKindAndClass(TypeKind.BOOLEAN, boolean.class);
    }

    public static TestTypeMirror booleanWrapper() {
        return withKindAndClass(TypeKind.DECLARED, Boolean.class);
    }

    public static TestTypeMirror primitiveByte() {
        return withKindAndClass(TypeKind.BYTE, byte.class);
    }

    public static TestTypeMirror byteWrapper() {
        return withKindAndClass(TypeKind.DECLARED, Byte.class);
    }

    public static TestTypeMirror primitiveChar() {
        return withKindAndClass(TypeKind.CHAR, char.class);
    }

    public static TestTypeMirror charWrapper() {
        return withKindAndClass(TypeKind.DECLARED, Character.class);
    }

    public static TestTypeMirror primitiveDouble() {
        return withKindAndClass(TypeKind.DOUBLE, double.class);
    }

    public static TestTypeMirror doubleWrapper() {
        return withKindAndClass(TypeKind.DECLARED, Double.class);
    }

    public static TestTypeMirror primitiveFloat() {
        return withKindAndClass(TypeKind.FLOAT, float.class);
    }

    public static TestTypeMirror floatWrapper() {
        return withKindAndClass(TypeKind.DECLARED, Float.class);
    }

    public static TestTypeMirror primitiveInt() {
        return withKindAndClass(TypeKind.INT, int.class);
    }

    public static TestTypeMirror intWrapper() {
        return withKindAndClass(TypeKind.DECLARED, Integer.class);
    }

    public static TestTypeMirror primitiveLong() {
        return withKindAndClass(TypeKind.LONG, long.class);
    }

    public static TestTypeMirror longWrapper() {
        return withKindAndClass(TypeKind.DECLARED, Long.class);
    }

    public static TestTypeMirror primitiveShort() {
        return withKindAndClass(TypeKind.SHORT, short.class);
    }

    public static TestTypeMirror shortWrapper() {
        return withKindAndClass(TypeKind.DECLARED, Short.class);
    }

    public static TestTypeMirror byteArray() {
        return withKindAndClass(TypeKind.ARRAY, byte[].class);
    }

    public static TypeMirror string() {
        return withKindAndClass(TypeKind.DECLARED, String.class);
    }

    public static TestTypeMirror bigInteger() {
        return withKindAndClass(TypeKind.DECLARED, BigInteger.class);
    }

    public static TestTypeMirror bigDecimal() {
        return withKindAndClass(TypeKind.DECLARED, BigDecimal.class);
    }

    public static Builder builder() {
        return new AutoValue_TestTypeMirror.Builder();
    }

    public abstract String getFakeName();
    public abstract TypeKind getKind();
    @Nullable public abstract List<? extends AnnotationMirror> getAnnotationMirrors();
    @Nullable public abstract List<Annotation> getFakedAnnotations();
    public abstract Builder toBuilder();

    @Override
    public <R, P> R accept(TypeVisitor<R, P> v, P p) {
        throw new UnsupportedOperationException("accept not implemented on TestTypeMirror");
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return AnnotatedConstructUtil.getAnnotation(annotationType, getFakedAnnotations());
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        return AnnotatedConstructUtil.getAnnotationsByType(annotationType, getFakedAnnotations());
    }

    @Override
    public String toString() {
        return getFakeName();
    }

    private static TestTypeMirror withKindAndClass(TypeKind typeKind, Class cls) {
        return builder()
                .setFakeName(cls.getCanonicalName())
                .setKind(typeKind)
                .build();
    }
}
