package com.fsryan.forsuredb.info.migration;

import com.fsryan.forsuredb.migration.MigrationSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static com.fsryan.forsuredb.migration.MigrationSetFixtures.emptyMigrationSet;
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
                            emptyMigrationSet(1),
                            emptyMigrationSet(2),
                            -1
                    },
                    {
                            emptyMigrationSet(1),
                            emptyMigrationSet(1),
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
}
