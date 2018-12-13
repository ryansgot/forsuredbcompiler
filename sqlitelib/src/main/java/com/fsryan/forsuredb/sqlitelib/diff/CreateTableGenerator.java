package com.fsryan.forsuredb.sqlitelib.diff;

import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.info.TableInfo;

import javax.annotation.Nonnull;
import java.util.*;

import static com.fsryan.forsuredb.sqlitelib.SqlGenerator.CURRENT_UTC_TIME;

public class CreateTableGenerator {

    private final TableInfo table;
    private final Map<String, TableInfo> schema;

    CreateTableGenerator(@Nonnull String tableClassName, @Nonnull Map<String, TableInfo> schema) {
        table = schema.get(tableClassName);
        if (table == null) {
            throw new IllegalArgumentException("TableInfo not found for key: '" + tableClassName + "'");
        }
        this.schema = schema;
    }

    // Columns sorted alphabetically always

    @Nonnull
    public List<String> statements() {
        final boolean hasForeignKeys = hasForeignKeys();
        List<String> ret = new ArrayList<>(hasForeignKeys ? 5 : 3);
        if (hasForeignKeys) {
            ret.add("PRAGMA foreign_keys = false;");
        }
        ret.add(String.format("DROP TABLE IF EXISTS %s;", table.tableName()));
        ret.add(createTableQuery());
        ret.add(String.format(
                "CREATE TRIGGER IF NOT EXISTS %s_modified_trigger AFTER UPDATE ON %s BEGIN UPDATE %s SET modified=%s WHERE _id=NEW._id; END;",
                table.tableName(),
                table.tableName(),
                table.tableName(),
                CURRENT_UTC_TIME
        ));
        // TODO: add indices
        if (hasForeignKeys) {
            ret.add("PRAGMA foreign_keys = true;");
        }
        return ret;
    }

    @Nonnull
    private static String interpretDefaultValue(@Nonnull String defVal) {
        return "CURRENT_TIMESTAMP".equals(defVal) ? CURRENT_UTC_TIME : escape(defVal);
    }

    @Nonnull
    private static String escape(@Nonnull String toEscape) {
        return '\'' + toEscape.replaceAll("\\'", "''") + '\'';
    }

    @Nonnull
    private String createTableQuery() {
        StringBuilder buf = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append(table.tableName())
                .append('(');

        boolean isDefaultPrimaryKey = isDefaultPrimaryKey();
        addColDefsTo(buf, isDefaultPrimaryKey);

        if (!isDefaultPrimaryKey) {
            addPrimaryKeyTo(buf);
        }
        if (hasForeignKeys()) {
            addForeignKeysTo(buf);
        }
        return buf.append(");").toString();
    }

    @SuppressWarnings("ConstantConditions")
    private StringBuilder addForeignKeysTo(StringBuilder buf) {
        for (TableForeignKeyInfo tfki : MigrationUtil.orderedForeignKeyDeclarations(table)) {
            buf.append(", FOREIGN KEY(");
            List<String> orderedLocalCols = new ArrayList<>(tfki.localToForeignColumnMap().keySet());
            Collections.sort(orderedLocalCols);
            for (String localCol : orderedLocalCols) {
                buf.append(localCol).append(", ");
            }
            buf.delete(buf.length() - 2, buf.length())
                    .append(") REFERENCES ")
                    .append(tfki.foreignTableName())
                    .append('(');
            for (String localCol : orderedLocalCols) {
                buf.append(tfki.localToForeignColumnMap().get(localCol)).append(", ");
            }
            buf.delete(buf.length() - 2, buf.length()).append(')');
            if (!tfki.deleteChangeAction().isEmpty()) {
                buf.append(" ON DELETE ").append(tfki.deleteChangeAction());
            }
            if (!tfki.updateChangeAction().isEmpty()) {
                buf.append(" ON UPDATE ").append(tfki.updateChangeAction());
            }
        }
        return buf;
    }

    @Nonnull
    private StringBuilder addPrimaryKeyTo(StringBuilder buf) {
        buf.append(", PRIMARY KEY(");
        List<String> sortedPkColList = new ArrayList<>(table.getPrimaryKey());
        Collections.sort(sortedPkColList);
        for (String pkCol : sortedPkColList) {
            buf.append(pkCol).append(", ");
        }
        return buf.delete(buf.length() - 2, buf.length()).append(')');
    }

    @Nonnull
    private StringBuilder addColDefsTo(StringBuilder buf, boolean isDefaultPrimaryKey) {
        for (ColumnInfo col : MigrationUtil.sortTableColumnsByName(table)) {
            if (col.getColumnName().equals(TableInfo.DEFAULT_PRIMARY_KEY_COLUMN) && isDefaultPrimaryKey) {
                buf.append(col.getColumnName()).append(" INTEGER PRIMARY KEY, ");
            } else {
                buf.append(col.getColumnName())
                        .append(' ')
                        .append(MigrationUtil.sqlTypeOf(col.getQualifiedType()));
                if (col.hasDefaultValue()) {
                    //noinspection ConstantConditions
                    buf.append(" DEFAULT(")
                            .append(interpretDefaultValue(col.defaultValue()))
                            .append(')');
                }
                buf.append(", ");
            }
        }
        return buf.delete(buf.length() - 2, buf.length());
    }

    private boolean hasForeignKeys() {
        Set<TableForeignKeyInfo> foreignKeys = table.foreignKeys();
        return foreignKeys != null && !foreignKeys.isEmpty();
    }

    private boolean isDefaultPrimaryKey() {
        Set<String> pk = table.getPrimaryKey();
        return pk.size() == 0 || (pk.size() == 1 && pk.contains(TableInfo.DEFAULT_PRIMARY_KEY_COLUMN));
    }
}
