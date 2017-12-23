package com.fsryan.forsuredb.info.migration;

import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.migration.Migration;
import com.fsryan.forsuredb.migration.MigrationSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public abstract class MigrationSetTest {

    @RunWith(Parameterized.class)
    public static class Sorting {

        private final MigrationSet lhs;
        private final MigrationSet rhs;
        private final int signOfLeftToRightComparison;

        public Sorting(MigrationSet lhs, MigrationSet rhs, int signOfLeftToRightComparison) {
            this.lhs = lhs;
            this.rhs = rhs;
            this.signOfLeftToRightComparison = signOfLeftToRightComparison;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {
                            createMigrationSet(1),
                            createMigrationSet(2),
                            -1
                    },
                    {
                            createMigrationSet(1),
                            createMigrationSet(1),
                            0
                    }
            });
        }

        @Test
        public void shoudlCalcualteCorrectLeftToRightComparison() {
            final int comparison = lhs.compareTo(rhs);
            assertEquals(signOfLeftToRightComparison, (int) Math.signum(comparison));
        }

        @Test
        public void shoudlCalcualteCorrecRightToLeftComparison() {
            final int comparison = rhs.compareTo(lhs);
            assertEquals(-1 * signOfLeftToRightComparison, (int) Math.signum(comparison));
        }
    }

    protected static MigrationSet createMigrationSet(int version) {
        return MigrationSet.builder()
                .dbVersion(version)
                .orderedMigrations(new ArrayList<Migration>())
                .targetSchema(new HashMap<String, TableInfo>())
                .build();
    }
}
