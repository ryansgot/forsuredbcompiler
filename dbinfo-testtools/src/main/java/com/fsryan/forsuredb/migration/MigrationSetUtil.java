package com.fsryan.forsuredb.migration;

import com.fsryan.forsuredb.info.TableInfoUtil;

import java.util.Collections;

import static com.fsryan.forsuredb.test.tools.CollectionUtil.mapOf;

public abstract class MigrationSetUtil {

    public static MigrationSet.Builder defaultBuilder() {
        return MigrationSet.builder()
                .dbVersion(1)
                .orderedMigrations(Collections.singletonList(MigrationUtil.createDefaultTable()))
                .targetSchema(mapOf(TableInfoUtil.DEFAULT_TABLE_NAME, TableInfoUtil.defaultBuilder().build()));
    }

    public static MigrationSet createDefault() {
        return defaultBuilder().build();
    }
}
