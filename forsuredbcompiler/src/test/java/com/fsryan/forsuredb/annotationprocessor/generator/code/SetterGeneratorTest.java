package com.fsryan.forsuredb.annotationprocessor.generator.code;

import com.fsryan.forsuredb.TestData;
import com.fsryan.forsuredb.annotationprocessor.generator.BaseGenerator;
import com.fsryan.forsuredb.annotationprocessor.generator.GeneratorTest;
import com.fsryan.forsuredb.info.TableInfo;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.tools.JavaFileObject;
import java.util.Arrays;

@RunWith(Parameterized.class)
public class SetterGeneratorTest extends GeneratorTest<JavaFileObject> {

    private SetterGenerator generatorUnderTest;
    private TableInfo table;

    public SetterGeneratorTest(String expectedCode, TableInfo table, Class<?> resultParameter) {
        super(expectedCode, resultParameter.getName(), true);
        this.table = table;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                        TestData.resourceText("example_relational_table_setter.txt"),
                        TestData.targetTableWithChildForeignKey(),
                        SetterGeneratorTest.class
                },
                // TODO: test SetterGenerator.DocStore
        });
    }

    @Before
    public void setUp() {
        generatorUnderTest = SetterGenerator.getFor(mockProcessingEnv, table);
    }

    @Override
    protected BaseGenerator<JavaFileObject> generatorUnderTest() {
        return generatorUnderTest;
    }
}
