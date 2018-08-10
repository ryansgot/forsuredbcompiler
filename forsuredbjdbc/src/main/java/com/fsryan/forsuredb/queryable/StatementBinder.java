package com.fsryan.forsuredb.queryable;

import com.fsryan.forsuredb.api.RecordContainer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

public abstract class StatementBinder {

    public static void bindObjects(PreparedStatement pStatement, List<String> columns, RecordContainer recordContainer) throws SQLException {
        for (int pos = 0; pos < columns.size(); pos ++) {
            bindObject(pos + 1, pStatement, recordContainer.get(columns.get(pos)));
        }
    }

    public static void bindObject(int idx, PreparedStatement pStatement, Object obj) throws SQLException {
        if (obj == null) {
            pStatement.setNull(idx, Types.NULL);
            return;
        }

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
