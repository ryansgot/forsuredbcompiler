package com.fsryan.forsuredb.resultset;

import com.fsryan.forsuredb.api.Retriever;
import org.junit.*;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

import static org.mockito.Mockito.*;

public class FSResultSetTest {
    
    private static final int columnIndex = 1;
    private static final int scale = 2;
    private static final int row = 42;
    private static final int direction = 0;
    private static final String columnLabel = "columnLabel";
    private static final byte[] byteArray = new byte[] {1, 2, 3, 4, 5, 6, 7, 8};

    private static InputStream byteArrayInputStream;

    @BeforeClass
    public static void setUpInputStream() {
        byteArrayInputStream = new ByteArrayInputStream(byteArray);
    }

    @AfterClass
    public static void tearDownInputStream() throws Exception {
        byteArrayInputStream.close();
    }

    @Mock
    private ResultSet mockResultSet;

    private FSResultSet fsResultSetUnderTest;

    @Before
    public void setUpTest() {
        MockitoAnnotations.initMocks(this);
        fsResultSetUnderTest = new FSResultSet(mockResultSet);
    }
    
    @After
    public void verifyNoMoreInteractionsWithMockResultSet() {
        verifyNoMoreInteractions(mockResultSet);
    }

    @Test
    public void shouldPassThroughNext() throws SQLException {
        fsResultSetUnderTest.next();
        verify(mockResultSet).next();
    }

    @Test
    public void shouldPassThroughClose() throws SQLException {
        fsResultSetUnderTest.close();
        verify(mockResultSet).close();
    }

    @Test
    public void shouldPassThroughWasNull() throws SQLException {
        fsResultSetUnderTest.wasNull();
        verify(mockResultSet).wasNull();
    }

    @Test
    public void shouldPassThroughGetStringByIndex() throws SQLException {
        fsResultSetUnderTest.getString(columnIndex);
        verify(mockResultSet).getString(columnIndex);
    }

    @Test
    public void shouldPassThroughGetBooleanByIndex() throws SQLException {
        fsResultSetUnderTest.getBoolean(columnIndex);
        verify(mockResultSet).getBoolean(columnIndex);
    }

    @Test
    public void shouldPassThroughGetByteByIndex() throws SQLException {
        fsResultSetUnderTest.getByte(columnIndex);
        verify(mockResultSet).getByte(columnIndex);
    }

    @Test
    public void shouldPassThroughGetShortByIndex() throws SQLException {
        fsResultSetUnderTest.getShort(columnIndex);
        verify(mockResultSet).getShort(columnIndex);
    }

    @Test
    public void shouldPassThroughGetIntByIndex() throws SQLException {
        fsResultSetUnderTest.getInt(columnIndex);
        verify(mockResultSet).getInt(columnIndex);
    }

    @Test
    public void shouldPassThroughGetLongByIndex() throws SQLException {
        fsResultSetUnderTest.getLong(columnIndex);
        verify(mockResultSet).getLong(columnIndex);
    }

    @Test
    public void shouldPassThroughGetFloatByIndex() throws SQLException {
        fsResultSetUnderTest.getFloat(columnIndex);
        verify(mockResultSet).getFloat(columnIndex);
    }

    @Test
    public void shouldPassThroughGetDoubleByIndex() throws SQLException {
        fsResultSetUnderTest.getDouble(columnIndex);
        verify(mockResultSet).getDouble(columnIndex);
    }

    @Test
    public void shouldPassThroughGetBigDecimalByIndexAndScale() throws SQLException {
        fsResultSetUnderTest.getBigDecimal(columnIndex, scale);
        verify(mockResultSet).getBigDecimal(columnIndex, scale);
    }

    @Test
    public void shouldPassThroughGetBytesByIndex() throws SQLException {
        fsResultSetUnderTest.getBytes(columnIndex);
        verify(mockResultSet).getBytes(columnIndex);
    }

    @Test
    public void shouldPassThroughGetDateByIndex() throws SQLException {
        fsResultSetUnderTest.getDate(columnIndex);
        verify(mockResultSet).getDate(columnIndex);
    }

    @Test
    public void shouldPassThroughGetTimeByIndex() throws SQLException {
        fsResultSetUnderTest.getTime(columnIndex);
        verify(mockResultSet).getTime(columnIndex);
    }

    @Test
    public void shouldPassThroughGetTimestampByIndex() throws SQLException {
        fsResultSetUnderTest.getTimestamp(columnIndex);
        verify(mockResultSet).getTimestamp(columnIndex);
    }

    @Test
    public void shouldPassThroughGetAsciiStreamByIndex() throws SQLException {
        fsResultSetUnderTest.getAsciiStream(columnIndex);
        verify(mockResultSet).getAsciiStream(columnIndex);
    }

    @Test
    public void shouldPassThroughGetUnicodeStreamByIndex() throws SQLException {
        fsResultSetUnderTest.getUnicodeStream(columnIndex);
        verify(mockResultSet).getUnicodeStream(columnIndex);
    }

    @Test
    public void shouldPassThroughGetBinaryStreamByIndex() throws SQLException {
        fsResultSetUnderTest.getBinaryStream(columnIndex);
        verify(mockResultSet).getBinaryStream(columnIndex);
    }

    @Test
    public void shouldPassThroughGetStringByColumnLabel() throws SQLException {
        fsResultSetUnderTest.getString(columnLabel);
        verify(mockResultSet).getString(columnLabel);
    }

    @Test
    public void shouldPassThroughGetBooleanByColumnLabel() throws SQLException {
        fsResultSetUnderTest.getBoolean(columnLabel);
        verify(mockResultSet).getBoolean(columnLabel);
    }

    @Test
    public void shouldPassThroughGetByteByColumnLabel() throws SQLException {
        fsResultSetUnderTest.getByte(columnLabel);
        verify(mockResultSet).getByte(columnLabel);
    }

    @Test
    public void shouldPassThroughGetShortByColumnLabel() throws SQLException {
        fsResultSetUnderTest.getShort(columnLabel);
        verify(mockResultSet).getShort(columnLabel);
    }

    @Test
    public void shouldPassThroughGetIntByColumnLabel() throws SQLException {
        fsResultSetUnderTest.getInt(columnLabel);
        verify(mockResultSet).getInt(columnLabel);
    }

    @Test
    public void shouldPassThroughGetLongByColumnLabel() throws SQLException {
        fsResultSetUnderTest.getLong(columnLabel);
        verify(mockResultSet).getLong(columnLabel);
    }

    @Test
    public void shouldPassThroughGetFloatByColumnLabel() throws SQLException {
        fsResultSetUnderTest.getFloat(columnLabel);
        verify(mockResultSet).getFloat(columnLabel);
    }

    @Test
    public void shouldPassThroughGetDoubleByColumnLabel() throws SQLException {
        fsResultSetUnderTest.getDouble(columnLabel);
        verify(mockResultSet).getDouble(columnLabel);
    }

    @Test
    public void shouldPassThroughGetBigDecimalByColumnLabelAndScale() throws SQLException {
        fsResultSetUnderTest.getBigDecimal(columnLabel ,scale);
        verify(mockResultSet).getBigDecimal(columnLabel ,scale);
    }

    @Test
    public void shouldPassThroughGetBytesByColumnLabel() throws SQLException {
        fsResultSetUnderTest.getBytes(columnLabel);
        verify(mockResultSet).getBytes(columnLabel);
    }

    @Test
    public void shouldPassThroughGetDateByColumnLabel() throws SQLException {
        fsResultSetUnderTest.getDate(columnLabel);
        verify(mockResultSet).getDate(columnLabel);
    }

    @Test
    public void shouldPassThroughGetTimeByColumnLabel() throws SQLException {
        fsResultSetUnderTest.getTime(columnLabel);
        verify(mockResultSet).getTime(columnLabel);
    }

    @Test
    public void shouldPassThroughGetTimestampByColumnLabel() throws SQLException {
        fsResultSetUnderTest.getTimestamp(columnLabel);
        verify(mockResultSet).getTimestamp(columnLabel);
    }

    @Test
    public void shouldPassThroughGetAsciiStreamByColumnLabel() throws SQLException {
        fsResultSetUnderTest.getAsciiStream(columnLabel);
        verify(mockResultSet).getAsciiStream(columnLabel);
    }

    @Test
    public void shouldPassThroughGetUnicodeStreamByColumnLabel() throws SQLException {
        fsResultSetUnderTest.getUnicodeStream(columnLabel);
        verify(mockResultSet).getUnicodeStream(columnLabel);
    }

    @Test
    public void shouldPassThroughGetBinaryStreamByColumnLabel() throws SQLException {
        fsResultSetUnderTest.getBinaryStream(columnLabel);
        verify(mockResultSet).getBinaryStream(columnLabel);
    }

    @Test
    public void shouldPassThroughGetWarningsByColumnLabel() throws SQLException {
        fsResultSetUnderTest.getWarnings();
        verify(mockResultSet).getWarnings();
    }

    @Test
    public void shouldPassThroughClearWarnings() throws SQLException {
        fsResultSetUnderTest.clearWarnings();
        verify(mockResultSet).clearWarnings();
    }

    @Test
    public void shouldPassThroughGetCursorName() throws SQLException {
        fsResultSetUnderTest.getCursorName();
        verify(mockResultSet).getCursorName();
    }

    @Test
    public void shouldPassThroughGetMetaData() throws SQLException {
        fsResultSetUnderTest.getMetaData();
        verify(mockResultSet).getMetaData();
    }

    @Test
    public void shouldPassThroughGetObjectByIndex() throws SQLException {
        fsResultSetUnderTest.getObject(columnIndex);
        verify(mockResultSet).getObject(columnIndex);
    }

    @Test
    public void shouldPassThroughGetObjectByColumnLabel() throws SQLException {
        fsResultSetUnderTest.getObject(columnLabel);
        verify(mockResultSet).getObject(columnLabel);
    }

    @Test
    public void shouldPassThroughFindColumnByColumnLabel() throws SQLException {
        fsResultSetUnderTest.findColumn(columnLabel);
        verify(mockResultSet).findColumn(columnLabel);
    }

    @Test
    public void shouldPassThroughGetCharacterStreamByColumnIndex() throws SQLException {
        fsResultSetUnderTest.getCharacterStream(columnIndex);
        verify(mockResultSet).getCharacterStream(columnIndex);
    }

    @Test
    public void shouldPassThroughGetCharacterStreamByColumnLabel() throws SQLException {
        fsResultSetUnderTest.getCharacterStream(columnLabel);
        verify(mockResultSet).getCharacterStream(columnLabel);
    }

    @Test
    public void shouldPassThroughGetBigDecimalByIndex() throws SQLException {
        fsResultSetUnderTest.getBigDecimal(columnIndex);
        verify(mockResultSet).getBigDecimal(columnIndex);
    }

    @Test
    public void shouldPassThroughGetBigDecimalByColumnLabel() throws SQLException {
        fsResultSetUnderTest.getBigDecimal(columnLabel);
        verify(mockResultSet).getBigDecimal(columnLabel);
    }

    @Test
    public void shouldPassThroughIsBeforeFirst() throws SQLException {
        fsResultSetUnderTest.isBeforeFirst();
        verify(mockResultSet).isBeforeFirst();
    }

    @Test
    public void shouldPassThroughIsAfterLast() throws SQLException {
        fsResultSetUnderTest.isAfterLast();
        verify(mockResultSet).isAfterLast();
    }

    @Test
    public void shouldPassThroughIsFirst() throws SQLException {
        fsResultSetUnderTest.isFirst();
        verify(mockResultSet).isFirst();
    }

    @Test
    public void shouldPassThroughIsLast() throws SQLException {
        fsResultSetUnderTest.isLast();
        verify(mockResultSet).isLast();
    }

    @Test
    public void shouldPassThroughBeforeFirst() throws SQLException {
        fsResultSetUnderTest.beforeFirst();
        verify(mockResultSet).beforeFirst();
    }

    @Test
    public void shouldPassThroughAfterLast() throws SQLException {
        fsResultSetUnderTest.afterLast();
        verify(mockResultSet).afterLast();
    }

    @Test
    public void shouldPassThroughFirst() throws SQLException {
        fsResultSetUnderTest.first();
        verify(mockResultSet).first();
    }

    @Test
    public void shouldPassThroughLast() throws SQLException {
        fsResultSetUnderTest.last();
        verify(mockResultSet).last();
    }

    @Test
    public void shouldPassThroughGetRow() throws SQLException {
        fsResultSetUnderTest.getRow();
        verify(mockResultSet).getRow();
    }

    @Test
    public void shouldPassThroughAbsolute() throws SQLException {
        fsResultSetUnderTest.absolute(row);
        verify(mockResultSet).absolute(row);
    }

    @Test
    public void shouldPassThroughRelative() throws SQLException {
        fsResultSetUnderTest.relative(row);
        verify(mockResultSet).relative(row);
    }

    @Test
    public void shouldPassThrougPrevious() throws SQLException {
        fsResultSetUnderTest.previous();
        verify(mockResultSet).previous();
    }

    @Test
    public void shouldPassThrougSetFetchDirection() throws SQLException {
        fsResultSetUnderTest.setFetchDirection(direction);
        verify(mockResultSet).setFetchDirection(direction);
    }

    @Test
    public void shouldPassThrougGetFetchDirection() throws SQLException {
        fsResultSetUnderTest.getFetchDirection();
        verify(mockResultSet).getFetchDirection();
    }

    @Test
    public void shouldPassThroughSetFetchSize() throws SQLException {
        fsResultSetUnderTest.setFetchSize(row);
        verify(mockResultSet).setFetchSize(row);
    }

    @Test
    public void shouldPassThroughGetFetchSize() throws SQLException {
        fsResultSetUnderTest.getFetchSize();
        verify(mockResultSet).getFetchSize();
    }

    @Test
    public void shouldPassThroughGetType() throws SQLException {
        fsResultSetUnderTest.getType();
        verify(mockResultSet).getType();
    }

    @Test
    public void shouldPassThroughGetConcurrency() throws SQLException {
        fsResultSetUnderTest.getConcurrency();
        verify(mockResultSet).getConcurrency();
    }

    @Test
    public void shouldPassThroughRowUpdated() throws SQLException {
        fsResultSetUnderTest.rowUpdated();
        verify(mockResultSet).rowUpdated();
    }

    @Test
    public void shouldPassThroughRowInserted() throws SQLException {
        fsResultSetUnderTest.rowInserted();
        verify(mockResultSet).rowInserted();
    }

    @Test
    public void shouldPassThroughRowDeleted() throws SQLException {
        fsResultSetUnderTest.rowDeleted();
        verify(mockResultSet).rowDeleted();
    }

    @Test
    public void shouldPassThroughUpdateNullByIndex() throws SQLException {
        fsResultSetUnderTest.updateNull(columnIndex);
        verify(mockResultSet).updateNull(columnIndex);
    }

    @Test
    public void shouldPassThroughUpdateNullByIndexAndValue() throws SQLException {
        fsResultSetUnderTest.updateBoolean(columnIndex, true);
        verify(mockResultSet).updateBoolean(columnIndex, true);
    }

    @Test
    public void shouldPassThroughUpdateByteByIndexAndValue() throws SQLException {
        fsResultSetUnderTest.updateByte(columnIndex, Byte.MAX_VALUE);
        verify(mockResultSet).updateByte(columnIndex, Byte.MAX_VALUE);
    }

    @Test
    public void shouldPassThroughUpdateShortByIndexAndValue() throws SQLException {
        fsResultSetUnderTest.updateShort(columnIndex, Short.MAX_VALUE);
        verify(mockResultSet).updateShort(columnIndex, Short.MAX_VALUE);
    }

    @Test
    public void shouldPassThroughUpdateIntByIndexAndValue() throws SQLException {
        fsResultSetUnderTest.updateInt(columnIndex, Integer.MAX_VALUE);
        verify(mockResultSet).updateInt(columnIndex, Integer.MAX_VALUE);
    }

    @Test
    public void shouldPassThroughUpdateLongByIndexAndValue() throws SQLException {
        fsResultSetUnderTest.updateLong(columnIndex, Long.MAX_VALUE);
        verify(mockResultSet).updateLong(columnIndex, Long.MAX_VALUE);
    }

    @Test
    public void shouldPassThroughUpdateFloatByIndexAndValue() throws SQLException {
        fsResultSetUnderTest.updateFloat(columnIndex, Float.MAX_VALUE);
        verify(mockResultSet).updateFloat(columnIndex, Float.MAX_VALUE);
    }

    @Test
    public void shouldPassThroughUpdateDoubleByIndexAndValue() throws SQLException {
        fsResultSetUnderTest.updateDouble(columnIndex, Double.MAX_VALUE);
        verify(mockResultSet).updateDouble(columnIndex, Double.MAX_VALUE);
    }

    @Test
    public void shouldPassThroughUpdateBigDecimalByIndexAndValue() throws SQLException {
        fsResultSetUnderTest.updateBigDecimal(columnIndex, BigDecimal.ONE);
        verify(mockResultSet).updateBigDecimal(columnIndex, BigDecimal.ONE);
    }

    @Test
    public void shouldPassThroughUpdateStringByIndexAndValue() throws SQLException {
        fsResultSetUnderTest.updateString(columnIndex, "string_value");
        verify(mockResultSet).updateString(columnIndex, "string_value");
    }

    @Test
    public void shouldPassThroughUpdateBytesByIndexAndValue() throws SQLException {
        fsResultSetUnderTest.updateBytes(columnIndex, byteArray);
        verify(mockResultSet).updateBytes(columnIndex, byteArray);
    }

    @Test
    public void shouldPassThroughUpdateDateByIndexAndValue() throws SQLException {
        Date d = new Date(0);
        fsResultSetUnderTest.updateDate(columnIndex, d);
        verify(mockResultSet).updateDate(columnIndex, d);
    }

    @Test
    public void shouldPassThroughUpdateTimeByIndexAndValue() throws SQLException {
        Time t = new Time(0L);
        fsResultSetUnderTest.updateTime(columnIndex, t);
        verify(mockResultSet).updateTime(columnIndex, t);
    }

    @Test
    public void shouldPassThroughUpdateTimestampByIndexAndValue() throws SQLException {
        Timestamp ts = new Timestamp(0L);
        fsResultSetUnderTest.updateTimestamp(columnIndex, ts);
        verify(mockResultSet).updateTimestamp(columnIndex, ts);
    }

    @Test
    public void shouldPassThroughUpdateAsciiStreamByIndexAndValueAndLength() throws SQLException {
        fsResultSetUnderTest.updateAsciiStream(columnIndex, byteArrayInputStream, byteArray.length);
        verify(mockResultSet).updateAsciiStream(columnIndex, byteArrayInputStream, byteArray.length);
    }

    @Test
    public void shouldPassThroughUpdateBinaryStreamByIndexAndValueAndLength() throws SQLException {
        fsResultSetUnderTest.updateBinaryStream(columnIndex, byteArrayInputStream, byteArray.length);
        verify(mockResultSet).updateBinaryStream(columnIndex, byteArrayInputStream, byteArray.length);
    }

    @Test
    public void shouldPassThroughUpdateCharacterStreamByIndexAndReaderAndLength() throws SQLException {
        Reader reader = mock(Reader.class);
        fsResultSetUnderTest.updateCharacterStream(columnIndex, reader, 1);
        verify(mockResultSet).updateCharacterStream(columnIndex, reader, 1);
    }

    @Test
    public void shouldPassThroughUpdateObjectByIndexAndValueAndScaleOrLength() throws SQLException {
        Object o = new Object();
        fsResultSetUnderTest.updateObject(columnIndex, o, scale);
        verify(mockResultSet).updateObject(columnIndex, o, scale);
    }

    @Test
    public void shouldPassThroughUpdateObjectByIndexAndValue() throws SQLException {
        Object o = new Object();
        fsResultSetUnderTest.updateObject(columnIndex, o);
        verify(mockResultSet).updateObject(columnIndex, o);
    }

    @Test
    public void shouldPassThroughUpdateObjectByColumnLabel() throws SQLException {
        fsResultSetUnderTest.updateNull(columnLabel);
        verify(mockResultSet).updateNull(columnLabel);
    }

    @Test
    public void shouldPassThroughUpdateBooleanByColumnLabelAndValue() throws SQLException {
        fsResultSetUnderTest.updateBoolean(columnLabel, false);
        verify(mockResultSet).updateBoolean(columnLabel, false);
    }

    @Test
    public void shouldPassThroughUpdateByteByColumnLabelAndValue() throws SQLException {
        fsResultSetUnderTest.updateByte(columnLabel, Byte.MAX_VALUE);
        verify(mockResultSet).updateByte(columnLabel, Byte.MAX_VALUE);
    }

    @Test
    public void shouldPassThroughUpdateShortByColumnLabelAndValue() throws SQLException {
        fsResultSetUnderTest.updateShort(columnLabel, Short.MAX_VALUE);
        verify(mockResultSet).updateShort(columnLabel, Short.MAX_VALUE);
    }

    @Test
    public void shouldPassThroughUpdateIntByColumnLabelAndValue() throws SQLException {
        fsResultSetUnderTest.updateInt(columnLabel, Integer.MAX_VALUE);
        verify(mockResultSet).updateInt(columnLabel, Integer.MAX_VALUE);
    }

    @Test
    public void shouldPassThroughUpdateLongByColumnLabelAndValue() throws SQLException {
        fsResultSetUnderTest.updateLong(columnLabel, Long.MAX_VALUE);
        verify(mockResultSet).updateLong(columnLabel, Long.MAX_VALUE);
    }

    @Test
    public void shouldPassThroughUpdateFloatByColumnLabelAndValue() throws SQLException {
        fsResultSetUnderTest.updateFloat(columnLabel, Float.MAX_VALUE);
        verify(mockResultSet).updateFloat(columnLabel, Float.MAX_VALUE);
    }

    @Test
    public void shouldPassThroughUpdateDoubleByColumnLabelAndValue() throws SQLException {
        fsResultSetUnderTest.updateDouble(columnLabel, Double.MAX_VALUE);
        verify(mockResultSet).updateDouble(columnLabel, Double.MAX_VALUE);
    }

    @Test
    public void shouldPassThroughUpdateBigDecimalByColumnLabelAndValue() throws SQLException {
        fsResultSetUnderTest.updateBigDecimal(columnLabel, BigDecimal.TEN);
        verify(mockResultSet).updateBigDecimal(columnLabel, BigDecimal.TEN);
    }

    @Test
    public void shouldPassThroughUpdateStringByColumnLabelAndValue() throws SQLException {
        fsResultSetUnderTest.updateString(columnLabel, "string_value");
        verify(mockResultSet).updateString(columnLabel, "string_value");
    }

    @Test
    public void shouldPassThroughUpdateBytesByColumnLabelAndValue() throws SQLException {
        fsResultSetUnderTest.updateBytes(columnLabel, byteArray);
        verify(mockResultSet).updateBytes(columnLabel, byteArray);
    }

    @Test
    public void shouldPassThroughUpdateDateByColumnLabelAndValue() throws SQLException {
        Date d = new Date(0L);
        fsResultSetUnderTest.updateDate(columnLabel, d);
        verify(mockResultSet).updateDate(columnLabel, d);
    }

    @Test
    public void shouldPassThroughUpdateTimeByColumnLabelAndValue() throws SQLException {
        Time t = new Time(0L);
        fsResultSetUnderTest.updateTime(columnLabel, t);
        verify(mockResultSet).updateTime(columnLabel, t);
    }

    @Test
    public void shouldPassThroughUpdateTimestampByColumnLabelAndValue() throws SQLException {
        Timestamp t = new Timestamp(0L);
        fsResultSetUnderTest.updateTimestamp(columnLabel, t);
        verify(mockResultSet).updateTimestamp(columnLabel, t);
    }

    @Test
    public void shouldPassThroughUpdateAsciiStreamByColumnLabelAndStreamAndLength() throws SQLException {
        fsResultSetUnderTest.updateAsciiStream(columnLabel, byteArrayInputStream, byteArray.length);
        verify(mockResultSet).updateAsciiStream(columnLabel, byteArrayInputStream, byteArray.length);
    }

    @Test
    public void shouldPassThroughUpdateBinaryStreamByColumnLabelAndStreamAndLength() throws SQLException {
        fsResultSetUnderTest.updateBinaryStream(columnLabel, byteArrayInputStream, byteArray.length);
        verify(mockResultSet).updateBinaryStream(columnLabel, byteArrayInputStream, byteArray.length);
    }

    @Test
    public void shouldPassThroughUpdateCharacterStreamByColumnLabelAndStreamAndLength() throws SQLException {
        Reader r = mock(Reader.class);
        fsResultSetUnderTest.updateCharacterStream(columnLabel, r, 1);
        verify(mockResultSet).updateCharacterStream(columnLabel, r, 1);
    }

    @Test
    public void shouldPassThroughUpdateObjectByColumnLabelAndValueAndScaleOrLength() throws SQLException {
        Object o = new Object();
        fsResultSetUnderTest.updateObject(columnLabel, o, scale);
        verify(mockResultSet).updateObject(columnLabel, o, scale);
    }

    @Test
    public void shouldPassThroughUpdateObjectByColumnLabelAndValue() throws SQLException {
        Object o = new Object();
        fsResultSetUnderTest.updateObject(columnLabel, o);
        verify(mockResultSet).updateObject(columnLabel, o);
    }

    @Test
    public void shouldPassThroughInsertRow() throws SQLException {
        fsResultSetUnderTest.insertRow();
        verify(mockResultSet).insertRow();
    }

    @Test
    public void shouldPassThroughUpdateRow() throws SQLException {
        fsResultSetUnderTest.updateRow();
        verify(mockResultSet).updateRow();
    }

    @Test
    public void shouldPassThroughDeleteRow() throws SQLException {
        fsResultSetUnderTest.deleteRow();
        verify(mockResultSet).deleteRow();
    }

    @Test
    public void shouldPassThroughRefreshRow() throws SQLException {
        fsResultSetUnderTest.refreshRow();
        verify(mockResultSet).refreshRow();
    }

    @Test
    public void shouldPassThroughCancelRowUpdates() throws SQLException {
        fsResultSetUnderTest.cancelRowUpdates();
        verify(mockResultSet).cancelRowUpdates();
    }

    @Test
    public void shouldPassThroughMoveToInsertRow() throws SQLException {
        fsResultSetUnderTest.moveToInsertRow();
        verify(mockResultSet).moveToInsertRow();
    }

    @Test
    public void shouldPassThroughMoveToCurrentRow() throws SQLException {
        fsResultSetUnderTest.moveToCurrentRow();
        verify(mockResultSet).moveToCurrentRow();
    }

    @Test
    public void shouldPassThroughGetStatement() throws SQLException {
        fsResultSetUnderTest.getStatement();
        verify(mockResultSet).getStatement();
    }

    @Test
    public void shouldPassThroughGetObjectByIndexAndMapStringClass() throws SQLException {
        Map<String, Class<?>> mockMapStringClass = mock(Map.class);
        fsResultSetUnderTest.getObject(columnIndex, mockMapStringClass);
        verify(mockResultSet).getObject(columnIndex, mockMapStringClass);
    }

    @Test
    public void shouldPassThroughGetRefByIndex() throws SQLException {
        fsResultSetUnderTest.getRef(columnIndex);
        verify(mockResultSet).getRef(columnIndex);
    }

    @Test
    public void shouldPassThroughGetBlobByIndex() throws SQLException {
        fsResultSetUnderTest.getBlob(columnIndex);
        verify(mockResultSet).getBlob(columnIndex);
    }

    @Test
    public void shouldPassThroughGetClobByIndex() throws SQLException {
        fsResultSetUnderTest.getClob(columnIndex);
        verify(mockResultSet).getClob(columnIndex);
    }

    @Test
    public void shouldPassThroughGetArrayByIndex() throws SQLException {
        fsResultSetUnderTest.getArray(columnIndex);
        verify(mockResultSet).getArray(columnIndex);
    }

    @Test
    public void shouldPassThroughGetObjectByColumnLabelAndMapStringClass() throws SQLException {
        Map<String, Class<?>> mockMap = mock(Map.class);
        fsResultSetUnderTest.getObject(columnLabel, mockMap);
        verify(mockResultSet).getObject(columnLabel, mockMap);
    }

    @Test
    public void shouldPassThroughGetRefByColumnLabel() throws SQLException {
        fsResultSetUnderTest.getRef(columnLabel);
        verify(mockResultSet).getRef(columnLabel);
    }

    @Test
    public void shouldPassThroughGetBlobByColumnLabel() throws SQLException {
        fsResultSetUnderTest.getBlob(columnLabel);
        verify(mockResultSet).getBlob(columnLabel);
    }

    @Test
    public void shouldPassThroughGetClobByColumnLabel() throws SQLException {
        fsResultSetUnderTest.getClob(columnLabel);
        verify(mockResultSet).getClob(columnLabel);
    }

    @Test
    public void shouldPassThroughGetArrayByColumnLabel() throws SQLException {
        fsResultSetUnderTest.getArray(columnLabel);
        verify(mockResultSet).getArray(columnLabel);
    }

    @Test
    public void shouldPassThroughGetDateByIndexAndCalendar() throws SQLException {
        Calendar mockCalendar = mock(Calendar.class);
        fsResultSetUnderTest.getDate(columnIndex, mockCalendar);
        verify(mockResultSet).getDate(columnIndex, mockCalendar);
    }

    @Test
    public void shouldPassThroughGetDateByColumnLabelAndCalendar() throws SQLException {
        Calendar mockCalendar = mock(Calendar.class);
        fsResultSetUnderTest.getDate(columnLabel, mockCalendar);
        verify(mockResultSet).getDate(columnLabel, mockCalendar);
    }

    @Test
    public void shouldPassThroughGetTimeByIndexAndCalendar() throws SQLException {
        Calendar mockCalendar = mock(Calendar.class);
        fsResultSetUnderTest.getTime(columnIndex, mockCalendar);
        verify(mockResultSet).getTime(columnIndex, mockCalendar);
    }

    @Test
    public void shouldPassThroughGetTimeByColumnLabelAndCalendar() throws SQLException {
        Calendar mockCalendar = mock(Calendar.class);
        fsResultSetUnderTest.getTime(columnLabel, mockCalendar);
        verify(mockResultSet).getTime(columnLabel, mockCalendar);
    }

    @Test
    public void shouldPassThroughGetTimestampByColumnIndexAndCalendar() throws SQLException {
        Calendar mockCalendar = mock(Calendar.class);
        fsResultSetUnderTest.getTimestamp(columnIndex, mockCalendar);
        verify(mockResultSet).getTimestamp(columnIndex, mockCalendar);
    }

    @Test
    public void shouldPassThroughGetTimestampByColumnLabelAndCalendar() throws SQLException {
        Calendar mockCalendar = mock(Calendar.class);
        fsResultSetUnderTest.getTimestamp(columnLabel, mockCalendar);
        verify(mockResultSet).getTimestamp(columnLabel, mockCalendar);
    }

    @Test
    public void shouldPassThroughGetURLByIndex() throws SQLException {
        fsResultSetUnderTest.getURL(columnIndex);
        verify(mockResultSet).getURL(columnIndex);
    }

    @Test
    public void shouldPassThroughGetURLByColumnLabel() throws SQLException {
        fsResultSetUnderTest.getURL(columnLabel);
        verify(mockResultSet).getURL(columnLabel);
    }

    @Test
    public void shouldPassThroughUpdateRefByIndex() throws SQLException {
        Ref mockRef = mock(Ref.class);
        fsResultSetUnderTest.updateRef(columnIndex, mockRef);
        verify(mockResultSet).updateRef(columnIndex, mockRef);
    }

    @Test
    public void shouldPassThroughUpdateRefByColumnLabel() throws SQLException {
        Ref mockRef = mock(Ref.class);
        fsResultSetUnderTest.updateRef(columnLabel, mockRef);
        verify(mockResultSet).updateRef(columnLabel, mockRef);
    }

    @Test
    public void shouldPassThroughUpdateBlobByIndex() throws SQLException {
        Blob mockBlob = mock(Blob.class);
        fsResultSetUnderTest.updateBlob(columnIndex, mockBlob);
        verify(mockResultSet).updateBlob(columnIndex, mockBlob);
    }

    @Test
    public void shouldPassThroughUpdateBlobByColumnLabel() throws SQLException {
        Blob mockBlob = mock(Blob.class);
        fsResultSetUnderTest.updateBlob(columnLabel, mockBlob);
        verify(mockResultSet).updateBlob(columnLabel, mockBlob);
    }

    @Test
    public void shouldPassThroughUpdateClobByIndex() throws SQLException {
        Clob mockClob = mock(Clob.class);
        fsResultSetUnderTest.updateClob(columnIndex, mockClob);
        verify(mockResultSet).updateClob(columnIndex, mockClob);
    }

    @Test
    public void shouldPassThroughUpdateClobByColumnLabel() throws SQLException {
        Clob mockClob = mock(Clob.class);
        fsResultSetUnderTest.updateClob(columnLabel, mockClob);
        verify(mockResultSet).updateClob(columnLabel, mockClob);
    }

    @Test
    public void shouldPassThroughUpdateArrayByIndex() throws SQLException {
        Array mockArray = mock(Array.class);
        fsResultSetUnderTest.updateArray(columnIndex, mockArray);
        verify(mockResultSet).updateArray(columnIndex, mockArray);
    }

    @Test
    public void shouldPassThroughUpdateArrayByColumnLabel() throws SQLException {
        Array mockArray = mock(Array.class);
        fsResultSetUnderTest.updateArray(columnLabel, mockArray);
        verify(mockResultSet).updateArray(columnLabel, mockArray);
    }

    @Test
    public void shouldPassThroughGetRowIdByIndex() throws SQLException {
        fsResultSetUnderTest.getRowId(columnIndex);
        verify(mockResultSet).getRowId(columnIndex);
    }

    @Test
    public void shouldPassThroughGetRowIdByColumnLabel() throws SQLException {
        fsResultSetUnderTest.getRowId(columnLabel);
        verify(mockResultSet).getRowId(columnLabel);
    }

    @Test
    public void shouldPassThroughGetRowIdByIndexAndRowId() throws SQLException {
        RowId mockRowId = mock(RowId.class);
        fsResultSetUnderTest.updateRowId(columnIndex, mockRowId);
        verify(mockResultSet).updateRowId(columnIndex, mockRowId);
    }

    @Test
    public void shouldPassThroughGetRowIdByColumnLabelAndRowId() throws SQLException {
        RowId mockRowId = mock(RowId.class);
        fsResultSetUnderTest.updateRowId(columnLabel, mockRowId);
        verify(mockResultSet).updateRowId(columnLabel, mockRowId);
    }

    @Test
    public void shouldPassThroughGetHoldability() throws SQLException {
        fsResultSetUnderTest.getHoldability();
        verify(mockResultSet).getHoldability();
    }

    @Test
    public void shouldPassThroughIsClosed() throws SQLException {
        fsResultSetUnderTest.isClosed();
        verify(mockResultSet).isClosed();
    }

    @Test
    public void shouldPassThroughUpdateNStringByIndexAndString() throws SQLException {
        fsResultSetUnderTest.updateNString(columnIndex, "string_column");
        verify(mockResultSet).updateNString(columnIndex, "string_column");
    }

    @Test
    public void shouldPassThroughUpdateNStringByColumnLabelAndString() throws SQLException {
        fsResultSetUnderTest.updateNString(columnLabel, "string_column");
        verify(mockResultSet).updateNString(columnLabel, "string_column");
    }

    @Test
    public void shouldPassThroughUpdateNClobByIndexAndNClob() throws SQLException {
        NClob mockNClob = mock(NClob.class);
        fsResultSetUnderTest.updateNClob(columnIndex, mockNClob);
        verify(mockResultSet).updateNClob(columnIndex, mockNClob);
    }

    @Test
    public void shouldPassThroughUpdateNClobByColumnLabelAndNClob() throws SQLException {
        NClob mockNClob = mock(NClob.class);
        fsResultSetUnderTest.updateNClob(columnLabel, mockNClob);
        verify(mockResultSet).updateNClob(columnLabel, mockNClob);
    }

    @Test
    public void shouldPassThroughGetNClobByIndex() throws SQLException {
        fsResultSetUnderTest.getNClob(columnIndex);
        verify(mockResultSet).getNClob(columnIndex);
    }

    @Test
    public void shouldPassThroughGetNClobByColumnLabel() throws SQLException {
        fsResultSetUnderTest.getNClob(columnLabel);
        verify(mockResultSet).getNClob(columnLabel);
    }

    @Test
    public void shouldPassThroughGetSQLXMLByIndex() throws SQLException {
        fsResultSetUnderTest.getSQLXML(columnIndex);
        verify(mockResultSet).getSQLXML(columnIndex);
    }

    @Test
    public void shouldPassThroughGetSQLXMLByColumnLabel() throws SQLException {
        fsResultSetUnderTest.getSQLXML(columnLabel);
        verify(mockResultSet).getSQLXML(columnLabel);
    }

    @Test
    public void shouldPassThroughUpdateSQLXMLByIndexAndSQLXML() throws SQLException {
        SQLXML mockSQLXML = mock(SQLXML.class);
        fsResultSetUnderTest.updateSQLXML(columnIndex, mockSQLXML);
        verify(mockResultSet).updateSQLXML(columnIndex, mockSQLXML);
    }

    @Test
    public void shouldPassThroughUpdateSQLXMLByColumnLabelAndSQLXML() throws SQLException {
        SQLXML mockSQLXML = mock(SQLXML.class);
        fsResultSetUnderTest.updateSQLXML(columnLabel, mockSQLXML);
        verify(mockResultSet).updateSQLXML(columnLabel, mockSQLXML);
    }

    @Test
    public void shouldPassThroughGetNStringByIndex() throws SQLException {
        fsResultSetUnderTest.getNString(columnIndex);
        verify(mockResultSet).getNString(columnIndex);
    }

    @Test
    public void shouldPassThroughGetNStringByColumnLabel() throws SQLException {
        fsResultSetUnderTest.getNString(columnLabel);
        verify(mockResultSet).getNString(columnLabel);
    }

    @Test
    public void shouldPassThroughGetNCharacterStreamByIndex() throws SQLException {
        fsResultSetUnderTest.getNCharacterStream(columnIndex);
        verify(mockResultSet).getNCharacterStream(columnIndex);
    }

    @Test
    public void shouldPassThroughGetNCharacterStreamByColumnLabel() throws SQLException {
        fsResultSetUnderTest.getNCharacterStream(columnLabel);
        verify(mockResultSet).getNCharacterStream(columnLabel);
    }

    @Test
    public void shouldPassThroughUpdateNCharacterStreamByIndexAndReaderAndLength() throws SQLException {
        Reader mockReader = mock(Reader.class);
        fsResultSetUnderTest.updateNCharacterStream(columnIndex, mockReader, 3L);
        verify(mockResultSet).updateNCharacterStream(columnIndex, mockReader, 3L);
    }

    @Test
    public void shouldPassThroughUpdateNCharacterStreamByColumnLabelAndReaderAndLength() throws SQLException {
        Reader mockReader = mock(Reader.class);
        fsResultSetUnderTest.updateNCharacterStream(columnLabel, mockReader, 3L);
        verify(mockResultSet).updateNCharacterStream(columnLabel, mockReader, 3L);
    }

    @Test
    public void shouldPassThroughUpdateAsciiStreamByIndexAndStreamAndLength() throws SQLException {
        fsResultSetUnderTest.updateAsciiStream(columnIndex, byteArrayInputStream, (long)byteArray.length);
        verify(mockResultSet).updateAsciiStream(columnIndex, byteArrayInputStream, (long)byteArray.length);
    }

    @Test
    public void shouldPassThroughUpdateBinaryStreamByIndexAndStreamAndLength() throws SQLException {
        fsResultSetUnderTest.updateBinaryStream(columnIndex, byteArrayInputStream, (long)byteArray.length);
        verify(mockResultSet).updateBinaryStream(columnIndex, byteArrayInputStream, (long)byteArray.length);
    }

    @Test
    public void shouldPassThroughUpdateCharacterStreamByIndexAndReaderAndLongLength() throws SQLException {
        Reader mockReader = mock(Reader.class);
        fsResultSetUnderTest.updateCharacterStream(columnIndex, mockReader, 4L);
        verify(mockResultSet).updateCharacterStream(columnIndex, mockReader, 4L);
    }

    @Test
    public void shouldPassThroughUpdateAsciiStreamByColumnLabelAndStreamAndLongLength() throws SQLException {
        fsResultSetUnderTest.updateAsciiStream(columnLabel, byteArrayInputStream, (long)byteArray.length);
        verify(mockResultSet).updateAsciiStream(columnLabel, byteArrayInputStream, (long)byteArray.length);
    }

    @Test
    public void shouldPassThroughUpdateBinaryStreamByColumnLabelAndStreamAndLongLength() throws SQLException {
        fsResultSetUnderTest.updateBinaryStream(columnLabel, byteArrayInputStream, (long)byteArray.length);
        verify(mockResultSet).updateBinaryStream(columnLabel, byteArrayInputStream, (long)byteArray.length);
    }

    @Test
    public void shouldPassThroughUpdateCharacterStreamByColumnLabelAndReaderAndLongLength() throws SQLException {
        Reader mockReader = mock(Reader.class);
        fsResultSetUnderTest.updateCharacterStream(columnLabel, mockReader, 5L);
        verify(mockResultSet).updateCharacterStream(columnLabel, mockReader, 5L);
    }

    @Test
    public void shouldPassThroughUpdateBlobByIndexAndStramAndLongLength() throws SQLException {
        fsResultSetUnderTest.updateBlob(columnIndex, byteArrayInputStream, (long)byteArray.length);
        verify(mockResultSet).updateBlob(columnIndex, byteArrayInputStream, (long)byteArray.length);
    }

    @Test
    public void shouldPassThroughUpdateBlobByColumnLabelAndStramAndLongLength() throws SQLException {
        fsResultSetUnderTest.updateBlob(columnLabel, byteArrayInputStream, (long)byteArray.length);
        verify(mockResultSet).updateBlob(columnLabel, byteArrayInputStream, (long)byteArray.length);
    }

    @Test
    public void shouldPassThroughUpdateClobByIndexAndReaderAndLongLength() throws SQLException {
        Reader mockReader = mock(Reader.class);
        fsResultSetUnderTest.updateClob(columnIndex, mockReader, 6L);
        verify(mockResultSet).updateClob(columnIndex, mockReader, 6L);
    }

    @Test
    public void shouldPassThroughUpdateClobByColumnLabelAndReaderAndLongLength() throws SQLException {
        Reader mockReader = mock(Reader.class);
        fsResultSetUnderTest.updateClob(columnLabel, mockReader, 6L);
        verify(mockResultSet).updateClob(columnLabel, mockReader, 6L);
    }

    @Test
    public void shouldPassThroughUpdateNClobByIndexAndReaderAndLongLength() throws SQLException {
        Reader mockReader = mock(Reader.class);
        fsResultSetUnderTest.updateNClob(columnIndex, mockReader, 7L);
        verify(mockResultSet).updateNClob(columnIndex, mockReader, 7L);
    }

    @Test
    public void shouldPassThroughUpdateNClobByColumnLabelAndReaderAndLongLength() throws SQLException {
        Reader mockReader = mock(Reader.class);
        fsResultSetUnderTest.updateNClob(columnLabel, mockReader, 8L);
        verify(mockResultSet).updateNClob(columnLabel, mockReader, 8L);
    }

    @Test
    public void shouldPassThroughUpdateNCharacterStreamByIndexAndReader() throws SQLException {
        Reader mockReader = mock(Reader.class);
        fsResultSetUnderTest.updateNCharacterStream(columnIndex, mockReader);
        verify(mockResultSet).updateNCharacterStream(columnIndex, mockReader);
    }

    @Test
    public void shouldPassThroughUpdateNCharacterStreamByColumnLabelAndReader() throws SQLException {
        Reader mockReader = mock(Reader.class);
        fsResultSetUnderTest.updateNCharacterStream(columnLabel, mockReader);
        verify(mockResultSet).updateNCharacterStream(columnLabel, mockReader);
    }

    @Test
    public void shouldPassThroughUpdateAsciiStreamByIndexAndStream() throws SQLException {
        fsResultSetUnderTest.updateAsciiStream(columnIndex, byteArrayInputStream);
        verify(mockResultSet).updateAsciiStream(columnIndex, byteArrayInputStream);
    }

    @Test
    public void shouldPassThroughUpdateBinaryStreamByIndexAndStream() throws SQLException {
        fsResultSetUnderTest.updateBinaryStream(columnIndex, byteArrayInputStream);
        verify(mockResultSet).updateBinaryStream(columnIndex, byteArrayInputStream);
    }

    @Test
    public void shouldPassThroughUpdateCharacterStreamByIndexAndReader() throws SQLException {
        Reader mockReader = mock(Reader.class);
        fsResultSetUnderTest.updateCharacterStream(columnIndex, mockReader);
        verify(mockResultSet).updateCharacterStream(columnIndex, mockReader);
    }

    @Test
    public void shouldPassThroughUpdateAsciiStreamByColumnLabelAndStream() throws SQLException {
        fsResultSetUnderTest.updateAsciiStream(columnLabel, byteArrayInputStream);
        verify(mockResultSet).updateAsciiStream(columnLabel, byteArrayInputStream);
    }

    @Test
    public void shouldPassThroughUpdateBinaryStreamByColumnLabelAndStream() throws SQLException {
        fsResultSetUnderTest.updateAsciiStream(columnLabel, byteArrayInputStream);
        verify(mockResultSet).updateAsciiStream(columnLabel, byteArrayInputStream);
    }

    @Test
    public void shouldPassThroughUpdateCharacterStreamByColumnLabelAndReader() throws SQLException {
        Reader mockReader = mock(Reader.class);
        fsResultSetUnderTest.updateCharacterStream(columnLabel, mockReader);
        verify(mockResultSet).updateCharacterStream(columnLabel, mockReader);
    }

    @Test
    public void shouldPassThroughUpdateBlobByIndexAndStream() throws SQLException {
        fsResultSetUnderTest.updateBlob(columnIndex, byteArrayInputStream);
        verify(mockResultSet).updateBlob(columnIndex, byteArrayInputStream);
    }

    @Test
    public void shouldPassThroughUpdateBlobByColumnLabelAndStream() throws SQLException {
        fsResultSetUnderTest.updateBlob(columnLabel, byteArrayInputStream);
        verify(mockResultSet).updateBlob(columnLabel, byteArrayInputStream);
    }

    @Test
    public void shouldPassThroughUpdateClobByIndexAndReader() throws SQLException {
        Reader mockReader = mock(Reader.class);
        fsResultSetUnderTest.updateClob(columnIndex, mockReader);
        verify(mockResultSet).updateClob(columnIndex, mockReader);
    }

    @Test
    public void shouldPassThroughUpdateClobByColumnLabelAndReader() throws SQLException {
        Reader mockReader = mock(Reader.class);
        fsResultSetUnderTest.updateClob(columnLabel, mockReader);
        verify(mockResultSet).updateClob(columnLabel, mockReader);
    }

    @Test
    public void shouldPassThroughUpdateNClobByIndexAndReader() throws SQLException {
        Reader mockReader = mock(Reader.class);
        fsResultSetUnderTest.updateNClob(columnIndex, mockReader);
        verify(mockResultSet).updateNClob(columnIndex, mockReader);
    }

    @Test
    public void shouldPassThroughUpdateNClobByColumnLabelAndReader() throws SQLException {
        Reader mockReader = mock(Reader.class);
        fsResultSetUnderTest.updateNClob(columnLabel, mockReader);
        verify(mockResultSet).updateNClob(columnLabel, mockReader);
    }

    @Test
    public void shouldPassThroughGetObjectByIndexAndType() throws SQLException {
        fsResultSetUnderTest.getObject(columnIndex, Object.class);
        verify(mockResultSet).getObject(columnIndex, Object.class);
    }

    @Test
    public void shouldPassThroughGetObjectByColumnLabelAndType() throws SQLException {
        fsResultSetUnderTest.getObject(columnLabel, Object.class);
        verify(mockResultSet).getObject(columnLabel, Object.class);
    }

    @Test
    public void shouldPassThroughUnwrap() throws SQLException {
        fsResultSetUnderTest.unwrap(Object.class);
        verify(mockResultSet).unwrap(Object.class);
    }

    @Test
    public void shouldPassThroughIsWrapperFor() throws SQLException {
        fsResultSetUnderTest.isWrapperFor(Object.class);
        verify(mockResultSet).isWrapperFor(Object.class);
    }

    @Test
    public void shouldPassThroughUpdateObjectByIndexAndValueAndSQLTypeAndScaleOrLength() throws SQLException {
        SQLType mockSQLType = mock(SQLType.class);
        Object o = new Object();
        fsResultSetUnderTest.updateObject(columnIndex, o, mockSQLType, scale);
        verify(mockResultSet).updateObject(columnIndex, o, mockSQLType, scale);
    }

    @Test
    public void shouldPassThroughUpdateObjectByColumnLabelAndValueAndSQLTypeAndScaleOrLength() throws SQLException {
        SQLType mockSQLType = mock(SQLType.class);
        Object o = new Object();
        fsResultSetUnderTest.updateObject(columnLabel, o, mockSQLType, scale);
        verify(mockResultSet).updateObject(columnLabel, o, mockSQLType, scale);
    }

    @Test
    public void shouldPassThroughUpdateObjectByIndexAndValueAndSQLType() throws SQLException {
        SQLType mockSQLType = mock(SQLType.class);
        Object o = new Object();
        fsResultSetUnderTest.updateObject(columnIndex, o, mockSQLType);
        verify(mockResultSet).updateObject(columnIndex, o, mockSQLType);
    }

    @Test
    public void shouldPassThroughUpdateObjectByColumnLabelAndValueAndSQLType() throws SQLException {
        SQLType mockSQLType = mock(SQLType.class);
        Object o = new Object();
        fsResultSetUnderTest.updateObject(columnLabel, o, mockSQLType);
        verify(mockResultSet).updateObject(columnLabel, o, mockSQLType);
    }

//    @Test
//    public int getCount() {
//        // TODO: fill in the various tests for this
//    }

    @Test
    public void shouldPassThroughMoveToPreviousToPrevious() throws SQLException {
        fsResultSetUnderTest.moveToPrevious();
        verify(mockResultSet).previous();
    }

    @Test
    public void shouldPassThroughMoveToFirstToNextWhenBeforeFirst() throws SQLException {
        when(mockResultSet.isBeforeFirst()).thenReturn(true);
        fsResultSetUnderTest.moveToFirst();

        InOrder inOrder = inOrder(mockResultSet);
        inOrder.verify(mockResultSet).isBeforeFirst();
        inOrder.verify(mockResultSet).next();
        verify(mockResultSet, times(0)).first();
    }

    @Test
    public void shouldPassThroughMoveToFirstToFirstWhenNotBeforeFirst() throws SQLException {
        when(mockResultSet.isBeforeFirst()).thenReturn(false);
        fsResultSetUnderTest.moveToFirst();

        InOrder inOrder = inOrder(mockResultSet);
        inOrder.verify(mockResultSet).isBeforeFirst();
        inOrder.verify(mockResultSet).first();
        verify(mockResultSet, times(0)).next();
    }

    @Test
    public void shouldPassThroughMoveToNextToNext() throws SQLException {
        fsResultSetUnderTest.moveToNext();
        verify(mockResultSet).next();
    }

    @Test
    public void shouldPassThroughMoveToPositionToAbsolute() throws SQLException {
        fsResultSetUnderTest.moveToPosition(row);
        verify(mockResultSet).absolute(row);
    }

    @Test
    public void shouldPassThroughMoveToToRelative() throws SQLException {
        fsResultSetUnderTest.move(row);
        verify(mockResultSet).relative(row);
    }

    @Test
    public void shouldPassThroughMoveToLastToLast() throws SQLException {
        fsResultSetUnderTest.moveToLast();
        verify(mockResultSet).last();
    }

    @Test
    public void shouldPassThroughGetPositionToGetRow() throws SQLException {
        fsResultSetUnderTest.getPosition();
        verify(mockResultSet).getRow();
    }
}
