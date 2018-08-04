package com.fsryan.forsuredb.serialization;

import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.migration.MigrationSet;

import java.io.InputStream;
import java.util.Set;

public interface FSDbInfoSerializer {
    MigrationSet deserializeMigrationSet(InputStream stream);
    MigrationSet deserializeMigrationSet(String json);
    Set<TableForeignKeyInfo> deserializeForeignKeys(String json);
    String serialize(MigrationSet migrationSet);
    Set<String> deserializeColumnNames(String stringSetJson);
}
