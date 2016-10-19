package com.fsryan.forsuredb.api.adapter;

import com.google.gson.GsonBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class FSSerializerFactoryHelperTest {

    protected final String fsSerializerFactoryClass;

    public FSSerializerFactoryHelperTest(String fsSerializerFactoryClass) {
        this.fsSerializerFactoryClass = fsSerializerFactoryClass;
    }

    @Test
    public void outputShouldNotBeNull() {
        assertNotNull(new FSSerializerFactoryPluginHelper(fsSerializerFactoryClass).getNew());
    }

    @RunWith(Parameterized.class)
    public static class ErrorConditions extends FSSerializerFactoryHelperTest {

        public ErrorConditions(String fsJsonAdapterFactoryClass) {
            super(fsJsonAdapterFactoryClass);
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {NotAnFSSerializerFactory.class.getName()},
                    {FSSerializerFactoryWithNullImpl.class.getName()},
                    {FSSerializerFactoryWithoutEmptyConstructor.class.getName()},
                    {"class.not.found.Cls"},
                    {null},
                    {""}
            });
        }

        @Test
        public void shouldDefaultToGsonSerializer() {
            assertEquals(FSGsonSerializer.class, new FSSerializerFactoryPluginHelper(fsSerializerFactoryClass).getNew().create().getClass());
        }

    }

    public static class SuccessCondition extends FSSerializerFactoryHelperTest {

        public SuccessCondition() {
            super(FSGsonSerializerFactoryImpl.class.getName());
        }

        @Test
        public void shouldBeFSJsonAdapterFactoryImplInstance() {
            assertEquals(FSGsonSerializerFactoryImpl.gsonSerializer, new FSSerializerFactoryPluginHelper(fsSerializerFactoryClass).getNew().create());
        }
    }

    public static class FSGsonSerializerFactoryImpl implements FSSerializerFactory {

        /*package*/ static FSSerializer gsonSerializer = new FSGsonSerializer(new GsonBuilder().setPrettyPrinting().create());

        @Override
        public FSSerializer create() {
            return gsonSerializer;
        }
    }

    /*package*/ static class FSSerializerFactoryWithNullImpl implements FSSerializerFactory {
        @Override
        public FSSerializer create() {
            return null;
        }
    }

    /*package*/ static class FSSerializerFactoryWithoutEmptyConstructor implements FSSerializerFactory {

        public FSSerializerFactoryWithoutEmptyConstructor(int arg) {

        }

        @Override
        public FSSerializer create() {
            return null;
        }
    }

    /*package*/ static class NotAnFSSerializerFactory {
    }
}
