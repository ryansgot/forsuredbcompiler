package com.fsyran.forsuredb.integrationtest.singletable;

import com.fsryan.forsuredb.FSDBHelper;
import com.fsryan.forsuredb.queryable.StatementBinder;
import com.fsyran.forsuredb.integrationtest.DBSetup;
import com.fsyran.forsuredb.integrationtest.ExecutionLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({DBSetup.class, ExecutionLog.class})
public class DBCreateTest {

    @Test
    public void shouldBeInitialized() {
        assertNotNull(FSDBHelper.inst());
    }

    @Test
    public void shouldHaveAllTypesTable() throws SQLException {
        Connection c = FSDBHelper.inst().getReadableDatabase();

        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM SQLITE_MASTER WHERE type = ? AND tbl_name = ?")) {
            StatementBinder.bindObject(1, ps,"table");
            StatementBinder.bindObject(2, ps, "all_types");
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next());
        }
    }
}
