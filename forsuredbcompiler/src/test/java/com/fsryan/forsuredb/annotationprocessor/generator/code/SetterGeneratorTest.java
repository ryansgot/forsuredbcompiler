package com.fsryan.forsuredb.annotationprocessor.generator.code;

import com.fsryan.forsuredb.annotationprocessor.generator.GeneratorTest;
import com.fsryan.forsuredb.annotationprocessor.generator.BaseGenerator;
import com.fsryan.forsuredb.api.info.TableInfo;
import com.fsryan.forsuredb.TestData;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.tools.JavaFileObject;
import java.util.Arrays;

@RunWith(Parameterized.class)
public class SetterGeneratorTest extends GeneratorTest<JavaFileObject> {

    private SetterGenerator gut;
    private TableInfo table;

    public SetterGeneratorTest(String expectedCode, TableInfo table, Class<?> resultParameter) {
        super(expectedCode);
        this.table = table;
        System.setProperty("resultParameter", resultParameter.getName());
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                        TestData.resourceText("example_setter_java.txt"),
                        TestData.targetTableWithChildForeignKey(),
                        SetterGeneratorTest.class
                }
        });
    }

    @Before
    public void setUp() {
        gut = new SetterGenerator(mockProcessingEnv, table);
    }

    @Override
    protected BaseGenerator<JavaFileObject> generatorUnderTest() {
        return gut;
    }
}
