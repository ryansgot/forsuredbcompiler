package com.fsryan.forsuredb.moshiserialization;

import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.serialization.FSDbInfoSerializer;
import com.fsryan.forsuredb.serializationtesting.SerializationTest;
import com.squareup.moshi.JsonAdapter;

import java.lang.reflect.Field;
import java.util.Set;

public abstract class MoshiSerializationTest {

    public static class MigrationSetFromStream extends SerializationTest.MigrationSetFromStream {

        public MigrationSetFromStream(String jsonResource) {
            super(jsonResource);
        }

        @Override
        protected FSDbInfoSerializer createFSDBInfoSerializer() {
            return new FSDbInfoMoshiSerializer();
        }
    }

    public static class MigrationSetFromString extends SerializationTest.MigrationSetFromString {

        public MigrationSetFromString(String jsonResource) {
            super(jsonResource);
        }

        @Override
        protected FSDbInfoSerializer createFSDBInfoSerializer() {
            return new FSDbInfoMoshiSerializer();
        }
    }

    public static class TableForeignKeyInfoSets extends SerializationTest.TableForeignKeyInfoSets {

        public TableForeignKeyInfoSets(String jsonResource) {
            super(jsonResource);
        }

        @Override
        protected String writeObject(Set<TableForeignKeyInfo> object) {
            return acquireSubjectTableForeignKeyInfoSetAdapter().toJson(object);
        }

        @Override
        protected FSDbInfoSerializer createFSDBInfoSerializer() {
            return new FSDbInfoMoshiSerializer();
        }
    }

    public static class ColumnNames extends SerializationTest.ColumnNames {

        public ColumnNames(String jsonResource) {
            super(jsonResource);
        }

        @Override
        protected String writeObject(Set<String> object) {
            try {
                return acquireSubjectStringSetAdapter().toJson(object);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected FSDbInfoSerializer createFSDBInfoSerializer() {
            return new FSDbInfoMoshiSerializer();
        }
    }

    static JsonAdapter<Set<TableForeignKeyInfo>> acquireSubjectTableForeignKeyInfoSetAdapter() {
        return (JsonAdapter<Set<TableForeignKeyInfo>>) reflectivelyAcquireStaticFieldFromSubject("tableForeignKeyInfoSetAdapter");
    }

    static JsonAdapter<Set<String>> acquireSubjectStringSetAdapter() {
        return (JsonAdapter<Set<String>>) reflectivelyAcquireStaticFieldFromSubject("stringSetAdapter");
    }

    static Object reflectivelyAcquireStaticFieldFromSubject(String fieldName) {
        try {
            Field gsonField = FSDbInfoMoshiSerializer.class.getDeclaredField(fieldName);
            gsonField.setAccessible(true);
            return gsonField.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
