package com.fsryan.forsuredb.serialization;

import com.fsryan.forsuredb.migration.MigrationSet;

import java.io.InputStream;

public interface FSDbInfoSerializer {
    MigrationSet deserialize(InputStream stream);
    String serialize(MigrationSet migrationSet);
}
