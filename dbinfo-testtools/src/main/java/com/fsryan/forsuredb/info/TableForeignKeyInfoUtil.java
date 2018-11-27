package com.fsryan.forsuredb.info;

import static com.fsryan.forsuredb.test.tools.CollectionUtil.mapOf;

public abstract class TableForeignKeyInfoUtil {

    public static TableForeignKeyInfo.Builder foreignKeyTo(String foreignTableName) {
        return TableForeignKeyInfo.builder()
                .foreignTableName(foreignTableName)
                .deleteChangeAction("CASCADE")
                .updateChangeAction("CASCADE")
                .localToForeignColumnMap(mapOf("local", "foreign"))
                .foreignTableApiClassName(TableInfoUtil.tableFQClassName(foreignTableName));
    }
}