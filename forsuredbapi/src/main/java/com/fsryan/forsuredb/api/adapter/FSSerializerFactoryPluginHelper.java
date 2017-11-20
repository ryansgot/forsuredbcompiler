package com.fsryan.forsuredb.api.adapter;

import com.fsryan.forsuredb.api.PluginHelper;

/*package*/ public class FSSerializerFactoryPluginHelper extends PluginHelper<FSSerializerFactory> {

    private static final String FACTORY_CLASS = getImplementationClassName(FSSerializerFactory.class);

    private static final FSSerializerFactory defaultFactory = new FSSerializerFactory() {
        @Override
        public FSSerializer create() {
            return new FSDefaultSerializer();
        }
    };

    public FSSerializerFactoryPluginHelper() {
        this(FACTORY_CLASS);
    }

    // visible for testing
    /*package*/ FSSerializerFactoryPluginHelper(String fsSerializerFactoryClass) {
        super(FSSerializerFactory.class, fsSerializerFactoryClass);
    }

    @Override
    protected FSSerializerFactory defaultImplementation() {
        return defaultFactory;
    }
}
