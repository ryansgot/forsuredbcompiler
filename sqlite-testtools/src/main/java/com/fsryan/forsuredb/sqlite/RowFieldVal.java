package com.fsryan.forsuredb.sqlite;

import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;

@AutoValue
public abstract class RowFieldVal {

    public static RowFieldVal createNull() {
        return create("NULL", "NULL", "IS");
    }

    public static RowFieldVal createNotNull() {
        return create("NULL", "NULL", "IS NOT");
    }

    public static RowFieldVal create(@Nonnull String sqlType, @Nonnull String val, @Nonnull String operator) {
        return new AutoValue_RowFieldVal(sqlType, val, operator);
    }

    @Nonnull public abstract String sqlType();
    @Nonnull public abstract String val();
    @Nonnull public abstract String operator();
}
