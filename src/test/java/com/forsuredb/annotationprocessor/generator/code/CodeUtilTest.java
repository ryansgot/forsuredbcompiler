package com.forsuredb.annotationprocessor.generator.code;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

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
}
