package com.fsryan.forsuredb.api.adapter;

import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.Field;

import static junit.framework.TestCase.assertEquals;

// TODO: more rigorous testing--perhaps using ParametrizedRunner

public abstract class BuiltInSerializerTest {

    private final Object input;

    public BuiltInSerializerTest(Object input) {
        this.input = input;
    }

    @Test
    public void serializedObjectShouldDeserializeToEqualObject() throws Exception {
        Object deserialized = serializeThenDeserialize(input);
        for (Field f : TestSerializable.class.getDeclaredFields()) {
            f.setAccessible(true);
            assertEquals(f.getName() + " was not as expected", f.get(input), f.get(deserialized));
        }
    }

    protected abstract <T> T serializeThenDeserialize(T o);

    public static class FSSerializableSerializerSerialization extends BuiltInSerializerTest {

        public FSSerializableSerializerSerialization() {
            super(TestSerializable.builder().stringField("This is a String")
                    .intField(56)
                    .longField(84573875L)
                    .floatField(457.67F)
                    .doubleField(3.5D)
                    .build());
        }

        @Override
        protected <T> T serializeThenDeserialize(T o) {
            FSSerializer serializableSerializer = new FSSerializableSerializer();
            byte[] objectBytes = serializableSerializer.createBlobDoc(o.getClass(), o);
            return serializableSerializer.fromStorage(o.getClass(), objectBytes);
        }
    }

    public static class FSGsonSerializerSerialization extends BuiltInSerializerTest {

        public FSGsonSerializerSerialization() {
            super(TestSerializable.builder().stringField("This is a String")
                    .intField(56)
                    .longField(84573875L)
                    .floatField(457.67F)
                    .doubleField(3.5D)
                    .build());
        }

        @Override
        protected <T> T serializeThenDeserialize(T o) {
            FSSerializer gsonSerializer = new FSGsonSerializer();
            String json = gsonSerializer.createStringDoc(o.getClass(), o);
            return gsonSerializer.fromStorage(o.getClass(), json);
        }
    }

    @lombok.Data
    @lombok.Builder(builderClassName = "Builder")
    private static class TestSerializable implements Serializable {

        private static final long serialVersionUID = 9247L;

        private String stringField;
        private int intField;
        private long longField;
        private double doubleField;
        private float floatField;
    }
}
