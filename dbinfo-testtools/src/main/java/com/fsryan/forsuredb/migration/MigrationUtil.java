package com.fsryan.forsuredb.migration;

import com.fsryan.forsuredb.info.TableInfoUtil;

public abstract class MigrationUtil {

    public static Migration createDefaultTable() {
        return createTable(TableInfoUtil.DEFAULT_TABLE_NAME);
    }

    public static Migration createTable(String tableName) {
        return Migration.create(tableName, null, Migration.Type.CREATE_TABLE);
    }
}
