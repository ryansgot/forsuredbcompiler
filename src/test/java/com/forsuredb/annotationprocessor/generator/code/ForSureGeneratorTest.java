package com.forsuredb.annotationprocessor.generator.code;

import com.forsuredb.annotationprocessor.TableContext;
import com.forsuredb.annotationprocessor.generator.GeneratorTest;
import com.forsuredb.annotationprocessor.generator.NewBaseGenerator;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.tools.JavaFileObject;
import java.util.Arrays;

import static com.forsuredb.TestData.*;

@RunWith(Parameterized.class)
public class ForSureGeneratorTest extends GeneratorTest<JavaFileObject> {

    private ForSureGenerator gut;

    private String packageName;
    private TableContext inputTargetContext;

    public ForSureGeneratorTest(String classCode, String packageName, TableContext inputTargetContext) {
        super(classCode);
        this.packageName = packageName;
        this.inputTargetContext = inputTargetContext;
        System.setProperty("resultParameter", ForSureGeneratorTest.class.getName());
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                        resourceText("example_forsure.txt"),
                        "com.forsuredb.annotationprocessor.generator.code",
                        testTargetContext()
                }
        });
    }

    @Before
    public void setUp() {
        gut = new ForSureGenerator(mockProcessingEnv, packageName, inputTargetContext);
    }

    @Override
    protected NewBaseGenerator<JavaFileObject> generatorUnderTest() {
        return gut;
    }
}
