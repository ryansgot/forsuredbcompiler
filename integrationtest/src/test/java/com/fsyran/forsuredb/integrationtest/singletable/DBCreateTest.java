package com.fsyran.forsuredb.integrationtest.singletable;

import com.fsryan.forsuredb.FSDBHelper;
import com.fsryan.forsuredb.api.Retriever;
import com.fsryan.forsuredb.integrationtest.singletable.AllTypesTable;
import com.fsyran.forsuredb.integrationtest.DBSetup;
import com.fsyran.forsuredb.integrationtest.ExecutionLog;
import com.fsyran.forsuredb.integrationtest.SqlMasterVerify;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import static com.fsryan.forsuredb.integrationtest.ForSure.allTypesTable;
import static com.fsyran.forsuredb.integrationtest.TestUtil.SMALL_DOUBLE;
import static com.fsyran.forsuredb.integrationtest.TestUtil.SMALL_FLOAT;
import static com.fsyran.forsuredb.integrationtest.TestUtil.bytesFromHex;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({DBSetup.class, ExecutionLog.class})
public class DBCreateTest {

    private static Connection connection;
    private static AllTypesTable allTypesTable;

    @BeforeAll
    public static void initConnection() throws SQLException {
        connection = FSDBHelper.inst().getReadableDatabase();
        allTypesTable = allTypesTable().getApi();
    }

    @Test
    public void shouldBeInitialized() {
        assertNotNull(FSDBHelper.inst());
    }

    @Test
    public void shouldHaveAllTypesTable() throws SQLException {
        SqlMasterVerify.tableExists(connection, "all_types");
    }

    @Test
    public void shouldHaveNonUniqueIndexOnIntegerWrapperColumn() throws SQLException {
        SqlMasterVerify.nonUniqueIndexExists(connection, "all_types", "integer_wrapper_column");
    }

    @Test
    public void shouldHaveUniqueIndexOnStringColumn() throws SQLException {
        SqlMasterVerify.uniqueIndexExists(connection, "all_types", "string_column");
    }

    // see all_types_static_data.xml for values
    @Test
    public void verifyCorrectStaticDataInsertion() throws SQLException {
        final Retriever r = allTypesTable().get();
        assertTrue(r.moveToPosition(1));
        verifyItemAtPosition(
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
        verifyItemAtPosition(
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
        verifyDefaultValuesAtPosition(
                r,
                3L,
                42
        );
        assertFalse(r.moveToNext());
        r.close();
    }

    private static void verifyItemAtPosition(Retriever r,
                                             long id,
                                             BigDecimal expectedBigDecimalColumn,
                                             BigInteger expectedBigIntegerColumn,
                                             boolean expectedBooleanColumn,
                                             Boolean expectedBooleanWrapperColumn,
                                             byte[] expectedBytArrayColumn,
                                             Date expectedDateColumn,
                                             double expectedDoubleColumn,
                                             Double expectedDoubleWrapperColumn,
                                             float expetedFloatColumn,
                                             Float expectedFloatWrapperColumn,
                                             int expectedIntColumn,
                                             Integer expectedIntegerWrapperColumn,
                                             long expectedLongColumn,
                                             Long expectedLongWrapperColumn,
                                             String expectedStringColumn) {
        assertEquals(id, allTypesTable.id(r));
        assertEquals(expectedBigDecimalColumn, allTypesTable.bigDecimalColumn(r));
        assertEquals(expectedBigIntegerColumn, allTypesTable.bigIntegerColumn(r));
        assertEquals(expectedBooleanColumn, allTypesTable.booleanColumn(r));
        assertEquals(expectedBooleanWrapperColumn, allTypesTable.booleanWrapperColumn(r));
        assertArrayEquals(expectedBytArrayColumn, allTypesTable.byteArrayColumn(r));
        assertEquals(expectedDateColumn, allTypesTable.dateColumn(r));
        assertEquals(expectedDoubleColumn, allTypesTable.doubleColumn(r), SMALL_DOUBLE);
        assertEquals(expectedDoubleWrapperColumn, allTypesTable.doubleWrapperColumn(r), SMALL_DOUBLE);
        assertEquals(expetedFloatColumn, allTypesTable.floatColumn(r), SMALL_FLOAT);
        assertEquals(expectedFloatWrapperColumn, allTypesTable.floatWrapperColumn(r), SMALL_FLOAT);
        assertEquals(expectedIntColumn, allTypesTable.intColumn(r));
        assertEquals(expectedIntegerWrapperColumn, allTypesTable.integerWrapperColumn(r));
        assertEquals(expectedLongColumn, allTypesTable.longColumn(r));
        assertEquals(expectedLongWrapperColumn, allTypesTable.longWrapperColumn(r));
        assertEquals(expectedStringColumn, allTypesTable.stringColumn(r));
    }

    private static void verifyDefaultValuesAtPosition(Retriever r, long id, int expectedIntColumn) {
        assertEquals(id, allTypesTable.id(r));
        assertEquals(expectedIntColumn, allTypesTable.intColumn(r));
    }
}
