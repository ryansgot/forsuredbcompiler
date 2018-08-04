package com.fsryan.forsuredb.annotationprocessor.generator.code;

import com.fsryan.forsuredb.annotationprocessor.TableContext;
import com.fsryan.forsuredb.annotationprocessor.generator.GeneratorTest;
import com.fsryan.forsuredb.annotationprocessor.generator.BaseGenerator;
import com.fsryan.forsuredb.TestData;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.tools.JavaFileObject;
import java.util.Arrays;

@RunWith(Parameterized.class)
public class ForSureGeneratorTest extends GeneratorTest<JavaFileObject> {

    private ForSureGenerator gut;

    private String packageName;
    private TableContext inputTargetContext;

    public ForSureGeneratorTest(String classCode, String packageName, TableContext inputTargetContext) {
        super(
                classCode,
                ForSureGeneratorTest.class.getName(),
                true
        );
        this.packageName = packageName;
        this.inputTargetContext = inputTargetContext;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                        TestData.resourceText("example_forsure.txt"),
                        "com.fsryan.annotationprocessor.generator.code",
                        TestData.testTargetContext()
                }
        });
    }

    @Before
    public void setUp() {
        gut = new ForSureGenerator(mockProcessingEnv, packageName, inputTargetContext);
    }

    @Override
    protected BaseGenerator<JavaFileObject> generatorUnderTest() {
        return gut;
    }
}
