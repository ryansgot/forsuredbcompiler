package com.fsryan.forsuredb.queryable;

import com.fsryan.forsuredb.ConnectionUtil;
import com.fsryan.forsuredb.FSDBHelper;
import com.fsryan.forsuredb.api.*;
import com.fsryan.forsuredb.api.adapter.SaveResultFactory;
import com.fsryan.forsuredb.api.sqlgeneration.DBMSIntegrator;
import com.fsryan.forsuredb.api.sqlgeneration.Sql;
import com.fsryan.forsuredb.api.sqlgeneration.SqlForPreparedStatement;
import com.fsryan.forsuredb.resultset.FSResultSet;

import javax.annotation.Nonnull;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.fsryan.forsuredb.queryable.StatementBinder.bindObject;
import static com.fsryan.forsuredb.queryable.StatementBinder.bindObjects;

public class JdbcQueryable implements FSQueryable<DirectLocator, TypedRecordContainer> {

    /*package*/ interface DBProvider {
        Connection writeableDb() throws SQLException;
        Connection readableDb() throws SQLException;
    }

    private static final DBProvider realProvider = new DBProvider() {
        @Nonnull
        @Override
        public Connection writeableDb() throws SQLException {
            return FSDBHelper.inst().getWritableDatabase();
        }

        @Nonnull
        @Override
        public Connection readableDb() throws SQLException {
            return FSDBHelper.inst().getReadableDatabase();
        }
    };

    @Nonnull
    private final DirectLocator locator;
    @Nonnull
    private final DBProvider dbProvider;
    @Nonnull
    private final DBMSIntegrator sqlGenerator;

    // TODO: output the queries by passing in a logger

    @SuppressWarnings("unused")
    public JdbcQueryable(@Nonnull String tableToQuery) {
        this(new DirectLocator(tableToQuery));
    }

    public JdbcQueryable(@Nonnull DirectLocator locator) {
        this(locator, realProvider, Sql.generator());
    }

    @SuppressWarnings("WeakerAccess")   // visible for testing
    JdbcQueryable(@Nonnull DirectLocator locator, @Nonnull DBProvider dbProvider, DBMSIntegrator sqlGenerator) {
        this.locator = locator;
        this.dbProvider = dbProvider;
        this.sqlGenerator = sqlGenerator;
    }

    @Override
    public DirectLocator insert(TypedRecordContainer recordContainer) {
        // this is kind of a hack to make the insertion logic easier
        if (recordContainer.keySet().isEmpty()) {
            recordContainer.put("deleted", 0);
        }

        final List<String> columns = new ArrayList<>(recordContainer.keySet());
        final String sql = sqlGenerator.newSingleRowInsertionSql(locator.table, columns);

        try (PreparedStatement pStatement = dbProvider.writeableDb().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindObjects(pStatement, columns, recordContainer);
            if (pStatement.executeUpdate() < 1) {
                return null;
            }

            try (ResultSet resultSet = pStatement.getGeneratedKeys()) {
                resultSet.next();   // TODO: not branching on this logic is a little sketchy
                return new DirectLocator(locator.table, resultSet.getLong(1));
            }
        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        }
    }

    @Override
    public int update(TypedRecordContainer recordContainer, FSSelection selection, List<FSOrdering> orderings) {
        if (recordContainer.keySet().isEmpty()) {
            return 0;
        }

        final List<String> columns = new ArrayList<>(recordContainer.keySet());
        SqlForPreparedStatement pssql = sqlGenerator.createUpdateSql(locator.table, columns, selection, orderings);
        try (PreparedStatement pStatement = dbProvider.writeableDb().prepareStatement(pssql.getSql())) {
            int pos;
            for (pos = 0; pos < columns.size(); pos++) {
                //noinspection ConstantConditions
                bindObject(pos + 1, pStatement, recordContainer.get(columns.get(pos)));
            }
            if (pssql.getReplacements() != null) {
                for (String replacement : pssql.getReplacements()) {
                    pStatement.setString(pos + 1, replacement);
                    pos++;
                }
            }
            return pStatement.executeUpdate();
        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        }
    }

    @Override
    public SaveResult<DirectLocator> upsert(TypedRecordContainer recordContainer, FSSelection selection, List<FSOrdering> orderings) {
        Connection db = null;
        boolean wasAutoCommit = false;
        try {
            db = dbProvider.writeableDb();
            wasAutoCommit = ConnectionUtil.ensureNotAutoCommit(db);

            try (Retriever r = query(null, selection, orderings)) {
                int rowsAffected = 0;
                DirectLocator inserted = null;

                if (r.moveToFirst()) {
                    rowsAffected = update(recordContainer, selection, orderings);
                } else {
                    inserted = insert(recordContainer);
                    rowsAffected = 1;
                }

                db.commit();
                return SaveResultFactory.create(inserted, rowsAffected, null);
            }
        } catch (RuntimeException | SQLException sqle) {
            if (db != null) {
                try {
                    db.rollback();
                } catch (SQLException sqle2) {
                    // TODO: determine what to do here
                }
            }
            return SaveResultFactory.create(null, 0, sqle);
        } finally {
            if (db != null && wasAutoCommit) {
                try {
                    db.setAutoCommit(true);
                } catch (SQLException sqle) {
                    // TODO: determine what to do here
                }
            }
        }
        // whole idea here is to create a transaction and perform the check-then-act sequence inside it, and then commit the transaction
    }

    @Override
    public int delete(FSSelection selection, List<FSOrdering> orderings) {
        SqlForPreparedStatement pssql = sqlGenerator.createDeleteSql(locator.table, selection, orderings);
        try (PreparedStatement pStatement = dbProvider.writeableDb().prepareStatement(pssql.getSql())) {
            if (pssql.getReplacements() != null) {
                for (int pos = 0; pos < pssql.getReplacements().length; pos++) {
                    pStatement.setString(pos + 1, pssql.getReplacements()[pos]);
                }
            }
            return pStatement.executeUpdate();
        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        }
    }

    @Override
    public Retriever query(FSProjection projection, FSSelection selection, List<FSOrdering> orderings) {
        SqlForPreparedStatement pssql = sqlGenerator.createQuerySql(locator.table, projection, selection, orderings);
        return query(pssql, dbProvider);
    }

    @Override
    public Retriever query(List<FSJoin> joins, List<FSProjection> projections, FSSelection selection, List<FSOrdering> orderings) {
        SqlForPreparedStatement pssql = sqlGenerator.createQuerySql(locator.table, joins, projections, selection, orderings);
        return query(pssql, dbProvider);
    }

    private static Retriever query(SqlForPreparedStatement pssql, DBProvider dbProvider) {
        try {
            PreparedStatement statement = dbProvider.readableDb().prepareStatement(pssql.getSql());
            if (pssql.getReplacements() != null) {
                for (int pos = 0; pos < pssql.getReplacements().length; pos++) {
                    statement.setString(pos + 1, pssql.getReplacements()[pos]);
                }
            }
            return new FSResultSet(statement.executeQuery());
        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        }
    }
}
