package com.fsyran.forsuredb.integrationtest.singletable;

import com.fsryan.forsuredb.FSDBHelper;
import com.fsryan.forsuredb.api.Retriever;
import com.fsyran.forsuredb.integrationtest.DBSetup;
import com.fsyran.forsuredb.integrationtest.ExecutionLog;
import com.fsyran.forsuredb.integrationtest.ForceMigrationsExtension;
import com.fsyran.forsuredb.integrationtest.SqlMasterAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import static com.fsryan.forsuredb.integrationtest.ForSure.allTypesTable;
import static com.fsyran.forsuredb.integrationtest.TestUtil.bytesFromHex;
import static com.fsyran.forsuredb.integrationtest.singletable.AllTypesTableTestUtil.verifyDefaultValuesAtCurrentPosition;
import static com.fsyran.forsuredb.integrationtest.singletable.AllTypesTableTestUtil.verifyColumnsAtCurrentPosition;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({ForceMigrationsExtension.class, ExecutionLog.class})
public class DBCreateTest {

    private static Connection connection;

    @BeforeAll
    public static void initConnection() throws SQLException {
        connection = FSDBHelper.inst().getReadableDatabase();
    }

    @Test
    @DisplayName("FSDBHelper should be initialized")
    public void shouldBeInitialized() {
        assertNotNull(FSDBHelper.inst());
    }

    @Test
    @DisplayName("all_types table should exist")
    public void shouldHaveAllTypesTable() throws SQLException {
        SqlMasterAssertions.tableExists(connection, "all_types");
    }

    @Test
    @DisplayName("non-unique index should should be on integer_wrapper_column")
    public void shouldHaveNonUniqueIndexOnIntegerWrapperColumn() throws SQLException {
        SqlMasterAssertions.nonUniqueIndexExists(connection, "all_types", "integer_wrapper_column");
    }

    @Test
    @DisplayName("unique index should shoudl be on string_column")
    public void shouldHaveUniqueIndexOnStringColumn() throws SQLException {
        SqlMasterAssertions.uniqueIndexExists(connection, "all_types", "string_column");
    }

    @Test
    @DisplayName("all static data should be inserted as in all_types_static_data.xml")
    public void verifyCorrectStaticDataInsertion() throws SQLException {
        final Retriever r = allTypesTable().get();
        assertTrue(r.moveToPosition(1));
        verifyColumnsAtCurrentPosition(
                r,
                1L,
                new BigDecimal("476583283.8932675346289"),
                new BigInteger("7984365438954389567894358943576349832"),
                true,
                false,
                bytesFromHex("7526ff726d95e170f09a14a1261110d4d3368cf8"),
                new Date(1514015639999L), // <-- 2017-12-22 23:53:59.999
                5325.3874D,
                5.438439D,
                1.1F,
                8.7F,
                5,
                8758503,
                40585L,
                74745094847459L,
                "Record 1"
        );
        assertTrue(r.moveToPosition(2));
        verifyColumnsAtCurrentPosition(
                r,
                2L,
                new BigDecimal("2.1"),
                new BigInteger("2"),
                false,
                true,
                bytesFromHex("b906dd9a5570586bf38e58a97470d28fb872abdf"),
                new Date(1514015939999L), // <-- Fri Dec 22 23:58:59 PST 2017
                5325.3D,
                5.6D,
                2.2F,
                8.7F,
                5,
                65432,
                9L,
                4532L,
                "Record 2"
        );
        assertTrue(r.moveToPosition(3));
        // This was a default-column
        verifyDefaultValuesAtCurrentPosition(r, 3L, 42);
        assertFalse(r.moveToNext());
        r.close();
    }
}
