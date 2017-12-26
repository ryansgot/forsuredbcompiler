package com.fsryan.forsuredb.annotationprocessor.generator;

import com.fsryan.forsuredb.annotationprocessor.util.PropertyRetriever;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class GeneratorTest<F extends FileObject> {

    private final String classCode;
    protected ProcessingEnvironment mockProcessingEnv;

    private final String resultParameter;
    private final boolean generatedAnnotation;

    public GeneratorTest(String classCode, String resultParameter, boolean generatedAnnotation) {
        this.classCode = classCode;
        mockProcessingEnv = mock(ProcessingEnvironment.class);

        this.resultParameter = resultParameter;
        this.generatedAnnotation = generatedAnnotation;
    }

    @Before
    public void setUpOptions() {
        Map<String, String> options = new HashMap<>();
        options.put("forsuredb.resultParameter", resultParameter);
        options.put("forsuredb.addGeneratedAnnotation", Boolean.toString(generatedAnnotation));
        resetPropertyRetrieverOptions(options);
    }

    @Test
    public void shouldMatchExpectedClassCode() {
        assertEquals(classCode, generatorUnderTest().getCode());
    }

    protected abstract BaseGenerator<F> generatorUnderTest();

    private static void resetPropertyRetrieverOptions(Map<String, String> options) {
        try {
            PropertyRetriever pr = PropertyRetriever.properties();
            Field optionsField = PropertyRetriever.class.getDeclaredField("options");
            optionsField.setAccessible(true);
            Map<String, String> prOptions = (Map<String, String>) optionsField.get(pr);
            prOptions.clear();
            prOptions.putAll(options);
        } catch (Exception e) {
            // in this case, the PropertyRetriever has not yet been initialized
            ProcessingEnvironment mockProcessingEnv = mock(ProcessingEnvironment.class);
            when(mockProcessingEnv.getOptions()).thenReturn(options);
            PropertyRetriever.init(mockProcessingEnv);
        }
    }
}
