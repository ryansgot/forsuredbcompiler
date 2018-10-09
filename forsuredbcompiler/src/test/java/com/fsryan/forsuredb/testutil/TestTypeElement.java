package com.fsryan.forsuredb.testutil;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@AutoValue
public abstract class TestTypeElement implements TypeElement {

    @AutoValue.Builder
    public static abstract class Builder {
        public abstract Builder setEnclosedElements(@Nullable List<Element> enclosedElements);
        public abstract Builder setAnnotationMirrors(@Nullable List<AnnotationMirror> annotationMirrors);
        public abstract Builder setNestingKind(@Nullable NestingKind nestingKind);
        public abstract Builder setQualifiedName(@Nullable Name qualifiedName);
        public abstract Builder setFakeType(@Nullable TypeMirror typeMirror);
        public abstract Builder setKind(@Nullable ElementKind elementKind);
        public abstract Builder setModifiers(@Nullable Set<Modifier> modifiers);
        public abstract Builder setSimpleName(@Nullable Name simpleName);
        public abstract Builder setSuperclass(@Nullable TypeMirror superclass);
        public abstract Builder setInterfaces(@Nullable List<TypeMirror> interfaces);
        public abstract Builder setTypeParameters(@Nullable List<TypeParameterElement> typeParameters);
        public abstract Builder setEnclosingElement(@Nullable Element enclosingElement);
        public abstract Builder setFakeAnnotations(@Nullable List<Annotation> fakeAnnotations);
        public abstract TestTypeElement build();
    }

    public static Builder withRawDocStoreInterface(String name) {
        return withDocStoreInterface(name, null);
    }

    public static Builder withDocStoreInterface(String name, String parameterization) {
        List<TypeMirror> typeArguments = parameterization == null
                ? Collections.emptyList()
                : Collections.singletonList(TestTypeMirror.forName(parameterization));
        return builder()
                .setSimpleName(TestNameUtil.createReal(name))
                .setInterfaces(Collections.singletonList(TestTypeMirror.OfDeclaredType.create(
                        FSTestTypesUtil.docStoreGetApi(),
                        TestDeclaredType.builder()
                                .setKind(TypeKind.DECLARED)
                                .setFakeClass(String.class)
                                .setTypeArguments(typeArguments)
                                .build()
                )));
    }

    public static Builder builder() {
        return new AutoValue_TestTypeElement.Builder();
    }

    @Nullable public abstract List<Element> getEnclosedElements();
    @Nullable public abstract List<AnnotationMirror> getAnnotationMirrors();
    @Nullable public abstract NestingKind getNestingKind();
    @Nullable public abstract Name getQualifiedName();
    @Nullable public abstract TypeMirror getFakeType();
    @Nullable public abstract ElementKind getKind();
    @Nullable public abstract Set<Modifier> getModifiers();
    @Nullable public abstract Name getSimpleName();
    @Nullable public abstract TypeMirror getSuperclass();
    @Nullable public abstract List<TypeMirror> getInterfaces();
    @Nullable public abstract List<TypeParameterElement> getTypeParameters();
    @Nullable public abstract Element getEnclosingElement();
    @Nullable public abstract List<Annotation> getFakeAnnotations();

    @Override
    public TypeMirror asType() {
        return getFakeType();
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
    public <R, P> R accept(ElementVisitor<R, P> v, P p) {
        throw new UnsupportedOperationException("TestTypeElement does not accept visitors");
    }
}
