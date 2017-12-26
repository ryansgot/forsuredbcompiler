package com.fsryan.forsuredb.api.sqlgeneration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

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

    @Override
    public String toString() {
        return "SqlForPreparedStatement{" +
                "sql='" + sql + '\'' +
                ", replacements=" + Arrays.toString(replacements) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SqlForPreparedStatement that = (SqlForPreparedStatement) o;
        return Objects.equals(sql, that.sql) &&
                Arrays.equals(replacements, that.replacements);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(sql);
        result = 31 * result + Arrays.hashCode(replacements);
        return result;
    }
}
