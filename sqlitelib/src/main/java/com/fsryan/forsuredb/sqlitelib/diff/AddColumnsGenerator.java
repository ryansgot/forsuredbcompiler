package com.fsryan.forsuredb.sqlitelib.diff;

import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.TableInfo;

import javax.annotation.Nonnull;
import java.util.*;

public class AddColumnsGenerator {

    private final TableInfo table;
    private final List<ColumnInfo> columns;

    AddColumnsGenerator(@Nonnull String tableClassName, @Nonnull Map<String, TableInfo> schema, @Nonnull Set<String> columnNames) {
        table = schema.get(tableClassName);
        if (table == null) {
            throw new IllegalArgumentException("TableInfo not found for key: '" + tableClassName + "'");
        }

        columns = new ArrayList<>(columnNames.size());
        for (ColumnInfo column : table.getColumns()) {
            if (columnNames.contains(column.getColumnName())) {
                columns.add(column);
            }
        }
        if (columns.size() != columnNames.size()) {
            throw new IllegalArgumentException("could not find all columns " + columnNames + " in table: " + table.tableName());
        }
    }

    public List<String> statements() {
        List<String> ret = new ArrayList<>((int) (1.5 * columns.size()));
        for (ColumnInfo column : MigrationUtil.sortColumnsByName(columns)) {
            ret.add(addColumnSql(column));
            if (column.unique()) {
                // TODO: probably relocate this into a different class
                String uniqueIndex = String.format(
                        "CREATE UNIQUE INDEX IF NOT EXISTS %s_unique_%s ON %s(%s);",
                        table.tableName(),
                        column.getColumnName(),
                        table.tableName(),
                        column.getColumnName()
                );
                ret.add(uniqueIndex);
            }
        }
        return ret;
    }

    private String addColumnSql(ColumnInfo column) {
        final String sqlType = MigrationUtil.sqlTypeOf(column.getQualifiedType());
        StringBuilder buf = new StringBuilder("ALTER TABLE ")
                .append(table.tableName())
                .append(" ADD COLUMN ")
                .append(column.getColumnName())
                .append(' ').append(sqlType);
        if (column.hasDefaultValue()) {
            buf.append(" DEFAULT(")
                    .append(MigrationUtil.extractDefault(column))
                    .append(')');
        }
        return buf.append(';').toString();
    }
}
