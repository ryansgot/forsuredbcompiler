package com.fsryan.forsuredb.api;

import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.info.TableInfo;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.fsryan.forsuredb.api.TableInfoUtil.bestEffortDAGSort;
import static com.fsryan.forsuredb.info.DBInfoFixtures.foreignKeyTo;
import static com.fsryan.forsuredb.info.DBInfoFixtures.tableBuilder;
import static com.fsryan.forsuredb.info.TableInfoUtil.tableMapOf;
import static com.fsryan.forsuredb.test.assertions.AssertCollection.assertListEquals;
import static com.fsryan.forsuredb.test.tools.CollectionUtil.mapOf;

public class TableInfoUtilTest {

    @Test
    public void shouldProperlySort() {
        TableInfo tA = tableBuilder("tA")
                .addForeignKey(tfkiTo("tB"))
                .build();
        TableInfo tB = tableBuilder("tB").build();
        TableInfo tC = tableBuilder("tC")
                .addForeignKey(tfkiTo("tA"))
                .build();
        TableInfo tD = tableBuilder("tD")
                .addForeignKey(tfkiTo("tC"))
                .addForeignKey(tfkiTo("tB"))
                .build();

        // A -> B
        // ^    ^
        // |    |
        // C <- D
        // should end up with sort BACD

        List<TableInfo> output = bestEffortDAGSort(tableMapOf(tA, tB, tC, tD));
        List<TableInfo> expected = Arrays.asList(tB, tA, tC, tD);
        assertListEquals(expected, output);
    }

    private static TableForeignKeyInfo tfkiTo(String table) {
        // we don't care about the column mapping at this point
        return foreignKeyTo(table).mapLocalToForeignColumn("", "").build();
    }
}
