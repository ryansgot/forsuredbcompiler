package com.fsryan.forsuredb.queryable;

import com.fsryan.forsuredb.FSDBHelper;
import com.fsryan.forsuredb.api.*;
import com.fsryan.forsuredb.api.sqlgeneration.DBMSIntegrator;
import com.fsryan.forsuredb.api.sqlgeneration.Sql;
import com.fsryan.forsuredb.api.sqlgeneration.SqlForPreparedStatement;
import com.fsryan.forsuredb.resultset.FSResultSet;

import javax.annotation.Nonnull;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcQueryable implements FSQueryable<DirectLocator, TypedRecordContainer> {

    /*package*/ interface DBProvider {
        Connection writeableDb();
        Connection readableDb();
    }

    private static final DBProvider realProvider = new DBProvider() {
        @Nonnull
        @Override
        public Connection writeableDb() {
            return FSDBHelper.inst().getWritableDatabase();
        }

        @Nonnull
        @Override
        public Connection readableDb() {
            return FSDBHelper.inst().getReadableDatabase();
        }
    };

    private final DirectLocator locator;
    private final DBProvider dbProvider;
    private final DBMSIntegrator sqlGenerator;

    public JdbcQueryable(@Nonnull String tableToQuery) {
        this(new DirectLocator(tableToQuery));
    }

    public JdbcQueryable(@Nonnull DirectLocator locator) {
        this(locator, realProvider, Sql.generator());
    }

    JdbcQueryable(@Nonnull DirectLocator locator, @Nonnull DBProvider dbProvider, DBMSIntegrator sqlGenerator) {
        this.locator = locator;
        this.dbProvider = dbProvider;
        this.sqlGenerator = sqlGenerator;
    }

    @Override
    public DirectLocator insert(TypedRecordContainer recordContainer) {
        // TODO: check whether this is necessary
        if (recordContainer.keySet().isEmpty()) {
            recordContainer.put("deleted", 0);
        }

        final List<String> columns = new ArrayList<>(recordContainer.keySet());
        final String sql = sqlGenerator.newSingleRowInsertionSql(locator.table, columns);

        try (PreparedStatement pStatement = dbProvider.writeableDb().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int pos = 0; pos < columns.size(); pos++) {
                bindObject(pos + 1, pStatement, recordContainer.get(columns.get(pos)));
            }
            if (pStatement.executeUpdate() < 1) {
                return null;
            }

            try (ResultSet resultSet = pStatement.getGeneratedKeys()) {
                resultSet.next();
                return new DirectLocator(locator.table, resultSet.getLong(1));
            }
        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        }
    }

    @Override
    public int update(TypedRecordContainer recordContainer, FSSelection selection, List<FSOrdering> orderings) {
        throw new UnsupportedOperationException();
        // TODO
//        String orderingString = sqlGenerator.expressOrdering(orderings);
//        final QueryCorrector qc = new QueryCorrector(locator.table, null, selection, orderingString);
//        return dbProvider.writeableDb()
//                .update(locator.table, recordContainer.getContentValues(), qc.getSelection(false), qc.getSelectionArgs());
    }

    @Override
    public int delete(FSSelection selection, List<FSOrdering> orderings) {
        // TODO
        throw new UnsupportedOperationException();
//        final QueryCorrector qc = new QueryCorrector(locator.table, null, selection, Sql.generator().expressOrdering(orderings));
//        return dbProvider.writeableDb()
//                .delete(locator.table, qc.getSelection(false), qc.getSelectionArgs());
    }

    @Override
    public Retriever query(FSProjection projection, FSSelection selection, List<FSOrdering> orderings) {
        SqlForPreparedStatement pssql = sqlGenerator.createQuerySql(locator.table, projection, selection, orderings);

        try {
            PreparedStatement statement = dbProvider.readableDb().prepareStatement(pssql.sql);
            if (pssql.replacements != null) {
                for (int pos = 0; pos < pssql.replacements.length; pos++) {
                    statement.setString(pos + 1, pssql.replacements[pos]);
                }
            }
            return new FSResultSet(statement.executeQuery());
        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        }
    }

    @Override
    public Retriever query(List<FSJoin> joins, List<FSProjection> projections, FSSelection selection, List<FSOrdering> orderings) {
        // TODO
        throw new UnsupportedOperationException();
//        final QueryCorrector qc = new QueryCorrector(locator.table, joins, selection, Sql.generator().expressOrdering(orderings));
//        final String sql = buildJoinQuery(projections, qc);
//        return (FSCursor) dbProvider.readableDb().rawQuery(sql, qc.getSelectionArgs());
    }

//    private String buildJoinQuery(List<FSProjection> projections, QueryCorrector qc) {
//        final StringBuilder buf = new StringBuilder("SELECT ");
//
//        // projection
//        final String[] p = formatProjection(projections);
//        if (p == null || p.length == 0) {
//            buf.append("* ");
//        } else {
//            for (String column : p) {
//                buf.append(column).append(", ");
//            }
//            buf.delete(buf.length() - 2, buf.length());
//        }
//
//        // TODO: using string concatenation in the string buffer is a little smelly
//        final String joinString = qc.getJoinString();
//        final String where = qc.getSelection(true);
//        final String orderBy = qc.getOrderBy();
//        return buf.append(" FROM ").append(locator.table)
//                .append(joinString.isEmpty() ? "" : " " + joinString)       // joins
//                .append(where.isEmpty() ? "" : " WHERE " + where)           // selection
//                .append(orderBy.isEmpty() ? "" : " ORDER BY " + orderBy)    // ordering
//                .append(qc.getLimit() > 0 ? " LIMIT " + qc.getLimit() : "") // limit
//                .append(';')
//                .toString();
//    }

    // TODO: perhaps this should be a helper
    private static void bindObject(int idx, PreparedStatement pStatement, Object obj) throws SQLException {
        Class<?> cls = obj.getClass();
        if (cls == Long.class) {
            pStatement.setLong(idx, (long) obj);
        } else if (cls == Integer.class) {
            pStatement.setInt(idx, (int) obj);
        } else if (cls == Double.class) {
            pStatement.setDouble(idx, (double) obj);
        } else if (cls == Float.class) {
            pStatement.setFloat(idx, (float) obj);
        } else if (cls == String.class) {
            pStatement.setString(idx, (String) obj);
        } else if (cls == byte[].class) {
            pStatement.setBytes(idx, (byte[]) obj);
        } else {
            throw new IllegalArgumentException("Cannot bind object of type: " + cls);
        }
    }
}
