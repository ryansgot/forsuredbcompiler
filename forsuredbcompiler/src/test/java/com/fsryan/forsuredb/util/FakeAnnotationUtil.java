package com.fsryan.forsuredb.util;

import com.fsryan.forsuredb.annotations.FSDefault;

import java.lang.annotation.Annotation;

public abstract class FakeAnnotationUtil {

    public static FSDefault createFSDefault(String value) {
        return new FSDefault() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return FSDefault.class;
            }

            @Override
            public String value() {
                return value;
            }
        };
    }
}
