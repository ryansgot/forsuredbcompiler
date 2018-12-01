package com.fsryan.forsuredb.info.migration;

import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.migration.Migration;
import com.fsryan.forsuredb.serialization.FSDbInfoSerializer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static com.fsryan.forsuredb.info.DBInfoFixtures.cascadeForeignKeyTo;
import static com.fsryan.forsuredb.test.assertions.AssertCollection.assertSetEquals;
import static com.fsryan.forsuredb.test.tools.CollectionUtil.mapOf;
import static com.fsryan.forsuredb.test.tools.CollectionUtil.setOf;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public abstract class MigrationTest {


    @RunWith(Parameterized.class)
    public static class ExtrasHelpers extends MigrationTest {

        private final Map<String, String> returnedExtras;
        private final Set<TableForeignKeyInfo> expectedForeignKeys;
        private final Set<String> expectedCurrentColumns;

        @Mock
        private Migration mockMigration;
        @Mock
        private FSDbInfoSerializer mockFSDbInfoSerializer;

        public ExtrasHelpers(Map<String, String> returnedExtras,
                             Set<TableForeignKeyInfo> expectedForeignKeys,
                             Set<String> expectedCurrentColumns) {
            this.returnedExtras = returnedExtras;
            this.expectedForeignKeys = expectedForeignKeys;
            this.expectedCurrentColumns = expectedCurrentColumns;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {   // 00: null extras should cause emptyMigrationSet foreign keys and current columns
                            null,
                            Collections.<TableForeignKeyInfo>emptySet(),
                            Collections.<String>emptySet()
                    },
                    {   // 01: non-null, but emptyMigrationSet
                            Collections.<String, String>emptyMap(),
                            Collections.<TableForeignKeyInfo>emptySet(),
                            Collections.<String>emptySet()
                    },
                    {   // 02: non-null, current_foreign_keys and existing_column_names defined
                            mapOf(
                                    "current_foreign_keys", "some non null value",
                                    "existing_column_names", "some non null value"
                            ),
                            setOf(
                                    cascadeForeignKeyTo("foreign")
                                            .mapLocalToForeignColumn("local", "foreign")
                                            .build()
                            ),
                            setOf("some", "list", "of", "column", "names")
                    }
            });
        }

        @Before
        public void setUpMocks() {
            MockitoAnnotations.initMocks(this);
            when(mockMigration.hasExtras()).thenReturn(returnedExtras != null && !returnedExtras.isEmpty());
            when(mockMigration.extras()).thenReturn(returnedExtras);
            if (returnedExtras != null) {
                if (returnedExtras.containsKey("current_foreign_keys")) {
                    when(mockFSDbInfoSerializer.deserializeForeignKeys(eq(returnedExtras.get("current_foreign_keys"))))
                            .thenReturn(expectedForeignKeys);
                }
                if (returnedExtras.containsKey("existing_column_names")) {
                    when(mockFSDbInfoSerializer.deserializeColumnNames(eq(returnedExtras.get("existing_column_names"))))
                            .thenReturn(expectedCurrentColumns);
                }
            }
        }

        @Test
        public void shouldRetrieveCorrectForeignKeys() {
            assertSetEquals(expectedForeignKeys, Migration.foreignKeysOf(mockMigration, mockFSDbInfoSerializer));
        }

        @Test
        public void shouldRetrieveCorrectCurrentColumns() {
            assertSetEquals(expectedCurrentColumns, Migration.existingColumnNamesOf(mockMigration, mockFSDbInfoSerializer));
        }

    }
}
