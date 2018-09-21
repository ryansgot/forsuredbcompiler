package com.fsryan.forsuredb.annotationprocessor;

import com.fsryan.forsuredb.*;
import com.fsryan.forsuredb.annotationprocessor.util.AnnotationTranslatorFactory;
import com.fsryan.forsuredb.annotations.FSDefault;
import com.fsryan.forsuredb.info.ColumnInfo;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class InfoTransformerTest<I extends Element, E> {

    I input;
    E expected;

    public InfoTransformerTest(I input, E expected) {
        this.input = input;
        this.expected = expected;
    }

    @RunWith(Parameterized.class)
    public static class DefaultsFromExecutableElement extends InfoTransformerTest<ExecutableElement, ColumnInfo> {

        public DefaultsFromExecutableElement(ExecutableElement input, ColumnInfo expected) {
            super(input, expected);
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {   // 00: minimal with boolean primitive type
                            TestExecutableElement.builder()
                                    .setReturnType(TestTypeMirror.primitiveBoolean())
                                    .setSimpleName(TestNameUtil.createReal("booleanColumn"))
                                    .build(),
                            ColumnInfo.builder()
                                    .methodName("booleanColumn")
                                    .defaultValue("0")
                                    .qualifiedType(boolean.class.getTypeName())
                                    .build()
                    },
                    {   // 01: minimal with byte primitive type
                            TestExecutableElement.builder()
                                    .setReturnType(TestTypeMirror.primitiveByte())
                                    .setSimpleName(TestNameUtil.createReal("byteColumn"))
                                    .build(),
                            ColumnInfo.builder()
                                    .methodName("byteColumn")
                                    .defaultValue("0")
                                    .qualifiedType(byte.class.getTypeName())
                                    .build()
                    },
                    {   // 02: minimal with char primitive type
                            TestExecutableElement.builder()
                                    .setReturnType(TestTypeMirror.primitiveChar())
                                    .setSimpleName(TestNameUtil.createReal("charColumn"))
                                    .build(),
                            ColumnInfo.builder()
                                    .methodName("charColumn")
                                    .defaultValue("0")  // TODO: a
                                    .qualifiedType(char.class.getTypeName())
                                    .build()
                    },
                    {   // 03: minimal with double primitive type
                            TestExecutableElement.builder()
                                    .setReturnType(TestTypeMirror.primitiveDouble())
                                    .setSimpleName(TestNameUtil.createReal("doubleColumn"))
                                    .build(),
                            ColumnInfo.builder()
                                    .methodName("doubleColumn")
                                    .defaultValue("0")
                                    .qualifiedType(double.class.getTypeName())
                                    .build()
                    },
                    {   // 04: minimal with float primitive type
                            TestExecutableElement.builder()
                                    .setReturnType(TestTypeMirror.primitiveFloat())
                                    .setSimpleName(TestNameUtil.createReal("floatColumn"))
                                    .build(),
                            ColumnInfo.builder()
                                    .methodName("floatColumn")
                                    .defaultValue("0")
                                    .qualifiedType(float.class.getTypeName())
                                    .build()
                    },
                    {   // 05: minimal with int primitive type
                            TestExecutableElement.builder()
                                    .setReturnType(TestTypeMirror.primitiveInt())
                                    .setSimpleName(TestNameUtil.createReal("intColumn"))
                                    .build(),
                            ColumnInfo.builder()
                                    .methodName("intColumn")
                                    .defaultValue("0")
                                    .qualifiedType(int.class.getTypeName())
                                    .build()
                    },
                    {   // 06: minimal with long primitive type
                            TestExecutableElement.builder()
                                    .setReturnType(TestTypeMirror.primitiveLong())
                                    .setSimpleName(TestNameUtil.createReal("longColumn"))
                                    .build(),
                            ColumnInfo.builder()
                                    .methodName("longColumn")
                                    .defaultValue("0")
                                    .qualifiedType(long.class.getTypeName())
                                    .build()
                    },
                    {   // 07: minimal with short primitive type
                            TestExecutableElement.builder()
                                    .setReturnType(TestTypeMirror.primitiveShort())
                                    .setSimpleName(TestNameUtil.createReal("shortColumn"))
                                    .build(),
                            ColumnInfo.builder()
                                    .methodName("shortColumn")
                                    .defaultValue("0")
                                    .qualifiedType(short.class.getTypeName())
                                    .build()
                    },
                    {   // 08: Minimal with Boolean wrapper type
                            TestExecutableElement.builder()
                                    .setReturnType(TestTypeMirror.booleanWrapper())
                                    .setSimpleName(TestNameUtil.createReal("booleanWrapperColumn"))
                                    .build(),
                            ColumnInfo.builder()
                                    .methodName("booleanWrapperColumn")
                                    .qualifiedType(Boolean.class.getName())
                                    .build()
                    },
                    {   // 09: Minimal with boolean array type
                            TestExecutableElement.builder()
                                    .setReturnType(TestTypeMirror.byteArray())
                                    .setSimpleName(TestNameUtil.createReal("blobColumn"))
                                    .build(),
                            ColumnInfo.builder()
                                    .methodName("blobColumn")
                                    .qualifiedType(byte[].class.getCanonicalName())
                                    .build()
                    },
            });
        }

        @Test
        public void shouldCorrectlyCreateDefaultFromElement() {
            assertEquals(expected, InfoTransformer.defaultsFromElement(input).build());
        }
    }

    @RunWith(Parameterized.class)
    public static class FSDefaultInterpretationSuccessPath extends InfoTransformerTest<ExecutableElement, ColumnInfo> {

        public FSDefaultInterpretationSuccessPath(TestTypeMirror returnType, Name methodName, String defaultValue, ColumnInfo expected) {
            super(TestExecutableElement.builder()
                    .setReturnType(returnType)
                    .setSimpleName(methodName)
                    .setFakedAnnotations(Collections.singletonList(FakeAnnotationUtil.createFSDefault(defaultValue)))
                    .setAnnotationMirrors(Collections.singletonList(
                            TestAnnotationMirror.builder()
                                    .setAnnotationType(TestDeclaredType.of(FSDefault.class))
                                    .setElementValues(TestAnnotationMirror.singletonElementValues(
                                            TestExecutableElement.returningString("value"),
                                            TestAnnotationValueUtil.createReal(defaultValue)
                                    ))
                                    .build()))
                    .build(),
                    expected);
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {   // 00: primitive boolean with false lower-case default
                            TestTypeMirror.primitiveBoolean(),
                            TestNameUtil.createReal("booleanColumn"),
                            "false",
                            ColumnInfo.builder()
                                    .methodName("booleanColumn")
                                    .defaultValue("0")
                                    .qualifiedType(boolean.class.getTypeName())
                                    .build()
                    },
                    {   // 01: primitive boolean with false upper-case default
                            TestTypeMirror.primitiveBoolean(),
                            TestNameUtil.createReal("booleanColumn"),
                            "FALSE",
                            ColumnInfo.builder()
                                    .methodName("booleanColumn")
                                    .defaultValue("0")
                                    .qualifiedType(boolean.class.getTypeName())
                                    .build()
                    },
                    {   // 02: primitive boolean with 0 default
                            TestTypeMirror.primitiveBoolean(),
                            TestNameUtil.createReal("booleanColumn"),
                            String.valueOf(0),
                            ColumnInfo.builder()
                                    .methodName("booleanColumn")
                                    .defaultValue("0")
                                    .qualifiedType(boolean.class.getTypeName())
                                    .build()
                    },
                    {   // 03: primitive boolean with true lower-case default
                            TestTypeMirror.primitiveBoolean(),
                            TestNameUtil.createReal("booleanColumn"),
                            "true",
                            ColumnInfo.builder()
                                    .methodName("booleanColumn")
                                    .defaultValue("1")
                                    .qualifiedType(boolean.class.getTypeName())
                                    .build()
                    },
                    {   // 04: primitive boolean with true upper-case default
                            TestTypeMirror.primitiveBoolean(),
                            TestNameUtil.createReal("booleanColumn"),
                            "TRUE",
                            ColumnInfo.builder()
                                    .methodName("booleanColumn")
                                    .defaultValue("1")
                                    .qualifiedType(boolean.class.getTypeName())
                                    .build()
                    },
                    {   // 05: primitive boolean with 1 default
                            TestTypeMirror.primitiveBoolean(),
                            TestNameUtil.createReal("booleanColumn"),
                            String.valueOf(1),
                            ColumnInfo.builder()
                                    .methodName("booleanColumn")
                                    .defaultValue("1")
                                    .qualifiedType(boolean.class.getTypeName())
                                    .build()
                    },
                    {   // 06: boolean wrapper with false lower-case default
                            TestTypeMirror.booleanWrapper(),
                            TestNameUtil.createReal("booleanWrapperColumn"),
                            "false",
                            ColumnInfo.builder()
                                    .methodName("booleanWrapperColumn")
                                    .defaultValue("0")
                                    .qualifiedType(Boolean.class.getTypeName())
                                    .build()
                    },
                    {   // 07: boolean wrapper with false upper-case default
                            TestTypeMirror.booleanWrapper(),
                            TestNameUtil.createReal("booleanWrapperColumn"),
                            "FALSE",
                            ColumnInfo.builder()
                                    .methodName("booleanWrapperColumn")
                                    .defaultValue("0")
                                    .qualifiedType(Boolean.class.getTypeName())
                                    .build()
                    },
                    {   // 08: boolean wrapper with value of zero
                            TestTypeMirror.booleanWrapper(),
                            TestNameUtil.createReal("booleanWrapperColumn"),
                            String.valueOf(0),
                            ColumnInfo.builder()
                                    .methodName("booleanWrapperColumn")
                                    .defaultValue("0")
                                    .qualifiedType(Boolean.class.getTypeName())
                                    .build()
                    },
                    {   // 09: boolean wrapper with true lower-case default
                            TestTypeMirror.booleanWrapper(),
                            TestNameUtil.createReal("booleanWrapperColumn"),
                            "true",
                            ColumnInfo.builder()
                                    .methodName("booleanWrapperColumn")
                                    .defaultValue("1")
                                    .qualifiedType(Boolean.class.getTypeName())
                                    .build()
                    },
                    {   // 10: boolean wrapper with true upper-case default
                            TestTypeMirror.booleanWrapper(),
                            TestNameUtil.createReal("booleanWrapperColumn"),
                            "TRUE",
                            ColumnInfo.builder()
                                    .methodName("booleanWrapperColumn")
                                    .defaultValue("1")
                                    .qualifiedType(Boolean.class.getTypeName())
                                    .build()
                    },
                    {   // 11: boolean wrapper with 1 default
                            TestTypeMirror.booleanWrapper(),
                            TestNameUtil.createReal("booleanWrapperColumn"),
                            String.valueOf(1),
                            ColumnInfo.builder()
                                    .methodName("booleanWrapperColumn")
                                    .defaultValue("1")
                                    .qualifiedType(Boolean.class.getTypeName())
                                    .build()
                    },
                    {   // 12: byte primitive with 1 default
                            TestTypeMirror.primitiveByte(),
                            TestNameUtil.createReal("byteColumn"),
                            String.valueOf(1),
                            ColumnInfo.builder()
                                    .methodName("byteColumn")
                                    .defaultValue("1")
                                    .qualifiedType(byte.class.getTypeName())
                                    .build()
                    },
                    {   // 13: byte wrapper with 1 default
                            TestTypeMirror.byteWrapper(),
                            TestNameUtil.createReal("byteWrapperColumn"),
                            String.valueOf(1),
                            ColumnInfo.builder()
                                    .methodName("byteWrapperColumn")
                                    .defaultValue("1")
                                    .qualifiedType(Byte.class.getTypeName())
                                    .build()
                    },
                    // TODO: char and Character when appropriate
                    {   // 14: double primitive with 1.238746 default
                            TestTypeMirror.primitiveDouble(),
                            TestNameUtil.createReal("doubleColumn"),
                            String.valueOf(1.238746),
                            ColumnInfo.builder()
                                    .methodName("doubleColumn")
                                    .defaultValue("1.238746")
                                    .qualifiedType(double.class.getTypeName())
                                    .build()
                    },
                    {   // 15: double wrapper with 1.238746 default
                            TestTypeMirror.doubleWrapper(),
                            TestNameUtil.createReal("doubleWrapperColumn"),
                            String.valueOf(1.238746),
                            ColumnInfo.builder()
                                    .methodName("doubleWrapperColumn")
                                    .defaultValue("1.238746")
                                    .qualifiedType(Double.class.getTypeName())
                                    .build()
                    },
                    {   // 16: float primitive with 1.238746 default
                            TestTypeMirror.primitiveDouble(),
                            TestNameUtil.createReal("doubleColumn"),
                            String.valueOf(1.238746),
                            ColumnInfo.builder()
                                    .methodName("doubleColumn")
                                    .defaultValue("1.238746")
                                    .qualifiedType(double.class.getTypeName())
                                    .build()
                    },
                    {   // 17: float wrapper with 1.238746 default
                            TestTypeMirror.doubleWrapper(),
                            TestNameUtil.createReal("doubleWrapperColumn"),
                            String.valueOf(1.238746),
                            ColumnInfo.builder()
                                    .methodName("doubleWrapperColumn")
                                    .defaultValue("1.238746")
                                    .qualifiedType(Double.class.getTypeName())
                                    .build()
                    },
                    {   // 18: int primitive with 1 default
                            TestTypeMirror.primitiveInt(),
                            TestNameUtil.createReal("intColumn"),
                            String.valueOf(1),
                            ColumnInfo.builder()
                                    .methodName("intColumn")
                                    .defaultValue("1")
                                    .qualifiedType(int.class.getTypeName())
                                    .build()
                    },
                    {   // 19: int wrapper with 1 default
                            TestTypeMirror.intWrapper(),
                            TestNameUtil.createReal("integerWrapperColumn"),
                            String.valueOf(1),
                            ColumnInfo.builder()
                                    .methodName("integerWrapperColumn")
                                    .defaultValue("1")
                                    .qualifiedType(Integer.class.getTypeName())
                                    .build()
                    },
                    {   // 20: long primitive with 1 default
                            TestTypeMirror.primitiveLong(),
                            TestNameUtil.createReal("longColumn"),
                            String.valueOf(1),
                            ColumnInfo.builder()
                                    .methodName("longColumn")
                                    .defaultValue("1")
                                    .qualifiedType(long.class.getTypeName())
                                    .build()
                    },
                    {   // 21: long wrapper with 1 default
                            TestTypeMirror.longWrapper(),
                            TestNameUtil.createReal("longWrapperColumn"),
                            String.valueOf(1),
                            ColumnInfo.builder()
                                    .methodName("longWrapperColumn")
                                    .defaultValue("1")
                                    .qualifiedType(Long.class.getTypeName())
                                    .build()
                    },
                    {   // 22: short primitive with 1 default
                            TestTypeMirror.primitiveShort(),
                            TestNameUtil.createReal("shortColumn"),
                            String.valueOf(1),
                            ColumnInfo.builder()
                                    .methodName("shortColumn")
                                    .defaultValue("1")
                                    .qualifiedType(short.class.getTypeName())
                                    .build()
                    },
                    {   // 23: short wrapper with 1 default
                            TestTypeMirror.shortWrapper(),
                            TestNameUtil.createReal("shortWrapperColumn"),
                            String.valueOf(1),
                            ColumnInfo.builder()
                                    .methodName("shortWrapperColumn")
                                    .defaultValue("1")
                                    .qualifiedType(Short.class.getTypeName())
                                    .build()
                    },
                    {   // 24: BigInteger with 1984349683434984 default
                            TestTypeMirror.bigInteger(),
                            TestNameUtil.createReal("bigIntegerColumn"),
                            String.valueOf(new BigInteger("1984349683434984")),
                            ColumnInfo.builder()
                                    .methodName("bigIntegerColumn")
                                    .defaultValue("1984349683434984")
                                    .qualifiedType(BigInteger.class.getTypeName())
                                    .build()
                    },
                    {   // 25: BigDecimal with 1984349683434984.289334857843754 default
                            TestTypeMirror.bigDecimal(),
                            TestNameUtil.createReal("bigDecimalColumn"),
                            String.valueOf(new BigDecimal("1984349683434984.289334857843754")),
                            ColumnInfo.builder()
                                    .methodName("bigDecimalColumn")
                                    .defaultValue("1984349683434984.289334857843754")
                                    .qualifiedType(BigDecimal.class.getTypeName())
                                    .build()
                    },
                    {   // 26: byte[] with 7526ff726d95e170f09a14a1261110d4d3368cf8 default
                            TestTypeMirror.byteArray(),
                            TestNameUtil.createReal("blobColumn"),
                            "7526ff726d95e170f09a14a1261110d4d3368cf8",
                            ColumnInfo.builder()
                                    .methodName("blobColumn")
                                    .defaultValue("7526ff726d95e170f09a14a1261110d4d3368cf8")
                                    .qualifiedType(byte[].class.getCanonicalName())
                                    .build()
                    }
            });
        }

        @Before
        public void setUpTranslatorFactory() {
            Elements mockElements = mock(Elements.class);
            when(mockElements.getElementValuesWithDefaults(any(AnnotationMirror.class)))
                    .thenAnswer((Answer<Map<? extends ExecutableElement, ? extends AnnotationValue>>) invocation -> {
                        return input.getAnnotationMirrors().get(0).getElementValues();
                    });
            AnnotationTranslatorFactory.init(TestProcessingEnvironment.withElements(mockElements));
        }

        @After
        public void tearDownAnnotationTranslatorFactory() throws Exception {
            Field instance = AnnotationTranslatorFactory.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, null);
        }

        @Test
        public void shouldCorrectlyInterpretDefaultValue() {
            assertEquals(expected, InfoTransformer.transform(input).build());
        }
    }
}
