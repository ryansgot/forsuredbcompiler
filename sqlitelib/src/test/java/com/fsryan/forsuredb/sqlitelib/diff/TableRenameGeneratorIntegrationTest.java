package com.fsryan.forsuredb.sqlitelib.diff;

import com.fsryan.forsuredb.sqlite.SqliteMasterAssertions;
import com.fsryan.forsuredb.sqlitelib.FreshDBForClassExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;

@ExtendWith(FreshDBForClassExtension.class)
public class TableRenameGeneratorIntegrationTest {

    @Test
    @DisplayName("Should successfully rename table with generated SQL")
    public void applyTableRename(Connection conn) throws Exception {
        try (PreparedStatement statement = conn.prepareStatement("CREATE TABLE IF NOT EXISTS t1(_id INTEGER PRIMARY KEY);")) {
            statement.execute();
        }

        List<String> statements = new TableRenameGenerator("t1", "t1_renamed").statements();
        for (String sql : statements) {
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.execute();
            }
        }

        SqliteMasterAssertions.makeAllAssertions(conn, Arrays.asList(
                SqliteMasterAssertions.forTableNotExists("t1"),
                SqliteMasterAssertions.forTableExists("t1_renamed")
        ));
    }
}
