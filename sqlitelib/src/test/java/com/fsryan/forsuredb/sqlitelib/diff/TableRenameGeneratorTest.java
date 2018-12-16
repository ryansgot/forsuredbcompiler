package com.fsryan.forsuredb.sqlitelib.diff;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.fsryan.forsuredb.test.assertions.AssertCollection.assertListEquals;

public class TableRenameGeneratorTest {

    @Test
    @DisplayName("Should generate correct SQL for table renaming")
    public void shouldCorrectlyGenerateSql() {
        List<String> statements = new TableRenameGenerator("t1", "t1_renamed").statements();
        assertListEquals(Arrays.asList(
                "PRAGMA foreign_keys = ON;",     // <-- if foreign keys are not on, then parent table references to not get updated prior to 3.26.0
                "ALTER TABLE t1 RENAME TO t1_renamed;"
        ), statements);
    }
}
