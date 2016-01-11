package com.forsuredb.annotationprocessor.generator.code;

import com.forsuredb.annotationprocessor.generator.GeneratorTest;
import com.forsuredb.annotationprocessor.generator.NewBaseGenerator;
import com.forsuredb.annotationprocessor.info.TableInfo;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.tools.JavaFileObject;
import java.util.Arrays;

import static com.forsuredb.TestData.*;

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
                        resourceText("example_setter_java.txt"),
                        targetTableWithChildForeignKey(),
                        SetterGeneratorTest.class
                }
        });
    }

    @Before
    public void setUp() {
        gut = new SetterGenerator(mockProcessingEnv, table);
    }

    @Override
    protected NewBaseGenerator<JavaFileObject> generatorUnderTest() {
        return gut;
    }
}
