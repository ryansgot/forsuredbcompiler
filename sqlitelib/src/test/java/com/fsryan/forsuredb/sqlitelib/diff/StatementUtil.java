package com.fsryan.forsuredb.sqlitelib.diff;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public abstract class StatementUtil {

    public static void executeScript(@Nonnull Connection conn, @Nonnull List<String> script) throws SQLException {
        for (String sql : script) {
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.execute();
            }
        }
    }
}
