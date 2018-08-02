package com.fsryan.forsuredb.api.staticdata;

import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.migration.Migration;
import com.fsryan.forsuredb.migration.MigrationSet;
import org.junit.After;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
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
                    .columnMap(mapOf("column", ColumnInfo.builder().columnName("column").methodName("column").build()))
                    .build()
    );
    private static final List<MigrationSet> errorMigrationSet = Arrays.asList(MigrationSet.builder()
            .dbVersion(1)
            .orderedMigrations(dummyMigrations)
            .targetSchema(errorSchema)
            .build());

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

        final String xml =
                "<static_data>\n" +
                "  <records db_version=\"1\">\n" +
                "    <record non_existent_column=\"non-existant\" />\n" +
                "  </records>\n" +
                "</static_data>";
        xmlStream = new ByteArrayInputStream(xml.getBytes());
        try {
            StaticDataRetrieverFactory.createFor("table", errorMigrationSet, xmlStream)
                    .retrieve(mock(OnRecordRetrievedListener.class));
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("java.lang.IllegalStateException: Table 'table' does not have column 'non_existent_column'; columns: [column, modified, deleted, _id, created]; db_version: 1", e.getMessage());
            return;
        }
    }

    @Test
    public void shouldThrowIllegalStateExceptionWithUsefulMessageWhenColumnDoesNotHaveQualifiedType() {

        final String xml =
                "<static_data>\n" +
                "  <records db_version=\"1\">\n" +
                "    <record column=\"non-existant\" />\n" +
                "  </records>\n" +
                "</static_data>";
        xmlStream = new ByteArrayInputStream(xml.getBytes());
        try {
            StaticDataRetrieverFactory.createFor("table", errorMigrationSet, xmlStream)
                    .retrieve(mock(OnRecordRetrievedListener.class));
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("java.lang.IllegalStateException: Column 'column' exists without a qualified type; db_version: 1", e.getMessage());
            return;
        }
    }
}
