package com.fsryan.forsuredb.sqlitelib;

import javax.annotation.Nonnull;
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
        return queryBuf.append(')').toString();
    }

    public static String buildQuery(boolean distinct, String tables, String[] columns, String where, String groupBy, String having, String orderBy, String limit) {
        if ((groupBy == null || groupBy.isEmpty()) && !(having == null || having.isEmpty())) {
            throw new IllegalArgumentException("HAVING clauses are only permitted when using a groupBy clause");
        }
        if (!(limit == null || limit.isEmpty()) && !SQLITE_LIMIT_PATTERN.matcher(limit).matches()) {
            throw new IllegalArgumentException("invalid LIMIT clauses:" + limit);
        }

        StringBuilder query = new StringBuilder(120);
        query.append("SELECT ");
        if (distinct) {
            query.append("DISTINCT ");
        }
        if (columns != null && columns.length != 0) {
            appendColumns(query, columns);
        } else {
            query.append("* ");
        }
        query.append("FROM ");
        query.append(tables);
        appendClause(query, " WHERE ", where);
        appendClause(query, " GROUP BY ", groupBy);
        appendClause(query, " HAVING ", having);
        appendClause(query, " ORDER BY ", orderBy);
        appendClause(query, " LIMIT ", limit);
        return query.append(';').toString();
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

    private static StringBuilder repeat(int times, String str, int trimEnd) {
        int capacity = str.length() * times;
        StringBuilder buf = new StringBuilder(capacity);
        while (buf.length() != capacity) {
            buf.append(str);
        }
        return trimEnd > 0 ? buf.delete(buf.length() - trimEnd, buf.length()) : buf;
    }
}
