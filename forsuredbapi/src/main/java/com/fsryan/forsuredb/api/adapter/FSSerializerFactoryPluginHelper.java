package com.fsryan.forsuredb.api.adapter;

import com.fsryan.forsuredb.api.PluginHelper;

/*package*/ class FSSerializerFactoryPluginHelper extends PluginHelper<FSSerializerFactory> {

    private static final String FACTORY_CLASS = getImplementationClassName(FSSerializerFactory.class);

    public FSSerializerFactoryPluginHelper() {
        this(FACTORY_CLASS);
    }

    // visible for testing
    /*package*/ FSSerializerFactoryPluginHelper(String fsSerializerFactoryClass) {
        super(FSSerializerFactory.class, fsSerializerFactoryClass);
    }

    @Override
    protected FSSerializerFactory defaultImplementation() {
        return new FSSerializerFactory() {
            @Override
            public FSSerializer create() {
                return new FSGsonSerializer();
            }
        };
    }
}
