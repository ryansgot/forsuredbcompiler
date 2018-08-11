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
        throwIfNullRetriever(retriever);
        final String dateString = retriever.getString(disambiguateColumn("created"));
        return dateString == null ? null : sqlGenerator.parseDate(dateString);
    }

    @Override
    public Date modified(@Nonnull Retriever retriever) {
        throwIfNullRetriever(retriever);
        final String dateString = retriever.getString(disambiguateColumn("modified"));
        return dateString == null ? null : sqlGenerator.parseDate(dateString);
    }

    @Override
    public boolean deleted(@Nonnull Retriever retriever) {
        throwIfNullRetriever(retriever);
        final int val = retriever.getInt(disambiguateColumn("deleted"));
        return val == 1;
    }

    protected String disambiguateColumn(String columnName) {
        return sqlGenerator.unambiguousRetrievalColumn(tableName, columnName);
    }

    protected void throwIfNullRetriever(Retriever retriever) {
        if (retriever == null) {
            throw new IllegalArgumentException("Null retriever not alllowed");
        }
    }
}
