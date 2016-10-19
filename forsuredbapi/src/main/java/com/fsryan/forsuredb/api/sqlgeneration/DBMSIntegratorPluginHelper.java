package com.fsryan.forsuredb.api.sqlgeneration;

import com.fsryan.forsuredb.api.PluginHelper;
import com.google.common.annotations.VisibleForTesting;

/*package*/ class DBMSIntegratorPluginHelper extends PluginHelper<DBMSIntegrator> {

    private static final String DBMS_INTEGRATOR_CLASS = getImplementationClassName(DBMSIntegrator.class);

    public DBMSIntegratorPluginHelper() {
        this(DBMS_INTEGRATOR_CLASS);
    }

    @VisibleForTesting
    /*package*/ DBMSIntegratorPluginHelper(String dbmsIntegratorClass) {
        super(DBMSIntegrator.class, dbmsIntegratorClass);
    }

    @Override
    protected DBMSIntegrator defaultImplementation() {
        throw new IllegalStateException(DBMSIntegrator.class.getSimpleName() + " plugin required for correct operation. Put a file called " + DBMSIntegrator.class.getName() + " in META-INF/services/ whose first line is the name of your " + DBMSIntegrator.class.getSimpleName() + " implementation or just set the forsuredb gradle extension dbmsIntegratorClass property.");
    }
}
