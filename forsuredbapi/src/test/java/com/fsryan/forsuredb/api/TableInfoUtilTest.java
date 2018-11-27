package com.fsryan.forsuredb.api;

import com.fsryan.forsuredb.info.TableInfo;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.fsryan.forsuredb.api.TableInfoUtil.bestEffortDAGSort;
import static com.fsryan.forsuredb.info.Fixtures.tableBuilder;
import static com.fsryan.forsuredb.info.TableForeignKeyInfoUtil.foreignKeyTo;
import static com.fsryan.forsuredb.info.TableInfoUtil.tableMapOf;
import static org.junit.Assert.assertEquals;

public class TableInfoUtilTest {

    @Test
    public void shouldProperlySort() {
        TableInfo tA = tableBuilder("tA")
                .addForeignKey(foreignKeyTo("tB").build())
                .build();
        TableInfo tB = tableBuilder("tB").build();
        TableInfo tC = tableBuilder("tC")
                .addForeignKey(foreignKeyTo("tA").build())
                .build();
        TableInfo tD = tableBuilder("tD")
                .addForeignKey(foreignKeyTo("tC").build())
                .addForeignKey(foreignKeyTo("tB").build())
                .build();

        // A -> B
        // ^    ^
        // |    |
        // C <- D
        // should end up with sort BACD

        List<TableInfo> output = bestEffortDAGSort(tableMapOf(tA, tB, tC, tD));
        List<TableInfo> expected = Arrays.asList(tB, tA, tC, tD);
        assertEquals(expected, output);
    }
}
