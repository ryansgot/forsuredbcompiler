package com.fsryan.forsuredb.util;

import com.google.auto.value.AutoValue;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AutoValue
public abstract class TestAnnotationMirror implements AnnotationMirror {

    @AutoValue.Builder
    public static abstract class Builder {
        public abstract Builder setAnnotationType(DeclaredType declaredType);
        public abstract Builder setElementValues(Map<ExecutableElement, AnnotationValue> elementValues);
        public abstract TestAnnotationMirror build();
    }

    public static Map<ExecutableElement, AnnotationValue> singletonElementValues(ExecutableElement ee, AnnotationValue av) {
        return createElementValues(Arrays.asList(ee), Arrays.asList(av));
    }

    public static Map<ExecutableElement, AnnotationValue> createElementValues(List<ExecutableElement> executableElements, List<AnnotationValue> annotationValues) {
        if (executableElements == null && annotationValues == null) {
            return null;
        }
        if (executableElements == null ^ annotationValues == null) {
            throw new IllegalArgumentException("cannot create map from executableElements '" + executableElements + "' and annotationValues '" + annotationValues + "'");
        }
        if (executableElements.size() != annotationValues.size()) {
            throw new IllegalArgumentException("cannot create element values from executable elements size: " + executableElements.size() + " and annotationValues size: " + annotationValues.size());
        }

        Map<ExecutableElement, AnnotationValue> ret = new HashMap<>(executableElements.size());
        for (int i = 0; i < executableElements.size(); i++) {
            ret.put(executableElements.get(i), annotationValues.get(i));
        }
        return ret;
    }

    public static Builder builder() {
        return new AutoValue_TestAnnotationMirror.Builder();
    }

    public abstract DeclaredType getAnnotationType();
    public abstract Map<ExecutableElement, AnnotationValue> getElementValues();
}
