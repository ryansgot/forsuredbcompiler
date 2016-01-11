package com.forsuredb.annotationprocessor.generator;

import org.junit.Test;
import org.mockito.Mockito;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;

import static org.junit.Assert.assertEquals;

public abstract class GeneratorTest<F extends FileObject> {

    private final String classCode;
    protected ProcessingEnvironment mockProcessingEnv;

    public GeneratorTest(String classCode) {
        this.classCode = classCode;
        mockProcessingEnv = Mockito.mock(ProcessingEnvironment.class);
    }

    @Test
    public void shouldMatchExpectedClassCode() {
        assertEquals(classCode, generatorUnderTest().getCode());
    }

    protected abstract BaseGenerator<F> generatorUnderTest();
}
