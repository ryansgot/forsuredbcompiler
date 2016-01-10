package com.forsuredb.annotationprocessor.generator.code;

import com.forsuredb.annotationprocessor.generator.GeneratorTest;
import com.forsuredb.annotationprocessor.generator.NewBaseGenerator;
import com.forsuredb.annotationprocessor.info.TableInfo;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.tools.JavaFileObject;
import java.util.Arrays;
import java.util.Collection;

import static com.forsuredb.TestData.*;

@RunWith(Parameterized.class)
public class TableCreatorGeneratorTest extends GeneratorTest<JavaFileObject> {

    private NewTableCreatorGenerator gut;

    private final Collection<TableInfo> tables;
    private final String packageName;

    public TableCreatorGeneratorTest(String expectedCode, String packageName, Collection<TableInfo> tables) {
        super(expectedCode);
        this.packageName = packageName;
        this.tables = tables;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                        resourceText("example_table_creator.txt"),
                        "com.forsuredb.annotationprocessor.generator.code",
                        Lists.newArrayList(testTargetContext().allTables())
                }
        });
    }

    @Before
    public void setUp() {
        gut = new NewTableCreatorGenerator(mockProcessingEnv, packageName, tables);
    }

    @Override
    protected NewBaseGenerator<JavaFileObject> generatorUnderTest() {
        return gut;
    }
}
