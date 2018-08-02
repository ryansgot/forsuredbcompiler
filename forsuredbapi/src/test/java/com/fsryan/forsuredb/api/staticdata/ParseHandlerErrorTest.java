package com.fsryan.forsuredb.api.staticdata;

import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.migration.Migration;
import com.fsryan.forsuredb.migration.MigrationSet;
import org.junit.After;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.fsryan.forsuredb.api.TestData.mapOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class ParseHandlerErrorTest {

    private static final List<Migration> dummyMigrations = Arrays.asList(
            Migration.builder().tableName("table").type(Migration.Type.CREATE_TABLE).build()
    );
    private static final Map<String, TableInfo> errorSchema = mapOf(
            "table",
            TableInfo.builder()
                    .tableName("table")
                    .qualifiedClassName(ParseHandlerErrorTest.class.getName())
                    .columnMap(mapOf("column", ColumnInfo.builder()
                            .columnName("column")
                            .methodName("column")
                            .build(),
                            "column2",
                            ColumnInfo.builder()
                            .columnName("column2")
                            .methodName("column2")
                            .qualifiedType("unsupported")
                            .build(),
                            "date_column",
                            ColumnInfo.builder()
                            .columnName("date_column")
                            .methodName("date_column")
                            .qualifiedType(Date.class.getName())
                            .build())
                    ).build()
    );
    private static final List<MigrationSet> errorMigrationSet = Arrays.asList(MigrationSet.builder()
            .dbVersion(1)
            .orderedMigrations(dummyMigrations)
            .targetSchema(errorSchema)
            .build());
    private static final String xmlFormat =
            "<static_data>\n" +
            "  <records db_version=\"1\">\n" +
            "    <record %s=\"%s\" />\n" +
            "  </records>\n" +
            "</static_data>";

    private ByteArrayInputStream xmlStream;

    @After
    public void closeXmlStream() {
        try {
            xmlStream.close();
        } catch (Exception e) {
            // do nothing
        }
    }

    @Test
    public void shouldThrowIllegalStateExceptionWithUsefulMessageWhenXmlColumnDoesNotExist() {
        final String xml = String.format(xmlFormat, "non_existent_column", "non-existant");
        xmlStream = new ByteArrayInputStream(xml.getBytes());
        try {
            StaticDataRetrieverFactory.createFor("table", errorMigrationSet, xmlStream)
                    .retrieve(mock(OnRecordRetrievedListener.class));
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("java.lang.IllegalStateException: Table 'table' does not have column 'non_existent_column'; db_version: 1; columns: [_id, column, column2, created, date_column, deleted, modified]", e.getMessage());
        }
    }

    @Test
    public void shouldThrowIllegalStateExceptionWithUsefulMessageWhenColumnDoesNotHaveQualifiedType() {
        final String xml = String.format(xmlFormat, "column", "somevalue");
        xmlStream = new ByteArrayInputStream(xml.getBytes());
        try {
            StaticDataRetrieverFactory.createFor("table", errorMigrationSet, xmlStream)
                    .retrieve(mock(OnRecordRetrievedListener.class));
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("java.lang.IllegalStateException: Column 'column' exists without a qualified type; db_version: 1; table: table", e.getMessage());
        }
    }

    @Test
    public void shouldThrowIllegalStateExceptionWhenColumnHasUnsupportedType() {
        final String xml = String.format(xmlFormat, "column2", "unsupported type");
        xmlStream = new ByteArrayInputStream(xml.getBytes());
        try {
            StaticDataRetrieverFactory.createFor("table", errorMigrationSet, xmlStream)
                    .retrieve(mock(OnRecordRetrievedListener.class));
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("java.lang.IllegalStateException: Unsupported type: 'unsupported; table: table; column 'column2'", e.getMessage());
        }
    }

    @Test
    public void shouldThrowIllegalStateExceptionWhenCannotParseDate() {
        final String xml = String.format(xmlFormat, "date_column", "Not a Date");
        xmlStream = new ByteArrayInputStream(xml.getBytes());
        try {
            StaticDataRetrieverFactory.createFor("table", errorMigrationSet, xmlStream)
                    .retrieve(mock(OnRecordRetrievedListener.class));
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("java.lang.IllegalStateException: could not parse date 'Not a Date'; db_version: 1; table: table; column: date_column", e.getMessage());
        }
    }
}
