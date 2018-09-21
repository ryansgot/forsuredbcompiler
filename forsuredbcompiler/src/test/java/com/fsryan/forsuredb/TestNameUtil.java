package com.fsryan.forsuredb;

import org.mockito.stubbing.Answer;

import javax.lang.model.element.Name;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class TestNameUtil {

    public static Name newMock(String name) {
        Name ret = mock(Name.class);
        when(ret.contentEquals(any(CharSequence.class)))
                .thenAnswer((Answer<Boolean>) inv -> name.contentEquals((CharSequence) inv.getArgument(0)));
        when(ret.contentEquals(any(CharSequence.class)))
                .thenAnswer((Answer<Boolean>) inv -> name.contentEquals((CharSequence) inv.getArgument(0)));
        when(ret.length()).thenAnswer((Answer<Integer>) inv -> name.length());
        when(ret.subSequence(anyInt(), anyInt()))
                .thenAnswer((Answer<CharSequence>) inv -> name.subSequence(inv.getArgument(0), inv.getArgument(1)));
        when(ret.toString()).thenReturn(name);
        return ret;
    }

    public static Name newSpy(String name) {
        return spy(createReal(name));
    }

    public static Name createReal(String name) {
        return new Name() {
            @Override
            public boolean contentEquals(CharSequence cs) {
                return name.contentEquals(cs);
            }

            @Override
            public int length() {
                return name.length();
            }

            @Override
            public char charAt(int index) {
                return name.charAt(index);
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                return name.subSequence(start, end);
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    public static Name forAnnotationValue() {
        return createReal("value");
    }
}
