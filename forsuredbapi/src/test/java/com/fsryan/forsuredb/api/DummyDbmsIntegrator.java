package com.fsryan.forsuredb.api;

import com.fsryan.forsuredb.migration.MigrationSet;
import com.fsryan.forsuredb.api.sqlgeneration.DBMSIntegrator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *     Since now the operation of the api does not have a default for {@link DBMSIntegrator}, this is necessary.
 * </p>
 */
public class DummyDbmsIntegrator implements DBMSIntegrator {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

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
    public String expressOrdering(List<FSOrdering> ordering) {
        return "";
    }

    @Override
    public String whereOperation(String tableName, String column, int operator) {
        return "";
    }

    @Override
    public String formatDate(Date date) {
        return DATE_FORMAT.format(date);
    }

    @Override
    public Date parseDate(String dateStr) {
        try {
            return DATE_FORMAT.parse(dateStr);
        } catch (ParseException pe) {
            pe.printStackTrace();
        }
        return null;
    }

    @Override
    public String wildcardKeyword() {
        return "";
    }

    @Override
    public String andKeyword() {
        return "";
    }

    @Override
    public String orKeyword() {
        return "";
    }
}
