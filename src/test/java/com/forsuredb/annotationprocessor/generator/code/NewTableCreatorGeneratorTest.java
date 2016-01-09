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
public class NewTableCreatorGeneratorTest extends GeneratorTest<JavaFileObject> {

    private NewTableCreatorGenerator gut;

    private final Collection<TableInfo> tables;
    private final String packageName;

    public NewTableCreatorGeneratorTest(String expectedCode, String packageName, Collection<TableInfo> tables) {
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
                        Lists.newArrayList(
                                table().qualifiedClassName("com.forsuredb.annotationprocessor.generator.code.TestTable")
                                        .columnMap(columnMapOf(idCol(),
                                                modifiedCol(),
                                                createdCol(),
                                                deletedCol(),
                                                longCol().columnName("global_id")
                                                        .methodName("globalId")
                                                        .foreignKeyInfo(cascadeFKI("test_table_2")
                                                                .apiClassName("com.forsuredb.annotationprocessor.generator.code.TestTable2")
                                                                .build())
                                                        .build()))
                                        .tableName("test_table")
                                        .staticDataAsset("test_table_data.xml")
                                        .staticDataRecordName("test_table_data")
                                        .build(),
                                table().qualifiedClassName("com.forsuredb.annotationprocessor.generator.code.TestTable2")
                                        .columnMap(columnMapOf(idCol(),
                                                modifiedCol(),
                                                createdCol(),
                                                deletedCol(),
                                                longCol().columnName("global_id")
                                                        .methodName("globalId")
                                                        .foreignKeyInfo(cascadeFKI("test_table_3")
                                                                .apiClassName("com.forsuredb.annotationprocessor.generator.code.TestTable3")
                                                                .build())
                                                        .build()))
                                        .tableName("test_table_2")
                                        .build(),
                                table().qualifiedClassName("com.forsuredb.annotationprocessor.generator.code.TestTable3")
                                        .columnMap(columnMapOf(idCol(),
                                                modifiedCol(),
                                                createdCol(),
                                                deletedCol()))
                                        .tableName("test_table_3")
                                        .build()
                        )
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
