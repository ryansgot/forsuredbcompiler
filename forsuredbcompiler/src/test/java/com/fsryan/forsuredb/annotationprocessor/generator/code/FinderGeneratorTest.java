package com.fsryan.forsuredb.annotationprocessor.generator.code;

import com.fsryan.forsuredb.annotationprocessor.generator.GeneratorTest;
import com.fsryan.forsuredb.annotationprocessor.generator.BaseGenerator;
import com.fsryan.forsuredb.info.TableInfo;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.tools.JavaFileObject;
import java.util.Arrays;

import static com.fsryan.forsuredb.TestData.targetTableWithChildForeignKey;
import static com.fsryan.forsuredb.test.tools.ResourceUtil.resourceText;

@RunWith(Parameterized.class)
public class FinderGeneratorTest extends GeneratorTest<JavaFileObject> {

    private FinderGenerator gut;

    private TableInfo inputTable;

    public FinderGeneratorTest(String classCode, TableInfo inputTable) {
        super(
                classCode,
                FinderGeneratorTest.class.getName(),
                true
        );
        this.inputTable = inputTable;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                        resourceText("example_finder.txt"),
                        targetTableWithChildForeignKey()
                },
                // TODO: test DocStoreFinder generation
        });
    }

    @Before
    public void setUp() {
        gut = new FinderGenerator(mockProcessingEnv, inputTable);
    }

    @Override
    protected BaseGenerator<JavaFileObject> generatorUnderTest() {
        return gut;
    }
}
