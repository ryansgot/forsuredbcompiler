package com.fsryan.forsuredb.annotationprocessor.generator.code;

import com.fsryan.forsuredb.annotationprocessor.generator.BaseGenerator;
import com.fsryan.forsuredb.annotationprocessor.generator.GeneratorTest;
import com.fsryan.forsuredb.info.TableInfo;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.tools.JavaFileObject;
import java.util.Arrays;

import static com.fsryan.forsuredb.TestData.targetTableWithChildForeignKey;
import static com.fsryan.forsuredb.test.tools.ResourceUtil.resourceText;

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
                        resourceText("example_getter.txt"),
                        targetTableWithChildForeignKey()
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
