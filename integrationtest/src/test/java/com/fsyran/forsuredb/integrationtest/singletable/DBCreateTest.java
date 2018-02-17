package com.fsyran.forsuredb.integrationtest.singletable;

import com.fsryan.forsuredb.FSDBHelper;
import com.fsryan.forsuredb.queryable.StatementBinder;
import com.fsyran.forsuredb.integrationtest.DBSetup;
import com.fsyran.forsuredb.integrationtest.ExecutionLog;
import com.fsyran.forsuredb.integrationtest.SqlMasterVerify;
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
        final Connection c = FSDBHelper.inst().getReadableDatabase();
        SqlMasterVerify.tableExists(c, "all_types");
    }

    @Test
    public void shouldHaveNonUniqueIndexOn() throws SQLException {
        final Connection c = FSDBHelper.inst().getReadableDatabase();
        SqlMasterVerify.indexExists(c, "all_types", "integer_wrapper_column", false);
    }
}
