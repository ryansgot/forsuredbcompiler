package com.fsryan.forsuredb.sqlitelib;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

class QueryBuilder {

    static final String EMPTY_SQL = ";";
    private static final Set<String> COLUMN_EXCLUSION_FILTER = new HashSet<>(Arrays.asList("_id", "created", "modified"));

    static String singleRecordInsertion(@Nonnull String tableName, @Nonnull List<String> columns) {
        final StringBuilder queryBuf = new StringBuilder("INSERT INTO " + tableName + " (");

        for (String column : columns) {
            if (column.isEmpty() || COLUMN_EXCLUSION_FILTER.contains(column)) {
                continue;   // <-- never insert _id, created, or modified columns
            }
            queryBuf.append(column).append(", ");
        }

        queryBuf.delete(queryBuf.length() - 2, queryBuf.length());  // <-- remove final ", "
        queryBuf.append(") VALUES (");
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).isEmpty() || COLUMN_EXCLUSION_FILTER.contains(columns.get(i))) {
                continue;   // <-- never insert _id, created, or modified columns
            }
            queryBuf.append("?, ");
        }
        queryBuf.delete(queryBuf.length() - 2, queryBuf.length());  // <-- remove final ", "
        return queryBuf.append(");").toString();
    }

    public static String buildUpdate(String table, List<String> updateColumns, String whereClause) {
        StringBuilder queryBuf = new StringBuilder(120);
        queryBuf.append("UPDATE ");
//        sql.append(CONFLICT_VALUES[conflictAlgorithm]); TODO
        queryBuf.append(table);
        queryBuf.append(" SET ");
        int i = 0;
        for (String column : updateColumns) {
            queryBuf.append((i > 0 ? "," : ""))
                    .append(column)
                    .append("=?");
            i++;
        }
        appendClause(queryBuf, " WHERE ", whereClause);
        return queryBuf.append(';').toString();
    }

    public static String buildDelete(String table, String whereClause) {
        return "DELETE FROM " + table + (whereClause == null || whereClause.isEmpty() ? "" : " WHERE " + whereClause) + ';';
    }

    public static String buildJoinQuery(boolean isCompound,
                                        boolean isDistinct,
                                        @Nonnull String table,
                                        @Nullable String[] projection,
                                        @Nullable String joinClause,
                                        @Nullable String whereClause,
                                        @Nullable String orderBy,
                                        int limit,
                                        int offset) {
        return buildQuery(
                isCompound,
                isDistinct,
                table + (joinClause == null || joinClause.isEmpty() ? "" : " " + joinClause),
                projection,
                whereClause,
                null,
                null,
                orderBy,
                limit,
                offset
        );
    }

    // TODO: change signature to accept ints for limit and offset?
    public static String buildQuery(boolean isCompound,
                                    boolean distinct,
                                    @Nonnull String tables,
                                    @Nullable String[] projection,
                                    @Nullable String where,
                                    @Nullable String groupBy,
                                    @Nullable String having,
                                    @Nullable String orderBy,
                                    int limit,
                                    int offset) {
        if ((groupBy == null || groupBy.isEmpty()) && !(having == null || having.isEmpty())) {
            throw new IllegalArgumentException("Cannot have HAVING without GROUP BY");
        }

        StringBuilder queryBuf = new StringBuilder(120);
        queryBuf.append("SELECT ");
        if (distinct) {
            queryBuf.append("DISTINCT ");
        }
        if (projection != null && projection.length != 0) {
            appendColumns(queryBuf, projection);
        } else {
            queryBuf.append("* ");
        }
        queryBuf.append("FROM ");
        queryBuf.append(tables);
        appendClause(queryBuf, " WHERE ", where);
        appendClause(queryBuf, " GROUP BY ", groupBy);
        appendClause(queryBuf, " HAVING ", having);
        appendClause(queryBuf, " ORDER BY ", orderBy);
        if (isCompound || (offset == 0 && limit == 0)) {
            return queryBuf.append(';').toString();
        }

        appendClause(queryBuf, " LIMIT ", Integer.toString(limit));
        if (offset > 0) {
            appendClause(queryBuf, " OFFSET ", Integer.toString(offset));
        }
        return queryBuf.append(';').toString();
    }

    private static void appendColumns(StringBuilder buf, String[] columns) {
        int n = columns.length;
        for (int i = 0; i < n; i++) {
            String column = columns[i];
            if (column != null) {
                if (i > 0) {
                    buf.append(", ");
                }
                buf.append(column);
            }
        }
        buf.append(' ');
    }

    private static void appendClause(StringBuilder buf, String name, String clause) {
        if (clause == null || clause.isEmpty()) {
            return;
        }
        buf.append(name);
        buf.append(clause);
    }
}
