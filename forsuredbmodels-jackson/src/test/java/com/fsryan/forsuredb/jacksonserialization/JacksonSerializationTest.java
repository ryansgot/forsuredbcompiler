package com.fsryan.forsuredb.jacksonserialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.migration.MigrationSet;
import com.fsryan.forsuredb.serialization.FSDbInfoSerializer;
import com.fsryan.forsuredb.serializationtesting.SerializationTest;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Set;

public abstract class JacksonSerializationTest {

    public static class MigrationSetFromStream extends SerializationTest.MigrationSetFromStream {

        public MigrationSetFromStream(String jsonResource) {
            super(jsonResource);
        }

        @Override
        protected MigrationSet readObject(InputStream source) {
            return serializerUnderTest.deserializeMigrationSet(source);
        }

        @Override
        protected String writeObject(MigrationSet migrationSet) {
            return serializerUnderTest.serialize(migrationSet);
        }

        @Override
        protected FSDbInfoSerializer createFSDBInfoSerializer() {
            return new FSDbInfoJacksonSerializer();
        }
    }

    public static class MigrationSetFromString extends SerializationTest.MigrationSetFromString {

        public MigrationSetFromString(String jsonResource) {
            super(jsonResource);
        }

        @Override
        protected MigrationSet readObject(String source) {
            return serializerUnderTest.deserializeMigrationSet(source);
        }

        @Override
        protected String writeObject(MigrationSet migrationSet) {
            return serializerUnderTest.serialize(migrationSet);
        }

        @Override
        protected FSDbInfoSerializer createFSDBInfoSerializer() {
            return new FSDbInfoJacksonSerializer();
        }
    }

    public static class TableForeignKeyInfoSets extends SerializationTest.TableForeignKeyInfoSets {

        public TableForeignKeyInfoSets(String jsonResource) {
            super(jsonResource);
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

        @Override
        protected FSDbInfoSerializer createFSDBInfoSerializer() {
            return new FSDbInfoJacksonSerializer();
        }
    }

    public static class ColumnNames extends SerializationTest.ColumnNames {

        public ColumnNames(String jsonResource) {
            super(jsonResource);
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

        @Override
        protected FSDbInfoSerializer createFSDBInfoSerializer() {
            return new FSDbInfoJacksonSerializer();
        }
    }

    static ObjectMapper acquireMapper(Object holder) throws Exception {
        Field mapperField = FSDbInfoJacksonSerializer.class.getDeclaredField("mapper");
        mapperField.setAccessible(true);
        return (ObjectMapper) mapperField.get(holder);
    }
}