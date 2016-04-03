package com.forsuredb.annotationprocessor.generator.code;

import com.forsuredb.annotationprocessor.generator.BaseGenerator;
import com.forsuredb.annotationprocessor.generator.GeneratorTest;
import com.forsuredb.annotationprocessor.info.TableInfo;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.tools.JavaFileObject;
import java.util.Arrays;

import static com.forsuredb.TestData.resourceText;
import static com.forsuredb.TestData.targetTableWithChildForeignKey;

@RunWith(Parameterized.class)
public class OrderByGeneratorTest extends GeneratorTest<JavaFileObject> {

    private OrderByGenerator gut;

    private TableInfo inputTable;

    public OrderByGeneratorTest(String classCode, TableInfo inputTable) {
        super(classCode);
        this.inputTable = inputTable;
        System.setProperty("resultParameter", OrderByGeneratorTest.class.getName());
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                        resourceText("example_order_by.txt"),
                        targetTableWithChildForeignKey()
                }
        });
    }

    @Before
    public void setUp() {
        gut = new OrderByGenerator(mockProcessingEnv, inputTable);
    }

    @Override
    protected BaseGenerator<JavaFileObject> generatorUnderTest() {
        return gut;
    }
}
