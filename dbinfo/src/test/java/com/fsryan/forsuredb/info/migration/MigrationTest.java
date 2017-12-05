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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
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
                    {   // 00: null extras should cause empty foreign keys and current columns
                            null,
                            Collections.<TableForeignKeyInfo>emptySet(),
                            Collections.<String>emptySet()
                    },
                    {   // 01: non-null, but empty
                            MigrationTest.<String, String>mapOf(),
                            Collections.<TableForeignKeyInfo>emptySet(),
                            Collections.<String>emptySet()
                    },
                    {   // 02: non-null, current_foreign_keys and existing_column_names defined
                            MigrationTest.mapOf(
                                    "current_foreign_keys", "some non null value",
                                    "existing_column_names", "some non null value"
                            ),
                            new HashSet<>(Arrays.asList(TableForeignKeyInfo.builder()
                                    .deleteChangeAction("CASCADE")
                                    .updateChangeAction("CASCADE")
                                    .localToForeignColumnMap(mapOf("local", "foreign"))
                                    .foreignTableApiClassName(MigrationTest.class.getName())
                                    .foreignTableName("foreign")
                                    .build())),
                            new HashSet<>(Arrays.asList("some", "list", "of", "column", "names"))
                    }
            });
        }

        @Before
        public void setUpMocks() {
            MockitoAnnotations.initMocks(this);
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
            assertEquals(expectedForeignKeys, Migration.foreignKeysOf(mockMigration, mockFSDbInfoSerializer));
        }

        @Test
        public void shouldRetrieveCorrectCurrentColumns() {
            assertEquals(expectedCurrentColumns, Migration.existingColumnNamesOf(mockMigration, mockFSDbInfoSerializer));
        }

    }

    static <K, V> Map<K, V> mapOf() {
        return new HashMap<>();
    }

    static <K, V> Map<K, V> mapOf(K k1, V v1) {
        Map<K, V> ret = mapOf();
        ret.put(k1, v1);
        return ret;
    }

    static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
        Map<K, V> ret = mapOf(k1, v1);
        ret.put(k2, v2);
        return ret;
    }
}
