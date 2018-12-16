package com.fsryan.forsuredb.sqlitelib.diff;

import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.info.TableInfo;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
        List<ColumnInfo> ret = new ArrayList<>(table.getColumns());
        Collections.sort(ret, new Comparator<ColumnInfo>() {
            @Override
            public int compare(ColumnInfo c1, ColumnInfo c2) {
                return c1.getColumnName().compareTo(c2.getColumnName());
            }
        });
        return ret;
    }
}
