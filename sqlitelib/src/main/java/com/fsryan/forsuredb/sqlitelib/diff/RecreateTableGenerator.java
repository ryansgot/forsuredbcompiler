package com.fsryan.forsuredb.sqlitelib.diff;

import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.migration.SchemaDiff;

import javax.annotation.Nonnull;
import java.util.*;

public class RecreateTableGenerator {

    private final TableInfo table;
    private final Map<String, TableInfo> schema;
    private final SchemaDiff diff;

    public RecreateTableGenerator(String tableClassName, Map<String, TableInfo> schema, SchemaDiff diff) {
        table = schema.get(tableClassName);
        if (table == null) {
            throw new IllegalArgumentException("TableInfo not found for key: '" + tableClassName + "'");
        }
        this.schema = schema;
        this.diff = diff;
    }

    @Nonnull
    public List<String> statements() {
        final String tmpTableName = "forsuredb_new_" + table.tableName();
        List<String> ret = new ArrayList<>(10);
        ret.add(MigrationUtil.setForeignKeyPragma(false));
        ret.add("BEGIN TRANSACTION;");
        ret.add(String.format("DROP TABLE IF EXISTS %s;", tmpTableName));
        ret.add(MigrationUtil.createTableQuery(table, "forsuredb_new_"));
        ret.add(transferDataQuery(tmpTableName));
        ret.add("DROP TABLE t1;");
        ret.add(MigrationUtil.renameTableQuery(tmpTableName, table.tableName()));
        ret.add(MigrationUtil.modifiedTriggerQuery(table.tableName()));
        ret.add("END TRANSACTION;");
        ret.add(MigrationUtil.setForeignKeyPragma(true));
        return ret;
    }

    private String transferDataQuery(String tmpTableName) {
        StringBuilder buf = new StringBuilder("INSERT INTO ")
                .append(tmpTableName)
                .append(" SELECT ");
        Map<String, String> newToOldStringNamesMap = createNewToOldColumnNames();

        for (ColumnInfo column : MigrationUtil.sortTableColumnsByName(table)) {
            String columnName = newToOldStringNamesMap.get(column.getColumnName());
            columnName = columnName == null ? column.getColumnName() : columnName;
            buf.append(columnName).append(", ");
        }
        return StringUtil.cutDownBuf(buf, 2)
                .append(" FROM ")
                .append(table.tableName())
                .append(';')
                .toString();
    }
    // TODO: this is flawed because it does not anticipate columns to be added/dropped as well
    // TODO: test column name change
    private Map<String, String> createNewToOldColumnNames() {
        if ((diff.subType() & SchemaDiff.TYPE_RENAME_COLUMNS) == 0) {
            return Collections.emptyMap();
        }

        String renameColumnsStr = diff.attributes().get(SchemaDiff.ATTR_RENAME_COLUMNS);
        if (renameColumnsStr == null) {
            return Collections.emptyMap();
        }

        Map<String, String> ret = new HashMap<>();
        int delimIdx = -1;
        do {
            int startIdx = delimIdx + 1;
            int eqIdx = renameColumnsStr.indexOf('=', startIdx);
            delimIdx = renameColumnsStr.indexOf(',', eqIdx);
            final String prev = renameColumnsStr.substring(startIdx, eqIdx);
            final String current = delimIdx == -1
                    ? renameColumnsStr.substring(eqIdx + 1)
                    : renameColumnsStr.substring(eqIdx + 1, delimIdx);
            ret.put(current, prev);
        } while (delimIdx != -1);
        return ret;
    }
}
