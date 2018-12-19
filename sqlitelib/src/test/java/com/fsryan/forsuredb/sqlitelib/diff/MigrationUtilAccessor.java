package com.fsryan.forsuredb.sqlitelib.diff;

import javax.annotation.Nonnull;

public class MigrationUtilAccessor {

    @Nonnull
    public static String sqlTypeOf(@Nonnull String qualifiedType) {
        return MigrationUtil.sqlTypeOf(qualifiedType);
    }
}
