package com.fsryan.forsuredb.api;

import com.fsryan.forsuredb.api.sqlgeneration.DBMSIntegrator;
import com.fsryan.forsuredb.api.sqlgeneration.Sql;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

public abstract class BaseGetter implements FSGetApi {

    protected final DBMSIntegrator sqlGenerator;
    protected final String tableName;

    public BaseGetter(String tableName) {
        this(Sql.generator(), tableName);
    }

    BaseGetter(DBMSIntegrator sqlGenerator, String tableName) {
        this.sqlGenerator = sqlGenerator;
        this.tableName = tableName;
    }

    @Override
    public long id(@Nonnull Retriever retriever) {
        throwIfNullRetriever(retriever);
        return retriever.getLong(disambiguateColumn("_id"));
    }

    @Override
    public Date created(@Nonnull Retriever retriever) {
        return parseDateColumn(retriever, "created");
    }

    @Override
    public Date modified(@Nonnull Retriever retriever) {
        return parseDateColumn(retriever, "modified");
    }

    @Override
    public boolean deleted(@Nonnull Retriever retriever) {
        return parseBooleanColumn(retriever, "deleted");
    }

    protected String disambiguateColumn(String columnName) {
        return sqlGenerator.unambiguousRetrievalColumn(tableName, columnName);
    }

    protected Date parseDateColumn(@Nonnull Retriever retriever, @Nonnull String columnName) {
        throwIfNullRetriever(retriever);
        final String dateString = retriever.getString(disambiguateColumn(columnName));
        return dateString == null ? null : sqlGenerator.parseDate(dateString);
    }

    protected boolean parseBooleanColumn(@Nonnull Retriever retriever, @Nonnull String columnName) {
        throwIfNullRetriever(retriever);
        final int val = retriever.getInt(disambiguateColumn(columnName));
        return val == 1;
    }

    protected BigInteger parseBigIntegerColumn(@Nonnull Retriever retriever, @Nonnull String columnName) {
        throwIfNullRetriever(retriever);
        final String val = retriever.getString(disambiguateColumn(columnName));
        try {
            return val == null ? null : new BigInteger(val);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Looks like column " + columnName + " was not a " + BigInteger.class + "; actual value: " + val, nfe);
        }
    }

    protected BigDecimal parseBigDecimalColumn(@Nonnull Retriever retriever, @Nonnull String columnName) {
        throwIfNullRetriever(retriever);
        final String val = retriever.getString(disambiguateColumn(columnName));
        try {
            return val == null ? null : new BigDecimal(val);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Looks like column " + columnName + " was not a " + BigDecimal.class + "; actual value: " + val, nfe);
        }
    }

    protected String retrieveString(Retriever retriever, String columnName) {
        throwIfNullRetriever(retriever);
        return retriever.getString(disambiguateColumn(columnName));
    }

    protected void throwIfNullRetriever(Retriever retriever) {
        if (retriever == null) {
            throw new IllegalArgumentException("Null retriever not alllowed");
        }
    }
}
