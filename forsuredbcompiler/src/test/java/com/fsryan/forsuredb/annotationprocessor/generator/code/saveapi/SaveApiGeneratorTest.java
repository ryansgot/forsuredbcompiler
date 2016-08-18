package com.fsryan.forsuredb.annotationprocessor.generator.code.saveapi;

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
public class SaveApiGeneratorTest extends GeneratorTest<JavaFileObject> {

    private SaveApiGenerator gut;
    private TableInfo table;

    public SaveApiGeneratorTest(String expectedCode, TableInfo table, Class<?> resultParameter) {
        super(expectedCode);
        this.table = table;
        System.setProperty("resultParameter", resultParameter.getName());
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                        TestData.resourceText("example_relational_save_api.txt"),
                        TestData.targetTableWithChildForeignKey(),
                        SaveApiGeneratorTest.class
                },
                // TODO: test DocStoreSaveApiGenerator
        });
    }

    @Before
    public void setUp() {
        gut = SaveApiGenerator.getFor(mockProcessingEnv, table);
    }

    @Override
    protected BaseGenerator<JavaFileObject> generatorUnderTest() {
        return gut;
    }
}
