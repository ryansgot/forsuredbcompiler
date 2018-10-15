package com.fsryan.forsuredb.info;

import com.fsryan.forsuredb.test.tools.CollectionUtil;

import java.util.Map;

public abstract class TableForeignKeyInfoUtil {

    public static TableForeignKeyInfo base(String foreignTable, String... localToForeignColumns) {
        return baseBuilder(foreignTable, localToForeignColumns).build();
    }

    public static TableForeignKeyInfo base(String foreignTable, Map<String, String> localToForeignColumnMap) {
        return baseBuilder(foreignTable, localToForeignColumnMap).build();
    }

    public static TableForeignKeyInfo.Builder baseBuilder(String foreignTable, String... localToForeignColumns) {
        return baseBuilder(foreignTable, CollectionUtil.mapFromArray(localToForeignColumns));
    }

    public static TableForeignKeyInfo.Builder baseBuilder(String foreignTable, Map<String, String> localToForeignColumnMap) {
        return builder()
                .localToForeignColumnMap(localToForeignColumnMap)
                .foreignTableName(foreignTable)
                .foreignTableApiClassName(TableForeignKeyInfoUtil.class.getName());
    }

    /**
     * <p>Defaults to set the foreignTableApiClassName to this class, update
     * and delete actions to CASCADE
     * @return
     */
    public static TableForeignKeyInfo.Builder builder() {
        return TableForeignKeyInfo.builder()
                .deleteChangeAction("CASCADE")
                .updateChangeAction("CASCADE")
                .foreignTableApiClassName(TableForeignKeyInfoUtil.class.getName());
    }
}
