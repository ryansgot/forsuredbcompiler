package com.fsryan.forsuredb.api;

import com.fsryan.forsuredb.info.TableInfo;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.fsryan.forsuredb.api.TableInfoUtil.bestEffortDAGSort;
import static com.fsryan.forsuredb.api.TestData.*;
import static org.junit.Assert.assertEquals;

public class TableInfoUtilTest {

    @Test
    public void shouldProperlySort() {
        TableInfo tA = table("tA")
                .addForeignKey(foreignKeyTo("tB").build())
                .build();
        TableInfo tB = table("tB")
                .build();
        TableInfo tC = table("tC")
                .addForeignKey(foreignKeyTo("tA").build())
                .build();
        TableInfo tD = table("tD")
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
