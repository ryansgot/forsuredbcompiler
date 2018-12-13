package com.fsryan.forsuredb.sqlitelib.diff;

import com.fsryan.forsuredb.info.TableInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.fsryan.forsuredb.info.DBInfoFixtures.tableBuilder;
import static com.fsryan.forsuredb.info.TableInfoUtil.tableMapOf;
import static com.fsryan.forsuredb.test.assertions.AssertCollection.assertListEquals;

public class TableRenameGeneratorTest {

    @Test
    @DisplayName("should generate correct TABLE CREATE sql given tableclassName and schema")
    public void shouldCorrectlyGenerateSql() {
        Map<String, TableInfo> schema = tableMapOf(
                tableBuilder("t1_renamed")
                        .build()
        );
        List<String> statements = new TableRenameGenerator("t1", "t1_renamed").statements();
        assertListEquals(Collections.singletonList("ALTER TABLE t1 RENAME TO t1_renamed"), statements);
    }
}
