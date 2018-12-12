package com.fsryan.forsuredb.sqlitelib;

import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.migration.Migration;

import java.util.Map;

/**
 * <p>Should only be used with existing columns. If the column is a new column,
 * then you can send it through
 * <p>Deprecated because this was intended to work with the previous migration
 * system. It probably would still work, but the ultimate goal is to completely
 * remove the old stuff when 1.0 rolls out.
 */
@Deprecated
public class ChangeDefaultValueGenerator extends RecreateTableGenerator {

    public ChangeDefaultValueGenerator(String tableName, Map<String, TableInfo> targetSchema) {
        super(tableName, targetSchema, Migration.Type.CHANGE_DEFAULT_VALUE);
    }
}
