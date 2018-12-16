package com.fsryan.forsuredb.sqlitelib.diff;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class TableRenameGenerator {

    private final String prevName;
    private final String currName;

    TableRenameGenerator(@Nonnull String prevName, @Nonnull String currName) {
        this.prevName = prevName;
        this.currName = currName;
    }

    @Nonnull
    public List<String> statements() {
        return Arrays.asList(
                "PRAGMA foreign_keys = ON;",
                String.format("ALTER TABLE %s RENAME TO %s;", prevName, currName)
        );
    }
}
