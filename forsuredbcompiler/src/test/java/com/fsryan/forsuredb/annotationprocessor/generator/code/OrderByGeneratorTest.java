package com.fsryan.forsuredb.annotationprocessor.generator.code;

import com.fsryan.forsuredb.annotationprocessor.generator.BaseGenerator;
import com.fsryan.forsuredb.annotationprocessor.generator.GeneratorTest;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.TestData;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.tools.JavaFileObject;
import java.util.Arrays;

@RunWith(Parameterized.class)
public class OrderByGeneratorTest extends GeneratorTest<JavaFileObject> {

    private OrderByGenerator gut;

    private TableInfo inputTable;

    public OrderByGeneratorTest(String classCode, TableInfo inputTable) {
        super(classCode, OrderByGeneratorTest.class.getName(), true);
        this.inputTable = inputTable;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                        TestData.resourceText("example_order_by.txt"),
                        TestData.targetTableWithChildForeignKey()
                },
                // TODO: tests for doc store generation
        });
    }

    @Before
    public void setUp() {
        gut = OrderByGenerator.create(mockProcessingEnv, inputTable);
    }

    @Override
    protected BaseGenerator<JavaFileObject> generatorUnderTest() {
        return gut;
    }
}
