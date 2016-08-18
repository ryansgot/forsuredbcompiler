package com.fsryan.forsuredb.annotationprocessor.generator.code.resolver;

import com.fsryan.forsuredb.annotationprocessor.TableContext;
import com.fsryan.forsuredb.annotationprocessor.generator.GeneratorTest;
import com.fsryan.forsuredb.annotationprocessor.generator.BaseGenerator;
import com.fsryan.forsuredb.api.info.TableInfo;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.tools.JavaFileObject;
import java.util.Arrays;

import static com.fsryan.forsuredb.TestData.*;

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
                        resourceText("example_relational_resolver_with_child_foreign_key.txt"),
                        targetTableWithChildForeignKey(),
                        testTargetContext()
                },
                {
                        resourceText("example_relational_resolver_with_parent_and_child_foreign_key.txt"),
                        targetTableWithParentAndChildForeignKey(),
                        testTargetContext()
                },
                // TODO: test DocStoreResolver extension
        });
    }

    @Before
    public void setUp() {
        gut = ResolverGenerator.getFor(mockProcessingEnv, inputTable, inputTargetContext);
    }

    @Override
    protected BaseGenerator<JavaFileObject> generatorUnderTest() {
        return gut;
    }
}
