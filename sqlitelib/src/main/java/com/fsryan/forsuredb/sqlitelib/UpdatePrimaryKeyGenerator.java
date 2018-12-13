package com.fsryan.forsuredb.sqlitelib;

import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.migration.Migration;

import java.util.*;

/**
 * <p>Deprecated because this was intended to work with the previous migration
 * system. It probably would still work, but the ultimate goal is to completely
 * remove the old stuff when 1.0 rolls out.
 */
@Deprecated
public class UpdatePrimaryKeyGenerator extends RecreateTableGenerator {

    public UpdatePrimaryKeyGenerator(String tableName, Set<String> currentColumnNames, Map<String, TableInfo> targetSchema) {
        super(tableName, currentColumnNames, targetSchema, Migration.Type.UPDATE_PRIMARY_KEY);
    }
}
