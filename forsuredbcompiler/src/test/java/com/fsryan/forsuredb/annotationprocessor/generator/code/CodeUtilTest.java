package com.fsryan.forsuredb.annotationprocessor.generator.code;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.Type;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class CodeUtilTest {

    @RunWith(Parameterized.class)
    public static class CodeUtilSnakeToCamelTest {

        private String inputString;
        private String expectedNonCapitalizedOutput;
        private String expectedCapitalizedOutput;

        public CodeUtilSnakeToCamelTest(String inputString, String expectedNonCapitalizedOutput, String expectedCapitalizedOutput) {
            this.inputString = inputString;
            this.expectedNonCapitalizedOutput = expectedNonCapitalizedOutput;
            this.expectedCapitalizedOutput = expectedCapitalizedOutput;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    { null, null, null },
                    { "", "", "" },
                    { "_id", "id", "Id"},
                    { "snake_case_test", "snakeCaseTest", "SnakeCaseTest" },
                    { "goo", "goo", "Goo" }
            });
        }

        @Test
        public void shouldMatchExpectedNonCapitalized() {
            assertEquals(expectedNonCapitalizedOutput, CodeUtil.snakeToCamel(inputString));
        }

        @Test
        public void shouldMatchExpectedCapitalized() {
            assertEquals(expectedCapitalizedOutput, CodeUtil.snakeToCamel(inputString, true));
        }
    }

    @RunWith(Parameterized.class)
    public static class PackageAndClassNameMethodsTest {

        private String inputString;
        private String expectedSimpleClassName;
        private String expectedPackageName;

        public PackageAndClassNameMethodsTest(String inputString, String expectedSimpleClassName, String expectedPackageName) {
            this.inputString = inputString;
            this.expectedSimpleClassName = expectedSimpleClassName;
            this.expectedPackageName = expectedPackageName;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    { null, Object.class.getSimpleName(), "java.lang" },
                    { "", Object.class.getSimpleName(), "java.lang" },
                    { String.class.getName(), "String", "java.lang"},
                    { "goo", "goo", "" }
            });
        }

        @Test
        public void shouldMatchExpectedSimpleClassName() {
            assertEquals(expectedSimpleClassName, CodeUtil.simpleClassNameFrom(inputString));
        }

        @Test
        public void shouldMatchExpectedPackageName() {
            assertEquals(expectedPackageName, CodeUtil.packageNameFrom(inputString));
        }
    }

    @RunWith(Parameterized.class)
    public static class TypeFromName {

        private final String fqTypeName;
        private final Type nonArrayType;
        private final Type arrayType;

        public TypeFromName(String fqTypeName, Type nonArrayType, Type arrayType) {
            this.fqTypeName = fqTypeName;
            this.nonArrayType = nonArrayType;
            this.arrayType = arrayType;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    { null, Object.class, Object[].class },
                    { "", Object.class, Object[].class },
                    { "non.existent.class.Name", Object.class, Object[].class },
                    { "java.lang.String", String.class, String[].class },
                    { "char", char.class, char[].class },
                    { "boolean", boolean.class, boolean[].class },
                    { "byte", byte.class, byte[].class },
                    { "byte[]", byte[].class, byte[][].class },
                    { "short", short.class, short[].class },
                    { "int", int.class, int[].class },
                    { "long", long.class, long[].class },
                    { "float", float.class, float[].class },
                    { "double", double.class, double[].class },
            });
        }

        @Test
        public void shouldMatchExpectedNonArrayType() {
            assertEquals(nonArrayType, CodeUtil.typeFromName(fqTypeName));
            assertEquals(nonArrayType, CodeUtil.typeFromName(fqTypeName, false));
        }

        @Test
        public void shouldMatchExpectedPackageName() {
            assertEquals(arrayType, CodeUtil.arrayTypeFromName(fqTypeName));
            assertEquals(arrayType, CodeUtil.typeFromName(fqTypeName, true));
        }
    }

    @RunWith(Parameterized.class)
    public static class PrimitiveToWrapperName {

        private final String fqTypeName;
        private final String expectedOutputName;

        public PrimitiveToWrapperName(String fqTypeName, String expectedOutputName) {
            this.fqTypeName = fqTypeName;
            this.expectedOutputName = expectedOutputName;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    { null, Object.class.getSimpleName() },
                    { "", Object.class.getSimpleName() },
                    { "non.existent.class.Name", "Name" },
                    { "java.lang.String", String.class.getSimpleName() },
                    { "char", Character.class.getSimpleName() },
                    { "boolean", Boolean.class.getSimpleName() },
                    { "byte", Byte.class.getSimpleName() },
                    { "byte[]", byte[].class.getSimpleName() },
                    { "short", Short.class.getSimpleName() },
                    { "int", Integer.class.getSimpleName() },
                    { "long", Long.class.getSimpleName() },
                    { "float", Float.class.getSimpleName() },
                    { "double", Double.class.getSimpleName() },
            });
        }

        @Test
        public void shouldMatchExpectedWrapperName() {
            assertEquals(expectedOutputName, CodeUtil.primitiveToWrapperName(fqTypeName));
        }
    }
}
