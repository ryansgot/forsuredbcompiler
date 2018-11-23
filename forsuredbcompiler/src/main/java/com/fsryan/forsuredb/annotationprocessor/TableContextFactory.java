package com.fsryan.forsuredb.annotationprocessor;

import com.fsryan.forsuredb.api.migration.MigrationRetriever;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.migration.MigrationSet;

import javax.annotation.Nonnull;
import java.util.*;

public abstract class TableContextFactory {

    @Nonnull
    public static TableContext createFromMigrationRetriever(MigrationRetriever mr) {
        return createfromMigrations(mr == null ? null : mr.getMigrationSets());
    }

    @Nonnull
    public static TableContext createfromMigrations(List<MigrationSet> migrationSets) {
        if (migrationSets == null || migrationSets.isEmpty()) {
            return TableContext.empty();
        }

        final Map<String, TableInfo> schema = migrationSets.stream()
                .max((ms1, ms2) -> ms2.dbVersion() - ms1.dbVersion())
                .orElseThrow(() -> new IllegalStateException("nonempty migration sets list must have a max db version"))
                .targetSchema();
        return TableContext.fromSchema(schema);
    }
}
