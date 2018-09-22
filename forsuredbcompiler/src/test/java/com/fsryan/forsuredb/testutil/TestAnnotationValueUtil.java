package com.fsryan.forsuredb.testutil;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class TestAnnotationValueUtil {

    public static AnnotationValue createMock(Object object) {
        AnnotationValue ret = mock(AnnotationValue.class);
        when(ret.getValue()).thenReturn(object);
        return ret;
    }

    public static AnnotationValue createSpy(Object object) {
        return spy(createReal(object));
    }

    public static AnnotationValue createReal(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("Object must be non null");
        }
        return new AnnotationValue() {
            @Override
            public Object getValue() {
                return object;
            }

            @Override
            public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p) {
                throw new UnsupportedOperationException("TestAnnotationValueUtil does not support visitors");
            }
        };
    }
}
