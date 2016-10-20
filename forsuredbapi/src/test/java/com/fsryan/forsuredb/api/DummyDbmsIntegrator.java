package com.fsryan.forsuredb.api;

import com.fsryan.forsuredb.api.migration.MigrationSet;
import com.fsryan.forsuredb.api.sqlgeneration.DBMSIntegrator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *     Since now the operation of the api does not have a default for {@link DBMSIntegrator}, this is necessary.
 * </p>
 */
public class DummyDbmsIntegrator implements DBMSIntegrator {

    @Override
    public List<String> generateMigrationSql(MigrationSet migrationSet) {
        return new ArrayList<>();
    }

    @Override
    public String newSingleRowInsertionSql(String tableName, Map<String, String> columnValueMap) {
        return "";
    }

    @Override
    public String unambiguousColumn(String tableName, String columnName) {
        return tableName + "_" + columnName;
    }

    @Override
    public String unambiguousRetrievalColumn(String tableName, String columnName) {
        return unambiguousColumn(tableName, columnName);
    }

    @Override
    public String orderByAsc(String tableName, String columnName) {
        return "";
    }

    @Override
    public String orderByDesc(String tableName, String columnName) {
        return "";
    }

    @Override
    public String combineOrderByExpressions(List<String> orderByList) {
        return "";
    }
}
