package com.fsryan.forsuredb.api.sqlgeneration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SqlForPreparedStatement {

    private final String sql;
    private final String[] replacements;

    public SqlForPreparedStatement(@Nonnull String sql, @Nullable String[] replacements) {
        this.sql = sql;
        this.replacements = replacements;
    }

    @Nonnull
    public String getSql() {
        return sql;
    }

    @Nullable
    public String[] getReplacements() {
        return replacements;
    }
}
