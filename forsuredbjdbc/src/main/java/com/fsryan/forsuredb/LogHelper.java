package com.fsryan.forsuredb;

import com.fsryan.forsuredb.api.FSLogger;
import com.fsryan.forsuredb.api.RecordContainer;
import com.fsryan.forsuredb.api.TypedRecordContainer;
import com.fsryan.forsuredb.api.sqlgeneration.SqlForPreparedStatement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public abstract class LogHelper {

    private static boolean loggingDisabled = true;

    public static void setLoggingOn(boolean enabled) {
        loggingDisabled = !enabled;
    }

    public static void logInsertion(@Nullable FSLogger logger,
                                    @Nonnull String sql,
                                    @Nonnull List<String> columns,
                                    @Nonnull RecordContainer record) {
        if (loggingDisabled || logger == null) {
            return;
        }

        logger.i("[forsuredb.insert] %s", bindColumnsFirst(sql, columns, record));
    }

    public static void logMigration(@Nullable FSLogger logger, @Nullable String prefix, @Nonnull String sql) {
        if (loggingDisabled || logger == null) {
            return;
        }
        logger.i("[forsuredb.migrate] %s %s", prefix == null ? "" : prefix, sql);
    }

    public static boolean isLoggingOn() {
        return !loggingDisabled;
    }

    public static void logUpdate(@Nullable FSLogger logger,
                                 @Nonnull List<String> columns,
                                 @Nonnull SqlForPreparedStatement pssql,
                                 @Nonnull TypedRecordContainer recordContainer) {
        if (loggingDisabled || logger == null) {
            return;
        }

        String actual = bindColumnsFirst(pssql.getSql(), columns, recordContainer);
        actual = bindColumnsFirst(actual, pssql.getReplacements());
        logger.i("[forsuredb.update] %s", actual);
    }

    public static void logDeletion(@Nullable FSLogger logger, @Nonnull SqlForPreparedStatement pssql) {
        if (loggingDisabled || logger == null) {
            return;
        }

        logger.i("[forsuredb.delete] %s", bindColumnsFirst(pssql.getSql(), pssql.getReplacements()));
    }

    public static void logQuery(@Nullable FSLogger logger, @Nonnull SqlForPreparedStatement pssql) {
        if (loggingDisabled || logger == null) {
            return;
        }

        logger.i("[forsuredb.query] %s", bindColumnsFirst(pssql.getSql(), pssql.getReplacements()));
    }

    public static void logWith(@Nullable FSLogger logger, @Nonnull String message, @Nonnull Object... replacements) {
        if (loggingDisabled || logger == null) {
            return;
        }

        logger.i(message, replacements);
    }

    private static String stringify(Object o) {
        if (!o.getClass().isArray() || o.getClass().getComponentType() != byte.class) {
            return String.valueOf(o);
        }

        int len = Array.getLength(o);
        byte[] asArray = new byte[len];
        for (int idx = 0; idx < len; idx++) {
            asArray[idx] = Array.getByte(o, idx);
        }
        return Arrays.toString(asArray);
    }

    private static String bindColumnsFirst(String toBind, List<String> columns, RecordContainer recordContainer) {
        int pos;
        for (pos = 0; pos < columns.size(); pos++) {
            toBind = bindToFirst(toBind, recordContainer.get(columns.get(pos)));
        }
        return toBind;
    }

    private static String bindColumnsFirst(String toBind, String[] values) {
        if (values == null) {
            return toBind;
        }

        for (String replacement : values) {
            toBind = bindToFirst(toBind, replacement);
        }
        return toBind;
    }

    private static String bindToFirst(String toBind, Object o) {
        return toBind.replaceFirst("\\?", stringify(o));
    }
}
