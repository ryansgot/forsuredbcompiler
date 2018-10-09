package com.fsryan.forsuredb.testutil;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@AutoValue
public abstract class TestExecutableElement implements ExecutableElement {

    @AutoValue.Builder
    public static abstract class Builder {
        public abstract Builder setTypeParameters(@Nullable List<? extends TypeParameterElement> typeParameterElements);
        public abstract Builder setReturnType(TypeMirror returnType);
        public abstract Builder setParameters(@Nullable List<? extends VariableElement> parameters);
        public abstract Builder setReceiverType(@Nullable TypeMirror receiverType);
        public abstract Builder setVarArgs(boolean isVarArgs);
        public abstract Builder setDefault(boolean isDefault);
        public abstract Builder setThrownTypes(@Nullable List<? extends TypeMirror> thrownTypes);
        public abstract Builder setDefaultValue(@Nullable AnnotationValue annotationValue);
        public abstract Builder setKind(@Nullable ElementKind elementKind);
        public abstract Builder setModifiers(@Nullable Set<Modifier> modifiers);
        public abstract Builder setSimpleName(Name simpleName);
        public abstract Builder setEnclosingElement(Element enclosingElement);
        public abstract Builder setEnclosedElements(@Nullable List<? extends Element> enclosedElements);
        public abstract Builder setAnnotationMirrors(@Nullable List<? extends AnnotationMirror> annotationMirrors);
        public abstract Builder setFakedAnnotations(@Nullable List<Annotation> annotations);
        public abstract TestExecutableElement build();
    }

    public static Builder builder() {
        return new AutoValue_TestExecutableElement.Builder()
                .setDefault(false)
                .setVarArgs(false);
    }

    public static TestExecutableElement returningString(String methodName, AnnotationMirror... annotationMirrors) {
        return builder()
                .setReturnType(TestTypeMirror.string())
                .setSimpleName(TestNameUtil.createReal(methodName))
                .setAnnotationMirrors(Arrays.asList(annotationMirrors))
                .build();
    }

    @Nullable public abstract List<? extends TypeParameterElement> getTypeParameters();
    public abstract TypeMirror getReturnType();
    @Nullable public abstract List<? extends VariableElement> getParameters();
    @Nullable public abstract TypeMirror getReceiverType();
    public abstract boolean isVarArgs();
    public abstract boolean isDefault();
    @Nullable public abstract List<? extends TypeMirror> getThrownTypes();
    @Nullable public abstract AnnotationValue getDefaultValue();
    @Nullable public abstract ElementKind getKind();
    @Nullable public abstract Set<Modifier> getModifiers();
    public abstract Name getSimpleName();
    @Nullable public abstract Element getEnclosingElement();
    @Nullable public abstract List<? extends Element> getEnclosedElements();
    @Nullable public abstract List<? extends AnnotationMirror> getAnnotationMirrors();
    @Nullable public abstract List<Annotation> getFakedAnnotations();


    @Nullable public TypeMirror asType() {
        throw new UnsupportedOperationException("asType not supporte for TestExecutableElement");
    }

    @Override
    @Nullable public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return AnnotatedConstructUtil.getAnnotation(annotationType, getFakedAnnotations());
    }

    @Override
    @Nullable public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        return AnnotatedConstructUtil.getAnnotationsByType(annotationType, getFakedAnnotations());
    }

    @Override
    @Nullable public <R, P> R accept(ElementVisitor<R, P> v, P p) {
        throw new UnsupportedOperationException("accept not supported in test executable element");
    }

    public boolean hasAnnotations() {
        return getFakedAnnotations() != null && !getFakedAnnotations().isEmpty();
    }
}
