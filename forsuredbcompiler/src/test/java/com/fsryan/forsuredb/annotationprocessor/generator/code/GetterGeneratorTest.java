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
public class GetterGeneratorTest extends GeneratorTest<JavaFileObject> {

    private GetterGenerator generatorUnderTest;

    private TableInfo inputTable;

    public GetterGeneratorTest(String classCode, TableInfo inputTable) {
        super(
                classCode,
                GetterGeneratorTest.class.getName(),
                true
        );
        this.inputTable = inputTable;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                        TestData.resourceText("example_getter.txt"),
                        TestData.targetTableWithChildForeignKey()
                },
        });
    }

    @Before
    public void setUp() {
        generatorUnderTest = GetterGenerator.getFor(mockProcessingEnv, inputTable);
    }

    @Override
    protected BaseGenerator<JavaFileObject> generatorUnderTest() {
        return generatorUnderTest;
    }
}
