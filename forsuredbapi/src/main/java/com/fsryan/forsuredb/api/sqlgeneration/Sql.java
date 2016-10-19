package com.fsryan.forsuredb.api.sqlgeneration;

public class Sql {

    private static DBMSIntegrator dbmsIntegrator = new DBMSIntegratorPluginHelper().getNew();

    public static DBMSIntegrator generator() {
        return dbmsIntegrator;
    }
}
