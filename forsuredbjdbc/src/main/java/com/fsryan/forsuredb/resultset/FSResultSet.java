package com.fsryan.forsuredb.resultset;

import com.fsryan.forsuredb.api.Retriever;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

public class FSResultSet implements ResultSet, Retriever {

    private ResultSet resultSet;

    public FSResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    @Override
    public boolean next() throws SQLException {
        return resultSet.next();
    }

    /**
     * @throws RuntimeException when closing the result set throws an {@link SQLException}
     */
    @Override
    public void close() {
        try {
            resultSet.close();
        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        }
    }

    @Override
    public boolean wasNull() throws SQLException {
        return resultSet.wasNull();
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        return resultSet.getString(columnIndex);
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        return resultSet.getBoolean(columnIndex);
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException {
        return resultSet.getByte(columnIndex);
    }

    @Override
    public short getShort(int columnIndex) throws SQLException {
        return resultSet.getShort(columnIndex);
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        return resultSet.getInt(columnIndex);
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        return resultSet.getLong(columnIndex);
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        return resultSet.getFloat(columnIndex);
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        return resultSet.getDouble(columnIndex);
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        return resultSet.getBigDecimal(columnIndex, scale);
    }

    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        return resultSet.getBytes(columnIndex);
    }

    @Override
    public Date getDate(int columnIndex) throws SQLException {
        return resultSet.getDate(columnIndex);
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException {
        return resultSet.getTime(columnIndex);
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return resultSet.getTimestamp(columnIndex);
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        return resultSet.getAsciiStream(columnIndex);
    }

    @Override
    @Deprecated
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        return resultSet.getUnicodeStream(columnIndex);
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        return resultSet.getBinaryStream(columnIndex);
    }

    /**
     * @param column the name (label) of the column
     * @return the integer value of the column with the label passed in
     * @throws RuntimeException when the wrapped {@link ResultSet} throws an {@link SQLException}
     */
    @Override
    public String getString(String column) {
        try {
            return resultSet.getString(column);
        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        }
    }

    @Override
    public boolean getBoolean(String column) throws SQLException {
        return resultSet.getBoolean(column);
    }

    @Override
    public byte getByte(String column) throws SQLException {
        return resultSet.getByte(column);
    }

    @Override
    public short getShort(String column) throws SQLException {
        return resultSet.getShort(column);
    }

    /**
     * @param column the name (label) of the column
     * @return the integer value of the column with the label passed in
     * @throws RuntimeException when the wrapped {@link ResultSet} throws an {@link SQLException}
     */
    @Override
    public int getInt(String column) {
        try {
            return resultSet.getInt(column);
        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        }
    }
    
    /**
     * @param column the name (label) of the column
     * @return the long value of the column with the label passed in
     * @throws RuntimeException when the wrapped {@link ResultSet} throws an {@link SQLException}
     */
    @Override
    public long getLong(String column) {
        try {
            return resultSet.getLong(column);
        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        }
    }

    /**
     * @param column the name (label) of the column
     * @return the double value of the column with the label passed in
     * @throws RuntimeException when the wrapped {@link ResultSet} throws an {@link SQLException}
     */
    @Override
    public double getDouble(String column) {
        try {
            return resultSet.getDouble(column);
        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        }
    }

    @Override
    public BigDecimal getBigDecimal(String column, int scale) throws SQLException {
        return resultSet.getBigDecimal(column, scale);
    }
    
    /**
     * @param column the name (label) of the column
     * @return the blob value of the column with the label passed in
     * @throws RuntimeException when the wrapped {@link ResultSet} throws an {@link SQLException}
     */
    @Override
    public byte[] getBytes(String column) {
        try {
            return resultSet.getBytes(column);
        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        }
    }
    
    @Override
    public Date getDate(String column) throws SQLException {
        return resultSet.getDate(column);
    }
    
    @Override
    public Time getTime(String column) throws SQLException {
        return resultSet.getTime(column);
    }
    
    @Override
    public Timestamp getTimestamp(String column) throws SQLException {
        return resultSet.getTimestamp(column);
    }
    
    @Override
    public InputStream getAsciiStream(String column) throws SQLException {
        return resultSet.getAsciiStream(column);
    }
    
    @Override
    @Deprecated
    public InputStream getUnicodeStream(String column) throws SQLException {
        return resultSet.getUnicodeStream(column);
    }
    
    @Override
    public InputStream getBinaryStream(String column) throws SQLException {
        return resultSet.getBinaryStream(column);
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return resultSet.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        resultSet.clearWarnings();
    }

    @Override
    public String getCursorName() throws SQLException {
        return resultSet.getCursorName();
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return resultSet.getMetaData();
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        return resultSet.getObject(columnIndex);
    }

    @Override
    public Object getObject(String column) throws SQLException {
        return resultSet.getObject(column);
    }

    @Override
    public int findColumn(String column) throws SQLException {
        return resultSet.findColumn(column);
    }

    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {
        return resultSet.getCharacterStream(columnIndex);
    }

    @Override
    public Reader getCharacterStream(String column) throws SQLException {
        return resultSet.getCharacterStream(column);
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return resultSet.getBigDecimal(columnIndex);
    }

    @Override
    public BigDecimal getBigDecimal(String column) throws SQLException {
        return resultSet.getBigDecimal(column);
    }

    /**
     * @return whether the current cursor is before the first record
     * @throws RuntimeException when the wrapped {@link ResultSet} throws an {@link SQLException}
     */
    @Override
    public boolean isBeforeFirst() {
        try {
            return resultSet.isBeforeFirst();
        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        }
    }

    /**
     * @return whether the current cursor is after the last record
     * @throws RuntimeException when the wrapped {@link ResultSet} throws an {@link SQLException}
     */
    @Override
    public boolean isAfterLast() {
        try {
            return resultSet.isAfterLast();
        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        }
    }

    /**
     * @return whether the current cursor is at the first record
     * @throws RuntimeException when the wrapped {@link ResultSet} throws an {@link SQLException}
     */
    @Override
    public boolean isFirst() {
        try {
            return resultSet.isFirst();
        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        }
    }

    /**
     * @return whether the current cursor is before the first record
     * @throws RuntimeException when the wrapped {@link ResultSet} throws an {@link SQLException}
     */
    @Override
    public boolean isLast() {
        try {
            return resultSet.isLast();
        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        }
    }

    @Override
    public void beforeFirst() throws SQLException {
        resultSet.beforeFirst();
    }

    @Override
    public void afterLast() throws SQLException {
        resultSet.afterLast();
    }

    @Override
    public boolean first() throws SQLException {
        return resultSet.first();
    }

    @Override
    public boolean last() throws SQLException {
        return resultSet.last();
    }

    @Override
    public int getRow() throws SQLException {
        return resultSet.getRow();
    }

    @Override
    public boolean absolute(int row) throws SQLException {
        return resultSet.absolute(row);
    }

    @Override
    public boolean relative(int rows) throws SQLException {
        return resultSet.relative(rows);
    }

    @Override
    public boolean previous() throws SQLException {
        return resultSet.previous();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        resultSet.setFetchDirection(direction);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return resultSet.getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        resultSet.setFetchSize(rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return resultSet.getFetchSize();
    }

    @Override
    public int getType() throws SQLException {
        return resultSet.getType();
    }

    @Override
    public int getConcurrency() throws SQLException {
        return resultSet.getConcurrency();
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        return resultSet.rowUpdated();
    }

    @Override
    public boolean rowInserted() throws SQLException {
        return resultSet.rowInserted();
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        return resultSet.rowDeleted();
    }

    @Override
    public void updateNull(int columnIndex) throws SQLException {
        resultSet.updateNull(columnIndex);
    }

    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        resultSet.updateBoolean(columnIndex, x);
    }

    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException {
        resultSet.updateByte(columnIndex, x);
    }

    @Override
    public void updateShort(int columnIndex, short x) throws SQLException {
        resultSet.updateShort(columnIndex, x);
    }

    @Override
    public void updateInt(int columnIndex, int x) throws SQLException {
        resultSet.updateInt(columnIndex, x);
    }

    @Override
    public void updateLong(int columnIndex, long x) throws SQLException {
        resultSet.updateLong(columnIndex, x);
    }

    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException {
        resultSet.updateFloat(columnIndex, x);
    }

    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException {
        resultSet.updateDouble(columnIndex, x);
    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        resultSet.updateBigDecimal(columnIndex, x);
    }

    @Override
    public void updateString(int columnIndex, String x) throws SQLException {
        resultSet.updateString(columnIndex, x);
    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        resultSet.updateBytes(columnIndex, x);
    }

    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException {
        resultSet.updateDate(columnIndex, x);
    }

    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException {
        resultSet.updateTime(columnIndex, x);
    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        resultSet.updateTimestamp(columnIndex, x);
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        resultSet.updateAsciiStream(columnIndex, x, length);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        resultSet.updateBinaryStream(columnIndex, x, length);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        resultSet.updateCharacterStream(columnIndex, x, length);
    }

    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
        resultSet.updateObject(columnIndex, x, scaleOrLength);
    }

    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {
        resultSet.updateObject(columnIndex, x);
    }

    @Override
    public void updateNull(String column) throws SQLException {
        resultSet.updateNull(column);
    }

    @Override
    public void updateBoolean(String column, boolean x) throws SQLException {
        resultSet.updateBoolean(column, x);
    }

    @Override
    public void updateByte(String column, byte x) throws SQLException {
        resultSet.updateByte(column, x);
    }

    @Override
    public void updateShort(String column, short x) throws SQLException {
        resultSet.updateShort(column, x);
    }

    @Override
    public void updateInt(String column, int x) throws SQLException {
        resultSet.updateInt(column, x);
    }

    @Override
    public void updateLong(String column, long x) throws SQLException {
        resultSet.updateLong(column, x);
    }

    @Override
    public void updateFloat(String column, float x) throws SQLException {
        resultSet.updateFloat(column, x);
    }

    @Override
    public void updateDouble(String column, double x) throws SQLException {
        resultSet.updateDouble(column, x);
    }

    @Override
    public void updateBigDecimal(String column, BigDecimal x) throws SQLException {
        resultSet.updateBigDecimal(column, x);
    }

    @Override
    public void updateString(String column, String x) throws SQLException {
        resultSet.updateString(column, x);
    }

    @Override
    public void updateBytes(String column, byte[] x) throws SQLException {
        resultSet.updateBytes(column, x);
    }

    @Override
    public void updateDate(String column, Date x) throws SQLException {
        resultSet.updateDate(column, x);
    }

    @Override
    public void updateTime(String column, Time x) throws SQLException {
        resultSet.updateTime(column, x);
    }

    @Override
    public void updateTimestamp(String column, Timestamp x) throws SQLException {
        resultSet.updateTimestamp(column, x);
    }

    @Override
    public void updateAsciiStream(String column, InputStream x, int length) throws SQLException {
        resultSet.updateAsciiStream(column, x, length);
    }

    @Override
    public void updateBinaryStream(String column, InputStream x, int length) throws SQLException {
        resultSet.updateBinaryStream(column, x, length);
    }

    @Override
    public void updateCharacterStream(String column, Reader reader, int length) throws SQLException {
        resultSet.updateCharacterStream(column, reader, length);
    }

    @Override
    public void updateObject(String column, Object x, int scaleOrLength) throws SQLException {
        resultSet.updateObject(column, x, scaleOrLength);
    }

    @Override
    public void updateObject(String column, Object x) throws SQLException {
        resultSet.updateObject(column, x);
    }

    @Override
    public void insertRow() throws SQLException {
        resultSet.insertRow();
    }

    @Override
    public void updateRow() throws SQLException {
        resultSet.updateRow();
    }

    @Override
    public void deleteRow() throws SQLException {
        resultSet.deleteRow();
    }

    @Override
    public void refreshRow() throws SQLException {
        resultSet.refreshRow();
    }

    @Override
    public void cancelRowUpdates() throws SQLException {
        resultSet.cancelRowUpdates();
    }

    @Override
    public void moveToInsertRow() throws SQLException {
        resultSet.moveToInsertRow();
    }

    @Override
    public void moveToCurrentRow() throws SQLException {
        resultSet.moveToCurrentRow();
    }

    @Override
    public Statement getStatement() throws SQLException {
        return resultSet.getStatement();
    }

    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        return resultSet.getObject(columnIndex, map);
    }

    @Override
    public Ref getRef(int columnIndex) throws SQLException {
        return resultSet.getRef(columnIndex);
    }

    @Override
    public Blob getBlob(int columnIndex) throws SQLException {
        return resultSet.getBlob(columnIndex);
    }

    @Override
    public Clob getClob(int columnIndex) throws SQLException {
        return resultSet.getClob(columnIndex);
    }

    @Override
    public Array getArray(int columnIndex) throws SQLException {
        return resultSet.getArray(columnIndex);
    }

    @Override
    public Object getObject(String column, Map<String, Class<?>> map) throws SQLException {
        return resultSet.getObject(column, map);
    }

    @Override
    public Ref getRef(String column) throws SQLException {
        return resultSet.getRef(column);
    }

    @Override
    public Blob getBlob(String column) throws SQLException {
        return resultSet.getBlob(column);
    }

    /**
     * @param column the name (label) of the column
     * @return the float value of the column with the label passed in
     * @throws RuntimeException when the wrapped {@link ResultSet} throws an {@link SQLException}
     */
    @Override
    public float getFloat(String column) {
        try {
            return resultSet.getFloat(column);
        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        }
    }

    @Override
    public Clob getClob(String column) throws SQLException {
        return resultSet.getClob(column);
    }

    @Override
    public Array getArray(String column) throws SQLException {
        return resultSet.getArray(column);
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        return resultSet.getDate(columnIndex, cal);
    }

    @Override
    public Date getDate(String column, Calendar cal) throws SQLException {
        return resultSet.getDate(column, cal);
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        return resultSet.getTime(columnIndex, cal);
    }

    @Override
    public Time getTime(String column, Calendar cal) throws SQLException {
        return resultSet.getTime(column, cal);
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        return resultSet.getTimestamp(columnIndex, cal);
    }

    @Override
    public Timestamp getTimestamp(String column, Calendar cal) throws SQLException {
        return resultSet.getTimestamp(column, cal);
    }

    @Override
    public URL getURL(int columnIndex) throws SQLException {
        return resultSet.getURL(columnIndex);
    }

    @Override
    public URL getURL(String column) throws SQLException {
        return resultSet.getURL(column);
    }

    @Override
    public void updateRef(int columnIndex, Ref x) throws SQLException {
        resultSet.updateRef(columnIndex, x);
    }

    @Override
    public void updateRef(String column, Ref x) throws SQLException {
        resultSet.updateRef(column, x);
    }

    @Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        resultSet.updateBlob(columnIndex, x);
    }

    @Override
    public void updateBlob(String column, Blob x) throws SQLException {
        resultSet.updateBlob(column, x);
    }

    @Override
    public void updateClob(int columnIndex, Clob x) throws SQLException {
        resultSet.updateClob(columnIndex, x);
    }

    @Override
    public void updateClob(String column, Clob x) throws SQLException {
        resultSet.updateClob(column, x);
    }

    @Override
    public void updateArray(int columnIndex, Array x) throws SQLException {
        resultSet.updateArray(columnIndex, x);
    }

    @Override
    public void updateArray(String column, Array x) throws SQLException {
        resultSet.updateArray(column, x);
    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
        return resultSet.getRowId(columnIndex);
    }

    @Override
    public RowId getRowId(String column) throws SQLException {
        return resultSet.getRowId(column);
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        resultSet.updateRowId(columnIndex, x);
    }

    @Override
    public void updateRowId(String column, RowId x) throws SQLException {
        resultSet.updateRowId(column, x);
    }

    @Override
    public int getHoldability() throws SQLException {
        return resultSet.getHoldability();
    }
    
    /**
     * @return whether the underlying {@link ResultSet} is closed
     * @throws RuntimeException when the wrapped {@link ResultSet} throws an {@link SQLException}
     */
    @Override
    public boolean isClosed() {
        try {
            return resultSet.isClosed();
        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        }
    }

    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException {
        resultSet.updateNString(columnIndex, nString);
    }

    @Override
    public void updateNString(String column, String nString) throws SQLException {
        resultSet.updateNString(column, nString);
    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        resultSet.updateNClob(columnIndex, nClob);
    }

    @Override
    public void updateNClob(String column, NClob nClob) throws SQLException {
        resultSet.updateNClob(column, nClob);
    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        return resultSet.getNClob(columnIndex);
    }

    @Override
    public NClob getNClob(String column) throws SQLException {
        return resultSet.getNClob(column);
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        return resultSet.getSQLXML(columnIndex);
    }

    @Override
    public SQLXML getSQLXML(String column) throws SQLException {
        return resultSet.getSQLXML(column);
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        resultSet.updateSQLXML(columnIndex, xmlObject);
    }

    @Override
    public void updateSQLXML(String column, SQLXML xmlObject) throws SQLException {
        resultSet.updateSQLXML(column, xmlObject);
    }

    @Override
    public String getNString(int columnIndex) throws SQLException {
        return resultSet.getNString(columnIndex);
    }

    @Override
    public String getNString(String column) throws SQLException {
        return resultSet.getNString(column);
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        return resultSet.getNCharacterStream(columnIndex);
    }

    @Override
    public Reader getNCharacterStream(String column) throws SQLException {
        return resultSet.getNCharacterStream(column);
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        resultSet.updateNCharacterStream(columnIndex, x, length);
    }

    @Override
    public void updateNCharacterStream(String column, Reader reader, long length) throws SQLException {
        resultSet.updateNCharacterStream(column, reader, length);
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
        resultSet.updateAsciiStream(columnIndex, x, length);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
        resultSet.updateBinaryStream(columnIndex, x, length);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        resultSet.updateCharacterStream(columnIndex, x, length);
    }

    @Override
    public void updateAsciiStream(String column, InputStream x, long length) throws SQLException {
        resultSet.updateAsciiStream(column, x, length);
    }

    @Override
    public void updateBinaryStream(String column, InputStream x, long length) throws SQLException {
        resultSet.updateBinaryStream(column, x, length);
    }

    @Override
    public void updateCharacterStream(String column, Reader reader, long length) throws SQLException {
        resultSet.updateCharacterStream(column, reader, length);
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
        resultSet.updateBlob(columnIndex, inputStream, length);
    }

    @Override
    public void updateBlob(String column, InputStream inputStream, long length) throws SQLException {
        resultSet.updateBlob(column, inputStream, length);
    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
        resultSet.updateClob(columnIndex, reader, length);
    }

    @Override
    public void updateClob(String column, Reader reader, long length) throws SQLException {
        resultSet.updateClob(column, reader, length);
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
        resultSet.updateNClob(columnIndex, reader, length);
    }

    @Override
    public void updateNClob(String column, Reader reader, long length) throws SQLException {
        resultSet.updateNClob(column, reader, length);
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
        resultSet.updateNCharacterStream(columnIndex, x);
    }

    @Override
    public void updateNCharacterStream(String column, Reader reader) throws SQLException {
        resultSet.updateNCharacterStream(column, reader);
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
        resultSet.updateAsciiStream(columnIndex, x);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
        resultSet.updateBinaryStream(columnIndex, x);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
        resultSet.updateCharacterStream(columnIndex, x);
    }

    @Override
    public void updateAsciiStream(String column, InputStream x) throws SQLException {
        resultSet.updateAsciiStream(column, x);
    }

    @Override
    public void updateBinaryStream(String column, InputStream x) throws SQLException {
        resultSet.updateBinaryStream(column, x);
    }

    @Override
    public void updateCharacterStream(String column, Reader reader) throws SQLException {
        resultSet.updateCharacterStream(column, reader);
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        resultSet.updateBlob(columnIndex, inputStream);
    }

    @Override
    public void updateBlob(String column, InputStream inputStream) throws SQLException {
        resultSet.updateBlob(column, inputStream);
    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        resultSet.updateClob(columnIndex, reader);
    }

    @Override
    public void updateClob(String column, Reader reader) throws SQLException {
        resultSet.updateClob(column, reader);
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        resultSet.updateNClob(columnIndex, reader);
    }

    @Override
    public void updateNClob(String column, Reader reader) throws SQLException {
        resultSet.updateNClob(column, reader);
    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        return resultSet.getObject(columnIndex, type);
    }

    @Override
    public <T> T getObject(String column, Class<T> type) throws SQLException {
        return resultSet.getObject(column, type);
    }

    @Override
    public void updateObject(int columnIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        resultSet.updateObject(columnIndex, x, targetSqlType, scaleOrLength);
    }

    @Override
    public void updateObject(String column, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        resultSet.updateObject(column, x, targetSqlType, scaleOrLength);
    }

    @Override
    public void updateObject(int columnIndex, Object x, SQLType targetSqlType) throws SQLException {
        resultSet.updateObject(columnIndex, x, targetSqlType);
    }

    @Override
    public void updateObject(String column, Object x, SQLType targetSqlType) throws SQLException {
        resultSet.updateObject(column, x, targetSqlType);
    }

    /**
     * @return the count of the rows if it can be known without winding the cursor past the point
     * wherein information can be returned. This has the potential to be <i>VERY</i> inefficient,
     * as, in the worst case, all records must be iterated to find out the count of records.
     * @throws RuntimeException when an {@link SQLException} is thrown while getting the type
     * @throws IllegalStateException when the scrolling type is {@link #TYPE_FORWARD_ONLY}
     * of {@link ResultSet}
     */
    @Override
    public int getCount() {
        try {
            if (getType() == TYPE_FORWARD_ONLY) {
                if (isLast()) {
                    return getPosition() + 1;
                }
                if (isAfterLast()) {
                    return getPosition();
                }
                throw new IllegalStateException("Can only iterate forward and not in last or after last position--so don't know count");
            }
        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        }

        try {
            int currentPosition = getPosition();
            moveToLast();
            int count = getPosition();
            absolute(currentPosition);
            return count;
        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        }
    }

    /**
     * @return true if the operation succeeded--false otherwise
     */
    @Override
    public boolean moveToPrevious() {
        try {
            return previous();
        } catch (SQLException sqle) {
            return false;
        }
    }

    /**
     * @return true if the operation succeeded--false otherwise
     */
    @Override
    public boolean moveToFirst() {
        try {
            return isBeforeFirst() ? next() : first();
        } catch (SQLException sqle) {
            return false;
        }
    }

    /**
     * @return true if the operation succeeded--false otherwise
     */
    @Override
    public boolean moveToNext() {
        try {
            return next();
        } catch (SQLException sqle) {
            return false;
        }
    }

    /**
     * @return true if the operation succeeded--false otherwise
     */
    @Override
    public boolean moveToPosition(int position) {
        try {
            return absolute(position);
        } catch (SQLException sqle) {
            return false;
        }
    }

    /**
     * @return true if the operation succeeded--false otherwise
     */
    @Override
    public boolean move(int offset) {
        try {
            return relative(offset);
        } catch (SQLException sqle) {
            return false;
        }
    }

    /**
     * @return true if the operation succeeded--false otherwise
     */
    @Override
    public boolean moveToLast() {
        try {
            return last();
        } catch (SQLException sqle) {
            return false;
        }
    }

    /**
     * @return the current position of the result set cursor
     * @throws RuntimeException when the wrapped {@link ResultSet} throws an {@link SQLException}
     */
    @Override
    public int getPosition() {
        try {
            return resultSet.getRow();
        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        }
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return resultSet.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return resultSet.isWrapperFor(iface);
    }
}
