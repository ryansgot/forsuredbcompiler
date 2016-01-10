package com.forsuredb.annotationprocessor.generator.code;

import com.forsuredb.annotationprocessor.TableContext;
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
public class ResolverGeneratorTest extends GeneratorTest<JavaFileObject> {

    private NewResolverGenerator gut;

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
                        resourceText("example_resolver_with_child_foreign_key.txt"),
                        targetTableWithChildForeignKey(),
                        testTargetContext()
                },
                {
                        resourceText("example_resolver_with_parent_and_child_foreign_key.txt"),
                        targetTableWithParentAndChildForeignKey(),
                        testTargetContext()
                }
        });
    }

    @Before
    public void setUp() {
        gut = new NewResolverGenerator(mockProcessingEnv, inputTable, inputTargetContext);
    }

    @Override
    protected NewBaseGenerator<JavaFileObject> generatorUnderTest() {
        return gut;
    }
}
