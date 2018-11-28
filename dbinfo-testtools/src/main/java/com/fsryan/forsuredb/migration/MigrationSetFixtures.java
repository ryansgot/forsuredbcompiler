package com.fsryan.forsuredb.migration;

import java.util.Collections;

public class MigrationSetFixtures {

    public static MigrationSet emptyMigrationSet(int dbVersion) {
        return migrationSet(dbVersion)
                .orderedMigrations(Collections.emptyList())
                .targetSchema(Collections.emptyMap())
                .build();
    }

    public static MigrationSet.Builder migrationSet(int dbVersion) {
        return MigrationSet.builder().dbVersion(dbVersion);
    }
}
