package com.fsryan.forsuredb.annotationprocessor.generator.code;

import com.fsryan.forsuredb.annotationprocessor.TableContext;
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
public class ResolverGeneratorTest extends GeneratorTest<JavaFileObject> {

    private ResolverGenerator gut;

    private TableInfo inputTable;
    private TableContext inputTargetContext;

    public ResolverGeneratorTest(String classCode, TableInfo inputTable, TableContext inputTargetContext) {
        super(classCode);
        this.inputTable = inputTable;
        this.inputTargetContext = inputTargetContext;
        System.setProperty("resultParameter", ResolverGeneratorTest.class.getName());
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                        TestData.resourceText("example_resolver_with_child_foreign_key.txt"),
                        TestData.targetTableWithChildForeignKey(),
                        TestData.testTargetContext()
                },
                {
                        TestData.resourceText("example_resolver_with_parent_and_child_foreign_key.txt"),
                        TestData.targetTableWithParentAndChildForeignKey(),
                        TestData.testTargetContext()
                }
        });
    }

    @Before
    public void setUp() {
        gut = new ResolverGenerator(mockProcessingEnv, inputTable, inputTargetContext);
    }

    @Override
    protected BaseGenerator<JavaFileObject> generatorUnderTest() {
        return gut;
    }
}
