package com.fsryan.forsuredb.util;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import java.lang.annotation.Annotation;
import java.util.List;

@AutoValue
public abstract class TestDeclaredType implements DeclaredType {

    @AutoValue.Builder
    public static abstract class Builder {
        public abstract Builder setEnclosingType(@Nullable TypeMirror typeMirror);
        public abstract Builder setTypeArguments(@Nullable List<TypeMirror> typeArguments);
        public abstract Builder setKind(TypeKind typeKind);
        public abstract Builder setAnnotationMirrors(@Nullable List<AnnotationMirror> annotationMirrors);
        public abstract Builder setFakeAnnotations(@Nullable List<Annotation> fakeAnnotations);
        public abstract Builder setFakeClassName(String fakeClassName);
        public Builder setFakeClass(Class cls) {
            return setFakeClassName(cls.getName());
        }
        public abstract TestDeclaredType build();
    }

    public static DeclaredType string() {
        return of(String.class);
    }

    public static DeclaredType of(Class cls) {
        return builder()
                .setFakeClass(cls)
                .setKind(TypeKind.DECLARED)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TestDeclaredType.Builder();
    }

    @Nullable public abstract TypeMirror getEnclosingType();
    @Nullable public abstract List<TypeMirror> getTypeArguments();
    public abstract TypeKind getKind();
    @Nullable public abstract List<AnnotationMirror> getAnnotationMirrors();
    @Nullable public abstract List<Annotation> getFakeAnnotations();
    public abstract String getFakeClassName();
    public abstract Builder toBuilder();

    @Override
    public <R, P> R accept(TypeVisitor<R, P> v, P p) {
        throw new UnsupportedOperationException("TestDeclaredType does not accept visitors");
    }
    @Override
    public Element asElement() {
        throw new UnsupportedOperationException("TestDeclaredType does not support asElement");
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return AnnotatedConstructUtil.getAnnotation(annotationType, getFakeAnnotations());
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        return AnnotatedConstructUtil.getAnnotationsByType(annotationType, getFakeAnnotations());
    }

    @Override
    public String toString() {
        return getFakeClassName();
    }
}
