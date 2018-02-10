package com.fsryan.forsuredb.api;

import com.fsryan.forsuredb.info.TableForeignKeyInfo;
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
                .foreignKeys(setOf(foreignKeyTo("tB").build()))
                .build();
        TableInfo tB = table("tB")
                .foreignKeys(TestData.<TableForeignKeyInfo>setOf())
                .build();
        TableInfo tC = table("tC")
                .foreignKeys(setOf(foreignKeyTo("tA").build()))
                .build();
        TableInfo tD = table("tD")
                .foreignKeys(setOf(
                        foreignKeyTo("tC").build(),
                        foreignKeyTo("tB").build()
                )).build();

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
