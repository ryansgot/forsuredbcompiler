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

    private NewSetterGenerator gut;
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
                        table().qualifiedClassName("com.forsuredb.annotationprocessor.generator.code.TestTable")
                                .columnMap(columnMapOf(idCol(),
                                        modifiedCol(),
                                        createdCol(),
                                        deletedCol(),
                                        longCol().columnName("global_id")
                                                .methodName("globalId")
                                                .build(),
                                        doubleCol().columnName("app_rating")
                                                .methodName("appRating")
                                                .build(),
                                        bigDecimalCol().columnName("competitor_app_rating")
                                                .methodName("competitorAppRating")
                                                .build(),
                                        intCol().columnName("login_count")
                                                .methodName("loginCount")
                                                .build()))
                                .tableName("test_table")
                                .build(),
                        SetterGeneratorTest.class
                }
        });
    }

    @Before
    public void setUp() {
        gut = new NewSetterGenerator(mockProcessingEnv, table);
    }

    @Override
    protected NewBaseGenerator<JavaFileObject> generatorUnderTest() {
        return gut;
    }
}
