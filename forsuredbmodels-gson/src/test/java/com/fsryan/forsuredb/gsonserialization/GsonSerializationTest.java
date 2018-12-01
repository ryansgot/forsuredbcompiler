package com.fsryan.forsuredb.gsonserialization;

import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.serialization.FSDbInfoSerializer;
import com.fsryan.forsuredb.serializationtesting.SerializationTest;
import com.google.gson.Gson;

import java.lang.reflect.Field;
import java.util.Set;

public abstract class GsonSerializationTest {

    public static class MigrationSetFromStream extends SerializationTest.MigrationSetFromStream {

        public MigrationSetFromStream(String jsonResource) {
            super(jsonResource);
        }

        @Override
        protected FSDbInfoSerializer createFSDBInfoSerializer() {
            return new FSDbInfoGsonSerializer();
        }
    }

    public static class MigrationSetFromString extends SerializationTest.MigrationSetFromString {

        public MigrationSetFromString(String jsonResource) {
            super(jsonResource);
        }

        @Override
        protected FSDbInfoSerializer createFSDBInfoSerializer() {
            return new FSDbInfoGsonSerializer();
        }
    }

    public static class TableForeignKeyInfoSets extends SerializationTest.TableForeignKeyInfoSets {

        public TableForeignKeyInfoSets(String jsonResource) {
            super(jsonResource);
        }

        @Override
        protected String writeObject(Set<TableForeignKeyInfo> object) {
            return acquireSubjectGson().toJson(object);
        }

        @Override
        protected FSDbInfoSerializer createFSDBInfoSerializer() {
            return new FSDbInfoGsonSerializer();
        }
    }

    public static class ColumnNames extends SerializationTest.ColumnNames {

        public ColumnNames(String jsonResource) {
            super(jsonResource);
        }

        @Override
        protected String writeObject(Set<String> object) {
            try {
                return acquireSubjectGson().toJson(object);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected FSDbInfoSerializer createFSDBInfoSerializer() {
            return new FSDbInfoGsonSerializer();
        }
    }

    static Gson acquireSubjectGson() {
        try {
            Field gsonField = FSDbInfoGsonSerializer.class.getDeclaredField("gson");
            gsonField.setAccessible(true);
            return (Gson) gsonField.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}