package com.fsryan.forsuredb.annotationprocessor.generator.code;

import com.fsryan.forsuredb.annotationprocessor.generator.GeneratorTest;
import com.fsryan.forsuredb.annotationprocessor.generator.BaseGenerator;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.TestData;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.tools.JavaFileObject;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class TableCreatorGeneratorTest extends GeneratorTest<JavaFileObject> {

    private TableCreatorGenerator gut;

    private final Collection<TableInfo> tables;
    private final String packageName;

    public TableCreatorGeneratorTest(String expectedCode, String packageName, Collection<TableInfo> tables) {
        super(expectedCode, TableCreatorGeneratorTest.class.getName(), true);
        this.packageName = packageName;
        this.tables = tables;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                        TestData.resourceText("example_table_creator.txt"),
                        "com.fsryan.annotationprocessor.generator.code",
                        Lists.newArrayList(TestData.testTargetContext().allTables())
                }
        });
    }

    @Before
    public void setUp() {
        gut = new TableCreatorGenerator(mockProcessingEnv, packageName, tables);
    }

    @Override
    protected BaseGenerator<JavaFileObject> generatorUnderTest() {
        return gut;
    }
}
