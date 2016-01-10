package com.forsuredb.annotationprocessor.generator.code;

import com.forsuredb.annotationprocessor.generator.GeneratorTest;
import com.forsuredb.annotationprocessor.generator.NewBaseGenerator;
import com.forsuredb.annotationprocessor.info.TableInfo;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.tools.JavaFileObject;
import java.util.Arrays;

import static com.forsuredb.TestData.resourceText;
import static com.forsuredb.TestData.testTable;

@RunWith(Parameterized.class)
public class FinderGeneratorTest extends GeneratorTest<JavaFileObject> {

    private NewFinderGenerator gut;

    private TableInfo inputTable;

    public FinderGeneratorTest(String classCode, TableInfo inputTable) {
        super(classCode);
        this.inputTable = inputTable;
        System.setProperty("resultParameter", FinderGeneratorTest.class.getName());
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                        resourceText("example_finder.txt"),
                        testTable()
                }
        });
    }

    @Before
    public void setUp() {
        gut = new NewFinderGenerator(mockProcessingEnv, inputTable);
    }

    @Override
    protected NewBaseGenerator<JavaFileObject> generatorUnderTest() {
        return gut;
    }
}
