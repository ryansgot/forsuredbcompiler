package com.fsryan.forsuredb.serialization;

import com.fsryan.forsuredb.migration.MigrationSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.*;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class SerialiaztionTest {

    private final String jsonResource;
    private InputStream fileInputStream;
    private InputStream stringInputStream;

    public SerialiaztionTest(String jsonResource) throws FileNotFoundException {
        this.jsonResource = jsonResource;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"00_first_schema.json"},
                {"01_a_legacy_schema.json"},
                {"02_schema_0_7_0.json"},
                {"03_schema_0_8_0.json"},
                {"04_schema_0_9_0.json"},
                {"05_schema_0_12_0.json"}
        });
    }

    @Before
    public void setUpInputStream() throws Exception {
        fileInputStream = new FileInputStream(file("src", "test", "resources", jsonResource));
    }

    @After
    public void closeInputStream() throws Exception {
        fileInputStream.close();
        if (stringInputStream != null) {
            stringInputStream.close();
        }
    }

    @Test
    public void shouldReadAndWriteInversely() throws Exception {
        FSDbInfoSerializer serializerUnderTest = new FSDbInfoGsonSerializer();
        MigrationSet read = serializerUnderTest.deserializeMigrationSet(fileInputStream);

        String wrote = serializerUnderTest.serialize(read);
        stringInputStream = new ByteArrayInputStream(wrote.getBytes());
        MigrationSet readAfterWrote = serializerUnderTest.deserializeMigrationSet(stringInputStream);

        assertEquals(read, readAfterWrote);
    }

    private static File file(String... pathParts) {
        StringBuilder buf = new StringBuilder();
        for (String part : pathParts) {
            buf.append(part).append(File.separator);
        }
        final String path = buf.delete(buf.length() - File.separator.length(), buf.length()).toString();
        return new File(path);
    }
}
