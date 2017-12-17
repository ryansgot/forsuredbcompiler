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

    private static final Pattern SQLITE_LIMIT_PATTERN = Pattern.compile("\\s*\\d+\\s*(,\\s*\\d+\\s*)?");

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
        return queryBuf.toString();
    }

    public static String buildDelete(String table, String whereClause) {
        return "DELETE FROM " + table + (whereClause == null || whereClause.isEmpty() ? "" : " WHERE " + whereClause);
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
                limit == 0 ? null : Integer.toBinaryString(limit),
                offset == 0 ? null : Integer.toString(offset)
        );
    }

    // TODO: change signature to accept ints for limit and offset?
    public static String buildQuery(boolean isCompound,
                                    boolean distinct,
                                    @Nonnull String tables,
                                    @Nullable String[] projection,
                                    @Nullable String where,
                                    @Nullable String groupBy,
                                    @Nullable  String having,
                                    @Nullable String orderBy,
                                    @Nullable String limit,
                                    @Nullable String offset) {
        if ((groupBy == null || groupBy.isEmpty()) && !(having == null || having.isEmpty())) {
            throw new IllegalArgumentException("Cannot have HAVING without GROUP BY");
        }
        if (!(limit == null || limit.isEmpty()) && !SQLITE_LIMIT_PATTERN.matcher(limit).matches()) {
            throw new IllegalArgumentException("invalid LIMIT clause:" + limit);
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
//        appendClause(queryBuf, " ORDER BY ", orderBy);
        queryBuf.append(orderBy);                   // <-- TODO: fix this oddity in QueryCorrector or just get rid of QueryCorrector
        if (!isCompound) {
            appendClause(queryBuf, " LIMIT ", limit);
            appendClause(queryBuf, " OFFSET ", offset);
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

    private static void appendClause(StringBuilder s, String name, String clause) {
        if (clause == null || clause.isEmpty()) {
            return;
        }
        s.append(name);
        s.append(clause);
    }
}
