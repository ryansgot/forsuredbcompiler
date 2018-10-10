package com.fsryan.forsuredb.annotationprocessor;

import com.fsryan.forsuredb.annotationprocessor.util.AnnotationTranslatorFactory;
import com.fsryan.forsuredb.annotations.FSColumn;
import com.fsryan.forsuredb.annotations.FSDefault;
import com.fsryan.forsuredb.annotations.ForeignKey;
import com.fsryan.forsuredb.api.FSGetApi;
import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.TableIndexInfo;
import com.fsryan.forsuredb.testutil.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.stubbing.Answer;

import javax.lang.model.element.*;
import javax.lang.model.util.Elements;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static com.fsryan.forsuredb.testutil.FSTestTypesUtil.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class InfoTranslatorTest<I extends Element, E> {

    I input;
    E expected;

    public InfoTranslatorTest(I input, E expected) {
        this.input = input;
        this.expected = expected;
    }

    static abstract class UsingAnnotationTranslatorFactory<I extends Element, E> extends InfoTranslatorTest<I, E> {

        public UsingAnnotationTranslatorFactory(I input, E expected) {
            super(input, expected);
        }

        @Before
        public void setUpTranslatorFactory() {
            Elements mockElements = mock(Elements.class);
            setUpMockElements(mockElements);
            AnnotationTranslatorFactory.init(TestProcessingEnvironment.withElements(mockElements));
        }

        @After
        public void tearDownAnnotationTranslatorFactory() throws Exception {
            Field instance = AnnotationTranslatorFactory.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, null);
        }

        abstract void setUpMockElements(Elements mockElements);
    }

    @RunWith(Parameterized.class)
    public static class DefaultsFromExecutableElement extends InfoTranslatorTest<ExecutableElement, ColumnInfo> {

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
                    {   // 09: Minimal with byte array type
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
            assertEquals(expected, InfoTranslator.defaultsFromElement(input).build());
        }
    }

    @RunWith(Parameterized.class)
    public static class FSDefaultInterpretationSuccessPath extends UsingAnnotationTranslatorFactory<ExecutableElement, ColumnInfo> {

        public FSDefaultInterpretationSuccessPath(TestTypeMirror returnType, Name methodName, String defaultValue, ColumnInfo expected) {
            super(TestExecutableElement.builder()
                    .setReturnType(returnType)
                    .setSimpleName(methodName)
                    .setFakedAnnotations(Collections.singletonList(FSTestTypesUtil.createFSDefault(defaultValue)))
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
                    {   // 12: byte primitive with min value default
                            TestTypeMirror.primitiveByte(),
                            TestNameUtil.createReal("byteColumn"),
                            String.valueOf(Byte.MIN_VALUE),
                            ColumnInfo.builder()
                                    .methodName("byteColumn")
                                    .defaultValue(String.valueOf(Byte.MIN_VALUE))
                                    .qualifiedType(byte.class.getTypeName())
                                    .build()
                    },
                    {   // 13: byte primitive with max value default
                            TestTypeMirror.primitiveByte(),
                            TestNameUtil.createReal("byteColumn"),
                            String.valueOf(Byte.MAX_VALUE),
                            ColumnInfo.builder()
                                    .methodName("byteColumn")
                                    .defaultValue(String.valueOf(Byte.MAX_VALUE))
                                    .qualifiedType(byte.class.getTypeName())
                                    .build()
                    },
                    {   // 14: byte wrapper with min value default
                            TestTypeMirror.byteWrapper(),
                            TestNameUtil.createReal("byteWrapperColumn"),
                            String.valueOf(Byte.MIN_VALUE),
                            ColumnInfo.builder()
                                    .methodName("byteWrapperColumn")
                                    .defaultValue(String.valueOf(Byte.MIN_VALUE))
                                    .qualifiedType(Byte.class.getTypeName())
                                    .build()
                    },
                    {   // 15: byte wrapper with max value default
                            TestTypeMirror.byteWrapper(),
                            TestNameUtil.createReal("byteWrapperColumn"),
                            String.valueOf(Byte.MAX_VALUE),
                            ColumnInfo.builder()
                                    .methodName("byteWrapperColumn")
                                    .defaultValue(String.valueOf(Byte.MAX_VALUE))
                                    .qualifiedType(Byte.class.getTypeName())
                                    .build()
                    },
                    // TODO: char and Character when appropriate
                    {   // 16: double primitive with positive floating point value
                            TestTypeMirror.primitiveDouble(),
                            TestNameUtil.createReal("doubleColumn"),
                            String.valueOf(1.238746),
                            ColumnInfo.builder()
                                    .methodName("doubleColumn")
                                    .defaultValue(String.valueOf(1.238746))
                                    .qualifiedType(double.class.getTypeName())
                                    .build()
                    },
                    {   // 17: double primitive with negative floating point value
                            TestTypeMirror.primitiveDouble(),
                            TestNameUtil.createReal("doubleColumn"),
                            String.valueOf(-1.238746),
                            ColumnInfo.builder()
                                    .methodName("doubleColumn")
                                    .defaultValue(String.valueOf(-1.238746))
                                    .qualifiedType(double.class.getTypeName())
                                    .build()
                    },
                    {   // 18: double wrapper with positive floating point value
                            TestTypeMirror.doubleWrapper(),
                            TestNameUtil.createReal("doubleWrapperColumn"),
                            String.valueOf(1.238746),
                            ColumnInfo.builder()
                                    .methodName("doubleWrapperColumn")
                                    .defaultValue(String.valueOf(1.238746))
                                    .qualifiedType(Double.class.getTypeName())
                                    .build()
                    },
                    {   // 19: double wrapper with negative floating point value
                            TestTypeMirror.doubleWrapper(),
                            TestNameUtil.createReal("doubleWrapperColumn"),
                            String.valueOf(-1.238746),
                            ColumnInfo.builder()
                                    .methodName("doubleWrapperColumn")
                                    .defaultValue(String.valueOf(-1.238746))
                                    .qualifiedType(Double.class.getTypeName())
                                    .build()
                    },
                    {   // 20: float primitive with positive floating point value
                            TestTypeMirror.primitiveFloat(),
                            TestNameUtil.createReal("floatColumn"),
                            String.valueOf(1.238746),
                            ColumnInfo.builder()
                                    .methodName("floatColumn")
                                    .defaultValue(String.valueOf(1.238746))
                                    .qualifiedType(float.class.getTypeName())
                                    .build()
                    },
                    {   // 21: float wrapper with negative floating point value
                            TestTypeMirror.primitiveFloat(),
                            TestNameUtil.createReal("floatColumn"),
                            String.valueOf(-1.238746),
                            ColumnInfo.builder()
                                    .methodName("floatColumn")
                                    .defaultValue(String.valueOf(-1.238746))
                                    .qualifiedType(float.class.getTypeName())
                                    .build()
                    },
                    {   // 22: float wrapper with positive floating point value
                            TestTypeMirror.floatWrapper(),
                            TestNameUtil.createReal("floatWrapperColumn"),
                            String.valueOf(1.238746),
                            ColumnInfo.builder()
                                    .methodName("floatWrapperColumn")
                                    .defaultValue(String.valueOf(1.238746))
                                    .qualifiedType(Float.class.getTypeName())
                                    .build()
                    },
                    {   // 23: float wrapper with negative floating point value
                            TestTypeMirror.floatWrapper(),
                            TestNameUtil.createReal("floatWrapperColumn"),
                            String.valueOf(-1.238746),
                            ColumnInfo.builder()
                                    .methodName("floatWrapperColumn")
                                    .defaultValue(String.valueOf(-1.238746))
                                    .qualifiedType(Float.class.getTypeName())
                                    .build()
                    },
                    {   // 24: int primitive with positive value
                            TestTypeMirror.primitiveInt(),
                            TestNameUtil.createReal("intColumn"),
                            String.valueOf(Integer.MAX_VALUE),
                            ColumnInfo.builder()
                                    .methodName("intColumn")
                                    .defaultValue(String.valueOf(Integer.MAX_VALUE))
                                    .qualifiedType(int.class.getTypeName())
                                    .build()
                    },
                    {   // 25: int primitive with negative value
                            TestTypeMirror.primitiveInt(),
                            TestNameUtil.createReal("intColumn"),
                            String.valueOf(Integer.MIN_VALUE),
                            ColumnInfo.builder()
                                    .methodName("intColumn")
                                    .defaultValue(String.valueOf(Integer.MIN_VALUE))
                                    .qualifiedType(int.class.getTypeName())
                                    .build()
                    },
                    {   // 26: int wrapper with positive value
                            TestTypeMirror.intWrapper(),
                            TestNameUtil.createReal("intWrapperColumn"),
                            String.valueOf(Integer.MAX_VALUE),
                            ColumnInfo.builder()
                                    .methodName("intWrapperColumn")
                                    .defaultValue(String.valueOf(Integer.MAX_VALUE))
                                    .qualifiedType(Integer.class.getTypeName())
                                    .build()
                    },
                    {   // 27: int wrapper with negative value
                            TestTypeMirror.intWrapper(),
                            TestNameUtil.createReal("intWrapperColumn"),
                            String.valueOf(Integer.MIN_VALUE),
                            ColumnInfo.builder()
                                    .methodName("intWrapperColumn")
                                    .defaultValue(String.valueOf(Integer.MIN_VALUE))
                                    .qualifiedType(Integer.class.getTypeName())
                                    .build()
                    },
                    {   // 28: long primitive with positive value
                            TestTypeMirror.primitiveLong(),
                            TestNameUtil.createReal("longColumn"),
                            String.valueOf(Long.MAX_VALUE),
                            ColumnInfo.builder()
                                    .methodName("longColumn")
                                    .defaultValue(String.valueOf(Long.MAX_VALUE))
                                    .qualifiedType(long.class.getTypeName())
                                    .build()
                    },
                    {   // 29: long primitive with negative value
                            TestTypeMirror.primitiveLong(),
                            TestNameUtil.createReal("longColumn"),
                            String.valueOf(Long.MIN_VALUE),
                            ColumnInfo.builder()
                                    .methodName("longColumn")
                                    .defaultValue(String.valueOf(Long.MIN_VALUE))
                                    .qualifiedType(long.class.getTypeName())
                                    .build()
                    },
                    {   // 30: long wrapper with positive value
                            TestTypeMirror.longWrapper(),
                            TestNameUtil.createReal("longWrapperColumn"),
                            String.valueOf(Long.MAX_VALUE),
                            ColumnInfo.builder()
                                    .methodName("longWrapperColumn")
                                    .defaultValue(String.valueOf(Long.MAX_VALUE))
                                    .qualifiedType(Long.class.getTypeName())
                                    .build()
                    },
                    {   // 31: long wrapper with negative value
                            TestTypeMirror.longWrapper(),
                            TestNameUtil.createReal("longWrapperColumn"),
                            String.valueOf(Long.MIN_VALUE),
                            ColumnInfo.builder()
                                    .methodName("longWrapperColumn")
                                    .defaultValue(String.valueOf(Long.MIN_VALUE))
                                    .qualifiedType(Long.class.getTypeName())
                                    .build()
                    },
                    {   // 32: short primitive with positive value
                            TestTypeMirror.primitiveShort(),
                            TestNameUtil.createReal("shortColumn"),
                            String.valueOf(Short.MAX_VALUE),
                            ColumnInfo.builder()
                                    .methodName("shortColumn")
                                    .defaultValue(String.valueOf(Short.MAX_VALUE))
                                    .qualifiedType(short.class.getTypeName())
                                    .build()
                    },
                    {   // 33: short primitive with negative value
                            TestTypeMirror.primitiveShort(),
                            TestNameUtil.createReal("shortColumn"),
                            String.valueOf(Short.MIN_VALUE),
                            ColumnInfo.builder()
                                    .methodName("shortColumn")
                                    .defaultValue(String.valueOf(Short.MIN_VALUE))
                                    .qualifiedType(short.class.getTypeName())
                                    .build()
                    },
                    {   // 34: short wrapper with positive value
                            TestTypeMirror.shortWrapper(),
                            TestNameUtil.createReal("shortWrapperColumn"),
                            String.valueOf(Short.MAX_VALUE),
                            ColumnInfo.builder()
                                    .methodName("shortWrapperColumn")
                                    .defaultValue(String.valueOf(Short.MAX_VALUE))
                                    .qualifiedType(Short.class.getTypeName())
                                    .build()
                    },
                    {   // 35: short wrapper with negative value
                            TestTypeMirror.shortWrapper(),
                            TestNameUtil.createReal("shortWrapperColumn"),
                            String.valueOf(Short.MIN_VALUE),
                            ColumnInfo.builder()
                                    .methodName("shortWrapperColumn")
                                    .defaultValue(String.valueOf(Short.MIN_VALUE))
                                    .qualifiedType(Short.class.getTypeName())
                                    .build()
                    },
                    {   // 36: BigInteger with positive value
                            TestTypeMirror.bigInteger(),
                            TestNameUtil.createReal("bigIntegerColumn"),
                            String.valueOf(new BigInteger("1984349683434984")),
                            ColumnInfo.builder()
                                    .methodName("bigIntegerColumn")
                                    .defaultValue("1984349683434984")
                                    .qualifiedType(BigInteger.class.getTypeName())
                                    .build()
                    },
                    {   // 37: BigInteger with negative default
                            TestTypeMirror.bigInteger(),
                            TestNameUtil.createReal("bigIntegerColumn"),
                            String.valueOf(new BigInteger("-1984349683434984")),
                            ColumnInfo.builder()
                                    .methodName("bigIntegerColumn")
                                    .defaultValue("-1984349683434984")
                                    .qualifiedType(BigInteger.class.getTypeName())
                                    .build()
                    },
                    {   // 38: BigDecimal with positive default
                            TestTypeMirror.bigDecimal(),
                            TestNameUtil.createReal("bigDecimalColumn"),
                            String.valueOf(new BigDecimal("1984349683434984.289334857843754")),
                            ColumnInfo.builder()
                                    .methodName("bigDecimalColumn")
                                    .defaultValue("1984349683434984.289334857843754")
                                    .qualifiedType(BigDecimal.class.getTypeName())
                                    .build()
                    },
                    {   // 39: BigDecimal with negative default
                            TestTypeMirror.bigDecimal(),
                            TestNameUtil.createReal("bigDecimalColumn"),
                            String.valueOf(new BigDecimal("-1984349683434984.289334857843754")),
                            ColumnInfo.builder()
                                    .methodName("bigDecimalColumn")
                                    .defaultValue("-1984349683434984.289334857843754")
                                    .qualifiedType(BigDecimal.class.getTypeName())
                                    .build()
                    },
                    {   // 40: byte[] with 7526ff726d95e170f09a14a1261110d4d3368cf8 default
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

        @Test
        public void shouldCorrectlyInterpretDefaultValue() {
            assertEquals(expected, InfoTranslator.toColumnInfoBuilder(input).build());
        }

        protected void setUpMockElements(Elements mockElements) {
            when(mockElements.getElementValuesWithDefaults(any(AnnotationMirror.class)))
                    .thenAnswer((Answer<Map<? extends ExecutableElement, ? extends AnnotationValue>>) invocation -> {
                        return input.getAnnotationMirrors().get(0).getElementValues();
                    });
        }
    }

    @RunWith(Parameterized.class)
    public static class FSDefaultInterpretationFailurePath extends UsingAnnotationTranslatorFactory<ExecutableElement, Exception> {

        static final String BYTE_TOO_LOW = String.valueOf(Byte.MIN_VALUE - 1);
        static final String BYTE_TOO_HIGH = String.valueOf(Byte.MAX_VALUE + 1);
        static final String DOUBLE_TOO_LOW = new BigDecimal(Double.MAX_VALUE).negate().multiply(BigDecimal.TEN).toPlainString();
        static final String DOUBLE_TOO_HIGH = new BigDecimal(Double.MAX_VALUE).multiply(BigDecimal.TEN).toPlainString();
        static final String FLOAT_TOO_LOW = new BigDecimal(Float.MAX_VALUE).negate().multiply(BigDecimal.TEN).toPlainString();
        static final String FLOAT_TOO_HIGH = new BigDecimal(Float.MAX_VALUE).multiply(BigDecimal.TEN).toPlainString();
        static final String INT_TOO_LOW = String.valueOf((long) Integer.MIN_VALUE - 1);
        static final String INT_TOO_HIGH = String.valueOf((long) Integer.MAX_VALUE + 1);
        static final String LONG_TOO_LOW = new BigInteger(Long.toString(Long.MIN_VALUE)).subtract(BigInteger.ONE).toString();
        static final String LONG_TOO_HIGH = new BigInteger(Long.toString(Long.MAX_VALUE)).add(BigInteger.ONE).toString();
        static final String SHORT_TOO_LOW = String.valueOf(Short.MIN_VALUE - 1);
        static final String SHORT_TOO_HIGH = String.valueOf(Short.MAX_VALUE + 1);

        public FSDefaultInterpretationFailurePath(TestTypeMirror returnType, Name methodName, String defaultValue, Exception expected) {
            super(TestExecutableElement.builder()
                    .setReturnType(returnType)
                    .setSimpleName(methodName)
                    .setFakedAnnotations(Collections.singletonList(FSTestTypesUtil.createFSDefault(defaultValue)))
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
                    {   // 00: primitive boolean with invalid negative value
                            TestTypeMirror.primitiveBoolean(),
                            TestNameUtil.createReal("booleanColumn"),
                            "-1",
                            new RuntimeException("boolean value for booleanColumn invalid default: -1")
                    },
                    {   // 01: primitive boolean with invalid positive value
                            TestTypeMirror.primitiveBoolean(),
                            TestNameUtil.createReal("booleanColumn"),
                            "2",
                            new RuntimeException("boolean value for booleanColumn invalid default: 2")
                    },
                    {   // 02: primitive boolean with invalid word
                            TestTypeMirror.primitiveBoolean(),
                            TestNameUtil.createReal("booleanColumn"),
                            "truce",
                            new RuntimeException("boolean value for booleanColumn invalid default: truce")
                    },
                    {   // 03: boolean wrapper with invalid negative value
                            TestTypeMirror.booleanWrapper(),
                            TestNameUtil.createReal("booleanWrapperColumn"),
                            String.valueOf(Integer.MIN_VALUE),
                            new RuntimeException("java.lang.Boolean value for booleanWrapperColumn invalid default: " + String.valueOf(Integer.MIN_VALUE))
                    },
                    {   // 04: boolean wrapper with invalid positive value
                            TestTypeMirror.booleanWrapper(),
                            TestNameUtil.createReal("booleanWrapperColumn"),
                            String.valueOf(Integer.MAX_VALUE),
                            new RuntimeException("java.lang.Boolean value for booleanWrapperColumn invalid default: " + String.valueOf(Integer.MAX_VALUE))
                    },
                    {   // 05: boolean wrapper with invalid word
                            TestTypeMirror.booleanWrapper(),
                            TestNameUtil.createReal("booleanWrapperColumn"),
                            "falsify",
                            new RuntimeException("java.lang.Boolean value for booleanWrapperColumn invalid default: falsify")
                    },
                    {   // 06: byte primitive with word default
                            TestTypeMirror.primitiveByte(),
                            TestNameUtil.createReal("byteColumn"),
                            "Seven",
                            new RuntimeException("byte value for byteColumn invalid default: Seven")
                    },
                    {   // 07: byte primitive outside minimum bounds
                            TestTypeMirror.primitiveByte(),
                            TestNameUtil.createReal("byteColumn"),
                            BYTE_TOO_LOW,
                            new RuntimeException("byte value for byteColumn invalid default: " + BYTE_TOO_LOW)
                    },
                    {   // 08: byte primitive outside maximum bounds
                            TestTypeMirror.primitiveByte(),
                            TestNameUtil.createReal("byteColumn"),
                            BYTE_TOO_HIGH,
                            new RuntimeException("byte value for byteColumn invalid default: " + BYTE_TOO_HIGH)
                    },
                    {   // 09: byte wrapper with word default
                            TestTypeMirror.byteWrapper(),
                            TestNameUtil.createReal("byteWrapperColumn"),
                            "Seven",
                            new RuntimeException(Byte.class.getName() + " value for byteWrapperColumn invalid default: Seven")
                    },
                    {   // 10: byte wrapper outside minimum bounds
                            TestTypeMirror.byteWrapper(),
                            TestNameUtil.createReal("byteWrapperColumn"),
                            BYTE_TOO_LOW,
                            new RuntimeException(Byte.class.getName() + " value for byteWrapperColumn invalid default: " + BYTE_TOO_LOW)
                    },
                    {   // 11: byte wrapper outside maximum bounds
                            TestTypeMirror.byteWrapper(),
                            TestNameUtil.createReal("byteWrapperColumn"),
                            BYTE_TOO_HIGH,
                            new RuntimeException(Byte.class.getName() + " value for byteWrapperColumn invalid default: " + BYTE_TOO_HIGH)
                    },
                    {   // 12: double primitive with word default
                            TestTypeMirror.primitiveDouble(),
                            TestNameUtil.createReal("doubleColumn"),
                            "Seven",
                            new RuntimeException("double value for doubleColumn invalid default: Seven")
                    },
                    {   // 13: double primitive outside minimum bounds
                            TestTypeMirror.primitiveDouble(),
                            TestNameUtil.createReal("doubleColumn"),
                            DOUBLE_TOO_LOW,
                            new RuntimeException("double value for doubleColumn invalid default: " + DOUBLE_TOO_LOW)
                    },
                    {   // 14: double primitive outside maximum bounds
                            TestTypeMirror.primitiveDouble(),
                            TestNameUtil.createReal("doubleColumn"),
                            DOUBLE_TOO_HIGH,
                            new RuntimeException("double value for doubleColumn invalid default: " + DOUBLE_TOO_HIGH)
                    },
                    {   // 15: double wrapper with word default
                            TestTypeMirror.doubleWrapper(),
                            TestNameUtil.createReal("doubleWrapperColumn"),
                            "Seven",
                            new RuntimeException(Double.class.getName() + " value for doubleWrapperColumn invalid default: Seven")
                    },
                    {   // 16: double wrapper outside minimum bounds
                            TestTypeMirror.doubleWrapper(),
                            TestNameUtil.createReal("doubleWrapperColumn"),
                            DOUBLE_TOO_LOW,
                            new RuntimeException(Double.class.getName() + " value for doubleWrapperColumn invalid default: " + DOUBLE_TOO_LOW)
                    },
                    {   // 17: double wrapper outside maximum bounds
                            TestTypeMirror.doubleWrapper(),
                            TestNameUtil.createReal("doubleWrapperColumn"),
                            DOUBLE_TOO_HIGH,
                            new RuntimeException(Double.class.getName() + " value for doubleWrapperColumn invalid default: " + DOUBLE_TOO_HIGH)
                    },
                    {   // 18: float primitive with word default
                            TestTypeMirror.primitiveFloat(),
                            TestNameUtil.createReal("floatColumn"),
                            "Seven",
                            new RuntimeException("float value for floatColumn invalid default: Seven")
                    },
                    {   // 19: float primitive outside minimum bounds
                            TestTypeMirror.primitiveFloat(),
                            TestNameUtil.createReal("floatColumn"),
                            FLOAT_TOO_LOW,
                            new RuntimeException("float value for floatColumn invalid default: " + FLOAT_TOO_LOW)
                    },
                    {   // 20: float primitive outside maximum bounds
                            TestTypeMirror.primitiveFloat(),
                            TestNameUtil.createReal("floatColumn"),
                            FLOAT_TOO_HIGH,
                            new RuntimeException("float value for floatColumn invalid default: " + FLOAT_TOO_HIGH)
                    },
                    {   // 21: float wrapper with word default
                            TestTypeMirror.floatWrapper(),
                            TestNameUtil.createReal("floatWrapperColumn"),
                            "Seven",
                            new RuntimeException(Float.class.getName() + " value for floatWrapperColumn invalid default: Seven")
                    },
                    {   // 22: float wrapper outside minimum bounds
                            TestTypeMirror.floatWrapper(),
                            TestNameUtil.createReal("floatWrapperColumn"),
                            FLOAT_TOO_LOW,
                            new RuntimeException(Float.class.getName() + " value for floatWrapperColumn invalid default: " + FLOAT_TOO_LOW)
                    },
                    {   // 23: float wrapper outside maximum bounds
                            TestTypeMirror.floatWrapper(),
                            TestNameUtil.createReal("floatWrapperColumn"),
                            FLOAT_TOO_HIGH,
                            new RuntimeException(Float.class.getName() + " value for floatWrapperColumn invalid default: " + FLOAT_TOO_HIGH)
                    },
                    {   // 24: int primitive with word default
                            TestTypeMirror.primitiveInt(),
                            TestNameUtil.createReal("intColumn"),
                            "Seven",
                            new RuntimeException("int value for intColumn invalid default: Seven")
                    },
                    {   // 25: int primitive outside minimum bounds
                            TestTypeMirror.primitiveInt(),
                            TestNameUtil.createReal("intColumn"),
                            INT_TOO_LOW,
                            new RuntimeException("int value for intColumn invalid default: " + INT_TOO_LOW)
                    },
                    {   // 26: int primitive outside maximum bounds
                            TestTypeMirror.primitiveInt(),
                            TestNameUtil.createReal("intColumn"),
                            INT_TOO_HIGH,
                            new RuntimeException("int value for intColumn invalid default: " + INT_TOO_HIGH)
                    },
                    {   // 27: int wrapper with word default
                            TestTypeMirror.intWrapper(),
                            TestNameUtil.createReal("intWrapperColumn"),
                            "Seven",
                            new RuntimeException(Integer.class.getName() + " value for intWrapperColumn invalid default: Seven")
                    },
                    {   // 28: int wrapper outside minimum bounds
                            TestTypeMirror.intWrapper(),
                            TestNameUtil.createReal("intWrapperColumn"),
                            INT_TOO_LOW,
                            new RuntimeException(Integer.class.getName() + " value for intWrapperColumn invalid default: " + INT_TOO_LOW)
                    },
                    {   // 29: int wrapper outside maximum bounds
                            TestTypeMirror.intWrapper(),
                            TestNameUtil.createReal("intWrapperColumn"),
                            INT_TOO_HIGH,
                            new RuntimeException(Integer.class.getName() + " value for intWrapperColumn invalid default: " + INT_TOO_HIGH)
                    },
                    {   // 30: long primitive with word default
                            TestTypeMirror.primitiveLong(),
                            TestNameUtil.createReal("longColumn"),
                            "Seven",
                            new RuntimeException("long value for longColumn invalid default: Seven")
                    },
                    {   // 31: long primitive outside minimum bounds
                            TestTypeMirror.primitiveLong(),
                            TestNameUtil.createReal("longColumn"),
                            LONG_TOO_LOW,
                            new RuntimeException("long value for longColumn invalid default: " + LONG_TOO_LOW)
                    },
                    {   // 32: long primitive outside maximum bounds
                            TestTypeMirror.primitiveLong(),
                            TestNameUtil.createReal("longColumn"),
                            LONG_TOO_HIGH,
                            new RuntimeException("long value for longColumn invalid default: " + LONG_TOO_HIGH)
                    },
                    {   // 33: long wrapper with word default
                            TestTypeMirror.longWrapper(),
                            TestNameUtil.createReal("longWrapperColumn"),
                            "Seven",
                            new RuntimeException(Long.class.getName() + " value for longWrapperColumn invalid default: Seven")
                    },
                    {   // 34: long wrapper outside minimum bounds
                            TestTypeMirror.longWrapper(),
                            TestNameUtil.createReal("longWrapperColumn"),
                            LONG_TOO_LOW,
                            new RuntimeException(Long.class.getName() + " value for longWrapperColumn invalid default: " + LONG_TOO_LOW)
                    },
                    {   // 35: long wrapper outside maximum bounds
                            TestTypeMirror.longWrapper(),
                            TestNameUtil.createReal("longWrapperColumn"),
                            LONG_TOO_HIGH,
                            new RuntimeException(Long.class.getName() + " value for longWrapperColumn invalid default: " + LONG_TOO_HIGH)
                    },
                    {   // 36: short primitive with word default
                            TestTypeMirror.primitiveShort(),
                            TestNameUtil.createReal("shortColumn"),
                            "Seven",
                            new RuntimeException("short value for shortColumn invalid default: Seven")
                    },
                    {   // 37: short primitive outside minimum bounds
                            TestTypeMirror.primitiveShort(),
                            TestNameUtil.createReal("shortColumn"),
                            SHORT_TOO_LOW,
                            new RuntimeException("short value for shortColumn invalid default: " + SHORT_TOO_LOW)
                    },
                    {   // 38: short primitive outside maximum bounds
                            TestTypeMirror.primitiveShort(),
                            TestNameUtil.createReal("shortColumn"),
                            SHORT_TOO_HIGH,
                            new RuntimeException("short value for shortColumn invalid default: " + SHORT_TOO_HIGH)
                    },
                    {   // 39: short wrapper with word default
                            TestTypeMirror.shortWrapper(),
                            TestNameUtil.createReal("shortWrapperColumn"),
                            "Seven",
                            new RuntimeException(Short.class.getName() + " value for shortWrapperColumn invalid default: Seven")
                    },
                    {   // 40: short wrapper outside minimum bounds
                            TestTypeMirror.shortWrapper(),
                            TestNameUtil.createReal("shortWrapperColumn"),
                            SHORT_TOO_LOW,
                            new RuntimeException(Short.class.getName() + " value for shortWrapperColumn invalid default: " + SHORT_TOO_LOW)
                    },
                    {   // 41: short wrapper outside maximum bounds
                            TestTypeMirror.shortWrapper(),
                            TestNameUtil.createReal("shortWrapperColumn"),
                            SHORT_TOO_HIGH,
                            new RuntimeException(Short.class.getName() + " value for shortWrapperColumn invalid default: " + SHORT_TOO_HIGH)
                    }
            });
        }

        @Test
        public void shouldCorrectlyInterpretDefaultValue() {
            try {
                InfoTranslator.toColumnInfoBuilder(input).build();
                fail("Should have thrown exception: " + expected);
            } catch (Exception actual) {
                assertEquals(expected.getClass(), actual.getClass());
                assertEquals(expected.getMessage(), actual.getMessage());
            }
        }

        @Override
        void setUpMockElements(Elements mockElements) {
            when(mockElements.getElementValuesWithDefaults(any(AnnotationMirror.class)))
                    .thenAnswer((Answer<Map<? extends ExecutableElement, ? extends AnnotationValue>>) invocation -> {
                        return input.getAnnotationMirrors().get(0).getElementValues();
                    });
        }
    }

    @RunWith(Parameterized.class)
    public static class ColumnNameOf extends UsingAnnotationTranslatorFactory<ExecutableElement, String> {

        private final String desc;

        public ColumnNameOf(String desc, ExecutableElement input, String expected) {
            super(input, expected);
            this.desc = desc;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {
                            "00: without @FSColumn annotation, should name the column with the method name",
                            TestExecutableElement.returningString("methodName"),
                            "methodName"
                    },
                    {
                            "01: with @FSColumn annotation, should name the column with the value of @FSColumn",
                            TestExecutableElement.returningString(
                                    "methodName",
                                    TestAnnotationMirror.builder()
                                            .setAnnotationType(TestDeclaredType.of(FSColumn.class))
                                            .setElementValues(TestAnnotationMirror.singletonElementValues(
                                                    TestExecutableElement.returningString("value"),
                                                    TestAnnotationValueUtil.createReal("string_column")
                                            ))
                                            .build()
                            ),
                            "string_column"
                    }
            });
        }

        @Test
        public void shouldCorrectlyDetermineColumnNameFromExecutableElement() {
            assertEquals(desc, expected, InfoTranslator.columnNameOf(input));
        }

        @Override
        void setUpMockElements(Elements mockElements) {
            when(mockElements.getElementValuesWithDefaults(any(AnnotationMirror.class)))
                    .thenAnswer((Answer<Map<? extends ExecutableElement, ? extends AnnotationValue>>) invocation -> {
                        return input.getAnnotationMirrors().get(0).getElementValues();
                    });
        }
    }

    @RunWith(Parameterized.class)
    public static class TableNameOf extends InfoTranslatorTest<TypeElement, String> {

        private final String desc;

        public TableNameOf(String desc, TypeElement input, String expected) {
            super(input, expected);
            this.desc = desc;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {
                            "00: without @FSTable annotation should return the name of the class",
                            TestTypeElement.builder()
                                    .setSimpleName(TestNameUtil.createReal("MyTable"))
                                    .build(),
                            "MyTable"
                    },
                    {
                            "01: with @FSColumn annotation, should name the column with the value of @FSColumn",
                            TestTypeElement.builder()
                                    .setSimpleName(TestNameUtil.createReal("MyTable"))
                                    .setFakeAnnotations(Arrays.asList(FSTestTypesUtil.createFSTable("table_name")))
                                    .build(),
                            "table_name"
                    }
            });
        }

        @Test
        public void shouldCorrectlyDetermineColumnNameFromExecutableElement() {
            assertEquals(desc, expected, InfoTranslator.tableNameOf(input));
        }
    }

    @RunWith(Parameterized.class)
    public static class DocStoreParameterizationOf extends InfoTranslatorTest<TypeElement, String> {

        private final String desc;

        public DocStoreParameterizationOf(String desc, TypeElement input, String expected) {
            super(input, expected);
            this.desc = desc;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {
                            "00: Raw FSDocStoreTable extension should have object as base class",
                            TestTypeElement.withRawDocStoreInterface("MyDocStoreTable").build(),
                            Object.class.getSimpleName()
                    },
                    {
                            "01: Parameterized FSDocStoreTable extension should correctly determine base class based upon parameterization",
                            TestTypeElement.withDocStoreInterface("MyDocStoreTable", Number.class.getName())
                                    .setSimpleName(TestNameUtil.createReal("MyTable"))
                                    .setFakeAnnotations(Arrays.asList(FSTestTypesUtil.createFSTable("table_name")))
                                    .build(),
                            Number.class.getName()
                    },
                    {
                            "02: Non doc store table should return null",
                            TestTypeElement.builder()
                                    .setInterfaces(Collections.emptyList())
                                    .build(),
                            null
                    }
            });
        }

        @Test
        public void shouldCorrectlyDetermineColumnNameFromExecutableElement() {
            assertEquals(desc, expected, InfoTranslator.docStoreParameterizationOf(input));
        }
    }

    @RunWith(Parameterized.class)
    public static class PrimaryKeyFrom extends InfoTranslatorTest<TypeElement, Set<String>> {

        private final String desc;
        private final String expectedOnConflict;

        public PrimaryKeyFrom(String desc, TypeElement input, Set<String> expected, String expectedOnConflict) {
            super(input, expected);
            this.desc = desc;
            this.expectedOnConflict = expectedOnConflict;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {
                            "00: Without @PrimaryKey annotation, should return [\"_id\"] as a singleton set; onConflict should be empty",
                            TestTypeElement.builder()
                                    .setFakeAnnotations(Collections.emptyList())
                                    .build(),
                            Collections.singleton("_id"),
                            ""
                    },
                    {
                            "01: with @PrimaryKey annotation, should return the full value as a set; should determine onConflict",
                            TestTypeElement.builder()
                                    .setFakeAnnotations(Collections.singletonList(createFSPrimaryKey("ABORT", "column1", "column2")))
                                    .build(),
                            new HashSet<>(Arrays.asList("column1", "column2")),
                            "ABORT"
                    }
            });
        }

        @Test
        public void shouldCorrectlyDeterminePrimaryKey() {
            Set<String> actual = InfoTranslator.primaryKeyFrom(input);
            String failureMessage = String.format("%s: expected %s, but was %s", desc, expected, actual);
            for (String primaryKeyComponent : actual) {
                assertTrue(failureMessage, actual.contains(primaryKeyComponent));
            }
            assertEquals(failureMessage, expected.size(), actual.size());
        }

        @Test
        public void shouldCorrectlyDeterminePrimaryKeyOnConflict() {
            assertEquals(desc, expectedOnConflict, InfoTranslator.primaryKeyOnConflictFrom(input));
        }
    }

    @RunWith(Parameterized.class)
    public static class StaticDataAssetOf extends InfoTranslatorTest<TypeElement, String> {

        private final String desc;

        public StaticDataAssetOf(String desc, TypeElement input, String expected) {
            super(input, expected);
            this.desc = desc;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {
                            "00: Without @FSStaticData annotation, should be null",
                            TestTypeElement.builder()
                                    .setFakeAnnotations(Collections.emptyList())
                                    .build(),
                            null
                    },
                    {
                            "01: with @FSStaticData annotation, should return the name of the static data asset",
                            TestTypeElement.builder()
                                    .setFakeAnnotations(Collections.singletonList(createFSStaticData("assetName")))
                                    .build(),
                            "assetName"
                    }
            });
        }

        @Test
        public void shouldCorrectlyDetermineStaticDataAsset() {
            assertEquals(desc, expected, InfoTranslator.staticDataAssetOf(input));
        }
    }

    @RunWith(Parameterized.class)
    public static class ContainsForeignKey extends InfoTranslatorTest<ExecutableElement, Boolean> {

        private final String desc;

        public ContainsForeignKey(String desc, ExecutableElement input, Boolean expected) {
            super(input, expected);
            this.desc = desc;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {
                            "00: Without @FSForeignKey and without @ForeignKey annotation, should return false",
                            TestExecutableElement.builder()
                                    .setSimpleName(TestNameUtil.createReal("someName"))
                                    .setReturnType(TestTypeMirror.primitiveInt())
                                    .setFakedAnnotations(Collections.emptyList())
                                    .build(),
                            false
                    },
                    {
                            "01: Without @FSForeignKey and with @ForeignKey annotation, should return true",
                            TestExecutableElement.builder()
                                    .setSimpleName(TestNameUtil.createReal("someName"))
                                    .setReturnType(TestTypeMirror.primitiveInt())
                                    .setFakedAnnotations(Collections.singletonList(createLegacyForeignKey(
                                            FSGetApi.class,
                                            "_id",
                                            ForeignKey.ChangeAction.SET_DEFAULT,
                                            ForeignKey.ChangeAction.SET_NULL
                                    )))
                                    .build(),
                            true
                    },
                    {
                            "02: with @FSForeignKey annotation, without @ForeignKey should return true",
                            TestExecutableElement.builder()
                                    .setSimpleName(TestNameUtil.createReal("someName"))
                                    .setReturnType(TestTypeMirror.primitiveInt())
                                    .setFakedAnnotations(Collections.singletonList(createFSForeignKey(
                                            FSGetApi.class,
                                            "_id",
                                            "CASCADE",
                                            "CASCADE",
                                            UUID.randomUUID().toString()
                                    ))).build(),
                            true
                    }
            });
        }

        @Test
        public void shouldCorrectlyDetermineForeignKey() {
            assertEquals(desc, expected, InfoTranslator.containsForeignKey(input));
        }

        @Test
        public void shouldNotThrowWhenValidating() {
            InfoTranslator.validateForeignKeyDeclaration(input);
        }
    }

    public static class ValidateForeignKeyDeclaration {

        @Test(expected = IllegalStateException.class)
        public void shouldThrowWhenBothUsed() {
            ExecutableElement testExecutableElement = TestExecutableElement.builder()
                    .setFakedAnnotations(Arrays.asList(createFSForeignKey(
                            FSGetApi.class,
                            "_id",
                            "CASCADE",
                            "CASCADE",
                            ""
                    ), createLegacyForeignKey(
                            FSGetApi.class,
                            "another_column",
                            ForeignKey.ChangeAction.CASCADE,
                            ForeignKey.ChangeAction.CASCADE
                    ))).setReturnType(TestTypeMirror.primitiveInt())
                    .setSimpleName(TestNameUtil.createReal("intColumn"))
                    .build();
            InfoTranslator.validateForeignKeyDeclaration(testExecutableElement);
        }
    }

    @RunWith(Parameterized.class)
    public static class ContainsFSIndex extends InfoTranslatorTest<ExecutableElement, Boolean> {

        private final String desc;

        public ContainsFSIndex(String desc, ExecutableElement input, Boolean expected) {
            super(input, expected);
            this.desc = desc;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {
                            "00: Without @FSIndex and without @Index annotation, should return false",
                            TestExecutableElement.builder()
                                    .setSimpleName(TestNameUtil.createReal("someName"))
                                    .setReturnType(TestTypeMirror.primitiveInt())
                                    .setFakedAnnotations(Collections.emptyList())
                                    .build(),
                            false
                    },
                    {
                            "01: Without @FSIndex and with @Index annotation, should return true",
                            TestExecutableElement.builder()
                                    .setSimpleName(TestNameUtil.createReal("someName"))
                                    .setReturnType(TestTypeMirror.primitiveInt())
                                    .setFakedAnnotations(Collections.singletonList(createLegacyIndex(false)))
                                    .build(),
                            true
                    },
                    {
                            "02: with @FSIndex annotation, without @Index should return true",
                            TestExecutableElement.builder()
                                    .setSimpleName(TestNameUtil.createReal("someName"))
                                    .setReturnType(TestTypeMirror.primitiveInt())
                                    .setFakedAnnotations(Collections.singletonList(createFSIndex(false, "DESC", "composite")))
                                    .build(),
                            true
                    }
            });
        }

        @Test
        public void shouldCorrectlyDetermineIndex() {
            assertEquals(desc, expected, InfoTranslator.containsIndex(input));
        }

        @Test
        public void shouldNotThrowWhenValidating() {
            InfoTranslator.validateIndexDeclaration(input);
        }
    }

    public static class ValidateIndexDeclaration {

        @Test(expected = IllegalStateException.class)
        public void shouldThrowWhenBothUsed() {
            ExecutableElement testExecutableElement = TestExecutableElement.builder()
                    .setFakedAnnotations(Arrays.asList(
                            createFSIndex(false, "DESC", "composite"),
                            createLegacyIndex(false)
                    )).setReturnType(TestTypeMirror.primitiveInt())
                    .setSimpleName(TestNameUtil.createReal("intColumn"))
                    .build();
            InfoTranslator.validateIndexDeclaration(testExecutableElement);
        }
    }

    @RunWith(Parameterized.class)
    public static class IndexCompositeIdOf extends InfoTranslatorTest<ExecutableElement, String> {

        private final String desc;

        public IndexCompositeIdOf(String desc, ExecutableElement input, String expected) {
            super(input, expected);
            this.desc = desc;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {
                            "00: Without @FSIndex and @Index, should reaturn null",
                            TestExecutableElement.builder()
                                    .setSimpleName(TestNameUtil.createReal("someName"))
                                    .setReturnType(TestTypeMirror.primitiveInt())
                                    .setFakedAnnotations(Collections.emptyList())
                                    .build(),
                            null
                    },
                    {
                            "01: Without @FSIndex and with @Index annotation, should return empty string",
                            TestExecutableElement.builder()
                                    .setSimpleName(TestNameUtil.createReal("someName"))
                                    .setReturnType(TestTypeMirror.primitiveInt())
                                    .setFakedAnnotations(Collections.singletonList(createLegacyIndex(false)))
                                    .build(),
                            ""
                    },
                    {
                            "02: with @FSIndex annotation, without @Index should return the defined compositeId",
                            TestExecutableElement.builder()
                                    .setSimpleName(TestNameUtil.createReal("someName"))
                                    .setReturnType(TestTypeMirror.primitiveInt())
                                    .setFakedAnnotations(Collections.singletonList(createFSIndex(false, "DESC", "composite")))
                                    .build(),
                            "composite"
                    }
            });
        }

        @Test
        public void shouldCorrectlyDetermineCompositeIndexId() {
            assertEquals(desc, expected, InfoTranslator.indexCompositeIdOf(input));
        }
    }

    @RunWith(Parameterized.class)
    public static class TableIndexInfoOf extends InfoTranslatorTest<ExecutableElement, TableIndexInfo> {

        private final String desc;

        public TableIndexInfoOf(String desc, ExecutableElement input, TableIndexInfo expected) {
            super(input, expected);
            this.desc = desc;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {
                            "00: Without @FSIndex and @Index, should reaturn null",
                            TestExecutableElement.builder()
                                    .setSimpleName(TestNameUtil.createReal("someName"))
                                    .setReturnType(TestTypeMirror.primitiveInt())
                                    .setFakedAnnotations(Collections.emptyList())
                                    .build(),
                            null
                    },
                    {
                            "01: Without @FSIndex and with @Index annotation, should return TableIndexInfo with correct unique value",
                            TestExecutableElement.builder()
                                    .setAnnotationMirrors(Collections.emptyList())
                                    .setSimpleName(TestNameUtil.createReal("someName"))
                                    .setReturnType(TestTypeMirror.primitiveInt())
                                    .setFakedAnnotations(Collections.singletonList(createLegacyIndex(true)))
                                    .build(),
                            TableIndexInfo.create(true, Collections.singletonList("someName"), Collections.singletonList(""))
                    },
                    {
                            "02: with @FSIndex annotation, without @Index should return TableIndexInfo with correct sort order and unique value",
                            TestExecutableElement.builder()
                                    .setAnnotationMirrors(Collections.emptyList())
                                    .setSimpleName(TestNameUtil.createReal("someName"))
                                    .setReturnType(TestTypeMirror.primitiveInt())
                                    .setFakedAnnotations(Collections.singletonList(createFSIndex(false, "ASC", "composite")))
                                    .build(),
                            TableIndexInfo.create(false, Collections.singletonList("someName"), Collections.singletonList("ASC"))
                    }
            });
        }

        @Test
        public void shouldCorrectlyDetermineCompositeIndexId() {
            assertEquals(desc, expected, InfoTranslator.tableIndexInfoOf(input));
        }
    }
}
