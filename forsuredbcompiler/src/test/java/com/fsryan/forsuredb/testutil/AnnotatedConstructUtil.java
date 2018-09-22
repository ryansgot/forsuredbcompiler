package com.fsryan.forsuredb.testutil;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.List;

public class AnnotatedConstructUtil {
    @Nullable
    public static <A extends Annotation> A getAnnotation(Class<A> annotationType, List<? extends Annotation> annotations) {
        if (annotations == null || annotations.isEmpty()) {
            return null;
        }

        return (A) annotations.stream()
                .filter(annotation -> annotation.annotationType().equals(annotationType))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public static <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType, List<? extends Annotation> annotations) {
        if (annotations == null || annotations.isEmpty()) {
            return null;
        }
        return (A[]) annotations.stream()
                .filter(annotation -> annotation.annotationType().equals(annotationType))
                .toArray();
    }
}
