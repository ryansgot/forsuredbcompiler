package com.fsryan.forsuredb.api.sqlgeneration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SqlForPreparedStatement {

    public final String sql;
    public final String[] replacements;

    public SqlForPreparedStatement(@Nonnull String sql, @Nullable String[] replacements) {
        this.sql = sql;
        this.replacements = replacements;
    }
}
