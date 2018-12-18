package com.fsryan.forsuredb.sqlitelib.diff;

import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.sqlitelib.SqlGenerator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public abstract class MigrationUtil {

    @Nonnull
    public static String sqlTypeOf(@Nonnull String fqType) {
        switch (fqType) {
            case "java.math.BigDecimal":
            case "java.math.BigInteger":
            case "java.lang.String":
                return "TEXT";
            case "boolean":
            case "int":
            case "long":
            case "java.lang.Boolean":
            case "java.lang.Integer":
            case "java.lang.Long":
                return "INTEGER";
            case "double":
            case "float":
            case "java.lang.Double":
            case "java.lang.Float":
                return "REAL";
            case "java.util.Date":
                return "DATETIME";
            case "byte[]":
                return "BLOB";
        }
        throw new IllegalArgumentException("Unsupported type: " + fqType);
    }

    @Nonnull
    static String setForeignKeyPragma(boolean on) {
        return String.format("PRAGMA foreign_keys = %s;", on ? "ON" : "OFF");
    }

    @Nonnull
    static List<TableForeignKeyInfo> orderedForeignKeyDeclarations(@Nonnull TableInfo table) {
        List<TableForeignKeyInfo> ret = new ArrayList<>(table.foreignKeys());
        Collections.sort(ret, new Comparator<TableForeignKeyInfo>() {
            @Override
            public int compare(TableForeignKeyInfo tfki1, TableForeignKeyInfo tfki2) {
                return tfki1.foreignTableName().compareTo(tfki2.foreignTableName());
            }
        });
        return ret;
    }

    @Nonnull
    static List<ColumnInfo> sortTableColumnsByName(@Nonnull TableInfo table) {
        return sortColumnsByName(table.getColumns());
    }

    @Nonnull
    static List<ColumnInfo> sortColumnsByName(@Nonnull Collection<ColumnInfo> columns) {
        List<ColumnInfo> ret = new ArrayList<>(columns);
        Collections.sort(ret, new Comparator<ColumnInfo>() {
            @Override
            public int compare(ColumnInfo c1, ColumnInfo c2) {
                return c1.getColumnName().compareTo(c2.getColumnName());
            }
        });
        return ret;
    }

    @Nonnull
    static String extractDefault(ColumnInfo column) {
        String dfltVal = column.defaultValue();
        if (dfltVal == null) {
            throw new IllegalArgumentException("ColumnInfo " + column + " expected to have default value but was null");
        }
        return extractDefault(
                dfltVal,
                sqlTypeOf(column.getQualifiedType()),
                String.class.getName().equals(column.getQualifiedType())
        );
    }

    @Nonnull
    static String extractDefault(@Nonnull String defaultVal, @Nonnull String sqlTypeName, boolean surroundInQuotes) {
        if ("DATETIME".equals(sqlTypeName) && "CURRENT_TIMESTAMP".equals(defaultVal)) {
            return SqlGenerator.CURRENT_UTC_TIME;
        }

        final String escaped = defaultVal.replaceAll("'", "''");
        return surroundInQuotes ? "'" + escaped + "'" : escaped;
    }

    static boolean hasDefaultPrimaryKey(@Nonnull TableInfo table) {
        Set<String> pk = table.getPrimaryKey();
        return pk.size() == 0 || (pk.size() == 1 && pk.contains(TableInfo.DEFAULT_PRIMARY_KEY_COLUMN));
    }

    @Nonnull
    static String renameTableQuery(@Nonnull String from, @Nonnull String to) {
        return String.format("ALTER TABLE %s RENAME TO %s;", from, to);
    }

    @Nonnull
    static String modifiedTriggerQuery(@Nonnull String tableName) {
        return String.format(
                "CREATE TRIGGER IF NOT EXISTS %s_modified_trigger AFTER UPDATE ON %s BEGIN UPDATE %s SET modified=%s WHERE _id=NEW._id; END;",
                tableName,
                tableName,
                tableName,
                SqlGenerator.CURRENT_UTC_TIME
        );
    }

    @Nonnull
    static String createTableQuery(@Nonnull TableInfo table) {
        return createTableQuery(table, null);
    }

    @Nonnull
    static String createTableQuery(@Nonnull TableInfo table, @Nullable String transferTablePrefix) {
        StringBuilder buf = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append(transferTablePrefix == null || transferTablePrefix.isEmpty() ? "" : transferTablePrefix)
                .append(table.tableName())
                .append('(');

        boolean isDefaultPrimaryKey = hasDefaultPrimaryKey(table);
        addColDefsTo(buf, table, isDefaultPrimaryKey);

        if (!isDefaultPrimaryKey) {
            addPrimaryKeyTo(buf, table);
        }
        if (table.referencesOtherTable()) {
            addForeignKeysTo(buf, table);
        }
        return buf.append(");").toString();
    }

    @SuppressWarnings("ConstantConditions")
    private static StringBuilder addForeignKeysTo(StringBuilder buf, TableInfo table) {
        for (TableForeignKeyInfo tfki : orderedForeignKeyDeclarations(table)) {
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
    private static StringBuilder addPrimaryKeyTo(StringBuilder buf, TableInfo table) {
        buf.append(", PRIMARY KEY(");
        List<String> sortedPkColList = new ArrayList<>(table.getPrimaryKey());
        Collections.sort(sortedPkColList);
        for (String pkCol : sortedPkColList) {
            buf.append(pkCol).append(", ");
        }
        return buf.delete(buf.length() - 2, buf.length()).append(')');
    }

    @Nonnull
    private static StringBuilder addColDefsTo(StringBuilder buf, TableInfo table, boolean isDefaultPrimaryKey) {
        for (ColumnInfo col : sortTableColumnsByName(table)) {
            if (col.getColumnName().equals(TableInfo.DEFAULT_PRIMARY_KEY_COLUMN) && isDefaultPrimaryKey) {
                buf.append(col.getColumnName()).append(" INTEGER PRIMARY KEY, ");
            } else {
                buf.append(col.getColumnName())
                        .append(' ')
                        .append(MigrationUtil.sqlTypeOf(col.getQualifiedType()));
                if (col.unique()) {
                    // TODO: when composite uniqueness is a thing, you'll actually have to check for that.
                    buf.append(" UNIQUE");
                }
                if (col.hasDefaultValue()) {
                    //noinspection ConstantConditions
                    buf.append(" DEFAULT(")
                            .append(MigrationUtil.extractDefault(col))
                            .append(')');
                }
                buf.append(", ");
            }
        }
        return buf.delete(buf.length() - 2, buf.length());
    }
}
