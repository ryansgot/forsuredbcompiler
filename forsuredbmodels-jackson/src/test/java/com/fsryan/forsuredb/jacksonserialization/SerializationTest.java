package com.fsryan.forsuredb.jacksonserialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.migration.MigrationSet;
import com.fsryan.forsuredb.serialization.FSDbInfoSerializer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Arrays;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public abstract class SerializationTest {

    private final String jsonResource;
    protected URL source;
    protected FSDbInfoSerializer serializerUnderTest;

    public SerializationTest(String jsonResource) {
        this.jsonResource = jsonResource;
    }

    @Before
    public void setUpInputStream() {
        serializerUnderTest = new FSDbInfoJacksonSerializer();
        source = SerializationTest.class.getClassLoader().getResource(jsonResource);
    }

    public static abstract class SymmetricReadWrite<IS, T> extends SerializationTest {

        private IS inputSource;

        public SymmetricReadWrite(String jsonResource) {
            super(jsonResource);
        }

        @Before
        public void convertInputSource() {
            inputSource = convertToInputSource(source);
        }

        @Test
        public void shouldReadAndWriteSymmetrically() throws Exception {
            T read = readObject(inputSource);

            String wrote = writeObject(read);
            IS wroteIs = convertToInputSource(wrote);
            T readAfterWrote = readObject(wroteIs);

            assertEquals(read, readAfterWrote);
            cleanUp(wroteIs);
        }

        protected abstract IS convertToInputSource(URL input);
        protected abstract IS convertToInputSource(String json);
        protected abstract T readObject(IS source);
        protected abstract String writeObject(T object);
        protected abstract void cleanUp(IS objectToClean);
    }

    public static abstract class SymmetricReadWriteString<T> extends SymmetricReadWrite<String, T> {

        public SymmetricReadWriteString(String jsonResource) {
            super(jsonResource);
        }

        @Override
        protected final String convertToInputSource(URL input) {
            try (BufferedReader inBuf = new BufferedReader(new InputStreamReader(input.openStream()))) {
                String line = null;
                StringBuilder outBuf = new StringBuilder();
                while ((line = inBuf.readLine()) != null) {
                    outBuf.append(line).append("\n");
                }
                return outBuf.delete(outBuf.length() - 1, outBuf.length()).toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected final String convertToInputSource(String input) {
            return input;
        }

        @Override
        protected final void cleanUp(String objectToClean) {
            // do nothing
        }
    }

    public static abstract class SymmetricReadWriteInputStream<T> extends SymmetricReadWrite<InputStream, T> {

        public SymmetricReadWriteInputStream(String jsonResource) {
            super(jsonResource);
        }

        @Override
        protected final InputStream convertToInputSource(URL input) {
            try {
                return input.openStream();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected final InputStream convertToInputSource(String json) {
            return new ByteArrayInputStream(json.getBytes());
        }

        @Override
        protected final void cleanUp(InputStream objectToClean) {
            try {
                objectToClean.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @RunWith(Parameterized.class)
    public static class MigrationSetFromStream extends SymmetricReadWriteInputStream<MigrationSet> {

        public MigrationSetFromStream(String jsonResource) {
            super(jsonResource);
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

        @Override
        protected MigrationSet readObject(InputStream source) {
            return serializerUnderTest.deserializeMigrationSet(source);
        }

        @Override
        protected String writeObject(MigrationSet migrationSet) {
            return serializerUnderTest.serialize(migrationSet);
        }
    }

    @RunWith(Parameterized.class)
    public static class MigrationSetFromString extends SymmetricReadWriteString<MigrationSet> {

        public MigrationSetFromString(String jsonResource) {
            super(jsonResource);
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

        @Override
        protected MigrationSet readObject(String source) {
            return serializerUnderTest.deserializeMigrationSet(source);
        }

        @Override
        protected String writeObject(MigrationSet migrationSet) {
            return serializerUnderTest.serialize(migrationSet);
        }
    }

    @RunWith(Parameterized.class)
    public static class TableForeignKeyInfoSets extends SymmetricReadWriteString<Set<TableForeignKeyInfo>> {

        public TableForeignKeyInfoSets(String jsonResource) {
            super(jsonResource);
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {"table_foreign_keys_set.json"},
            });
        }

        @Override
        protected Set<TableForeignKeyInfo> readObject(String json) {
            return serializerUnderTest.deserializeForeignKeys(json);
        }

        @Override
        protected String writeObject(Set<TableForeignKeyInfo> object) {
            try {
                return acquireMapper(serializerUnderTest).writeValueAsString(object);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @RunWith(Parameterized.class)
    public static class ColumnNames extends SymmetricReadWriteString<Set<String>> {

        public ColumnNames(String jsonResource) {
            super(jsonResource);
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {"string_set.json"},
            });
        }

        @Override
        protected Set<String> readObject(String json) {
            return serializerUnderTest.deserializeColumnNames(json);
        }

        @Override
        protected String writeObject(Set<String> object) {
            try {
                return acquireMapper(serializerUnderTest).writeValueAsString(object);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    static ObjectMapper acquireMapper(Object holder) throws Exception {
        Field mapperField = FSDbInfoJacksonSerializer.class.getDeclaredField("mapper");
        mapperField.setAccessible(true);
        return (ObjectMapper) mapperField.get(holder);
    }
}