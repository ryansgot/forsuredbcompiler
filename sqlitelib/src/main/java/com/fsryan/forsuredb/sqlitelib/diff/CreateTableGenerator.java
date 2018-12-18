package com.fsryan.forsuredb.sqlitelib.diff;

import com.fsryan.forsuredb.info.TableInfo;

import javax.annotation.Nonnull;
import java.util.*;

public class CreateTableGenerator {

    private final TableInfo table;
    private final Map<String, TableInfo> schema;

    CreateTableGenerator(@Nonnull String tableClassName, @Nonnull Map<String, TableInfo> schema) {
        table = schema.get(tableClassName);
        if (table == null) {
            throw new IllegalArgumentException("TableInfo not found for key: '" + tableClassName + "'");
        }
        this.schema = schema;
    }

    // Columns sorted alphabetically always

    @Nonnull
    public List<String> statements() {
        final boolean hasForeignKeys = table.referencesOtherTable();
        List<String> ret = new ArrayList<>(hasForeignKeys ? 5 : 3);
        if (hasForeignKeys) {
            ret.add(MigrationUtil.setForeignKeyPragma(false));
        }
        ret.add(String.format("DROP TABLE IF EXISTS %s;", table.tableName()));
        ret.add(MigrationUtil.createTableQuery(table));
        ret.add(MigrationUtil.modifiedTriggerQuery(table.tableName()));
        // TODO: add indices
        if (hasForeignKeys) {
            ret.add(MigrationUtil.setForeignKeyPragma(true));
        }
        return ret;
    }
}
