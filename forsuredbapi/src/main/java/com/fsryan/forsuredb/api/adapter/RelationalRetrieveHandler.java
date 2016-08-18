package com.fsryan.forsuredb.api.adapter;

import com.fsryan.forsuredb.api.FSGetApi;

import java.util.Map;

/*package*/ class RelationalRetrieveHandler extends RetrieveHandler {
    public RelationalRetrieveHandler(Class<? extends FSGetApi> tableApi, String tableName, Map<String, String> methodNameToColumnNameMap, boolean isUnambiguous) {
        super(tableApi, tableName, methodNameToColumnNameMap, isUnambiguous);
    }
}
