package com.fsryan.forsuredb.annotationprocessor;

import com.fsryan.forsuredb.api.migration.MigrationRetriever;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.info.TableInfoUtil;
import com.fsryan.forsuredb.migration.MigrationSet;
import com.fsryan.forsuredb.migration.MigrationSetUtil;
import com.fsryan.forsuredb.migration.MigrationUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.fsryan.forsuredb.test.assertions.AssertCollection.assertMapEquals;
import static com.fsryan.forsuredb.test.tools.CollectionUtil.mapOf;

public abstract class TableContextFactoryTest<T> {

    final String desc;
    private final T source;
    private final Map<String, TableInfo> expectedSchema;

    public TableContextFactoryTest(String desc, T source, Map<String, TableInfo> expectedSchema) {
        this.desc = desc;
        this.source = source;
        this.expectedSchema = expectedSchema;
    }

    @Test
    public void shouldCreateCorrectSchema() {
        Map<String, TableInfo> actual = resultingSchema(source);
        assertMapEquals(desc, expectedSchema, actual);
    }


    protected abstract Map<String, TableInfo> resultingSchema(T source);

    public static class CreateFromMigrationRetriever extends TableContextFactoryTest<MigrationRetriever> {
        public CreateFromMigrationRetriever() {
            super("a null migration retriever should result in an empty schema", null, Collections.emptyMap());
        }

        @Override
        protected Map<String, TableInfo> resultingSchema(MigrationRetriever source) {
            return TableContextFactory.createFromMigrationRetriever(source).tableMap();
        }
    }

    @RunWith(Parameterized.class)
    public static class CreateFromMigrationSetList extends TableContextFactoryTest<List<MigrationSet>> {

        public CreateFromMigrationSetList(String desc, List<MigrationSet> source, Map<String, TableInfo> expected) {
            super(desc, source, expected);
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {
                            "00: null migration set list result in empty context",
                            null,
                            Collections.<String, TableInfo>emptyMap()
                    },
                    {
                            "01: single migration defining a table should result int correct table",
                            Collections.singletonList(MigrationSetUtil.createDefault()),
                            mapOf(TableInfoUtil.DEFAULT_TABLE_NAME, TableInfoUtil.defaultBuilder().build()),
                    }
            });
        }

        @Override
        protected Map<String, TableInfo> resultingSchema(List<MigrationSet> source) {
            return TableContextFactory.createfromMigrations(source).tableMap();
        }
    }
}
