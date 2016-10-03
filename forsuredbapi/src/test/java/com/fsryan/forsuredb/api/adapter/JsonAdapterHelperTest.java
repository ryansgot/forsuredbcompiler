package com.fsryan.forsuredb.api.adapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public abstract class JsonAdapterHelperTest {

    protected final String fsJsonAdapterFactoryClass;

    public JsonAdapterHelperTest(String fsJsonAdapterFactoryClass) {
        this.fsJsonAdapterFactoryClass = fsJsonAdapterFactoryClass;
    }


    @Test
    public void outputShouldNotBeNull() {
        assertNotNull(new FSSerializerAdapterHelper(fsJsonAdapterFactoryClass).getNew());
    }

    @RunWith(Parameterized.class)
    public static class ErrorConditions extends JsonAdapterHelperTest {

        public ErrorConditions(String fsJsonAdapterFactoryClass) {
            super(fsJsonAdapterFactoryClass);
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {NotAnFSJsonAdapterFactory.class.getName()},
                    {FSJsonAdapterFactoryNullImpl.class.getName()},
                    {"class.not.found.Cls"},
                    {null},
                    {""}
            });
        }

        @Test
        public void shouldBeNewGsonInstance() {
            assertNotEquals(FSJsonAdapterFactoryImpl.GSON, new FSSerializerAdapterHelper(fsJsonAdapterFactoryClass).getNew());
        }

    }

    public static class SuccessCondition extends JsonAdapterHelperTest {

        public SuccessCondition() {
            super(FSJsonAdapterFactoryImpl.class.getName());
        }

        @Test
        public void shouldBeFSJsonAdapterFactoryImplInstance() {
            assertEquals(FSJsonAdapterFactoryImpl.GSON, new FSSerializerAdapterHelper(fsJsonAdapterFactoryClass).getNew());
        }
    }

    /*package*/ static class FSJsonAdapterFactoryImpl implements FSSerializationAdapterFactory {

        /*package*/ static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

        @Override
        public Gson create() {
            return GSON;
        }
    }

    /*package*/ static class FSJsonAdapterFactoryNullImpl implements FSSerializationAdapterFactory {
        @Override
        public Gson create() {
            return null;
        }
    }

    /*package*/ static class NotAnFSJsonAdapterFactory {
    }
}
