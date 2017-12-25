package com.fsryan.forsuredb.annotationprocessor.util;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.HashMap;
import java.util.Map;

public class PropertyRetriever {

    private static PropertyRetriever instance;

    private final Map<String, String> options = new HashMap<>();

    private PropertyRetriever(ProcessingEnvironment processingEnv) {
        options.putAll(processingEnv.getOptions());
        for (Map.Entry<String, String> entry : options.entrySet()) {
            APLog.i("PropertyRetriever", "got option " + entry.getKey() + "=" + entry.getValue());
        }
    }

    public static void init(ProcessingEnvironment processingEnv) {
        if (instance == null) {
            instance = new PropertyRetriever(processingEnv);
        }
    }

    public static PropertyRetriever properties() {
        if (instance == null) {
            throw new IllegalStateException("must call init before properties");
        }
        return instance;
    }

    public boolean addGeneratedAnnotation() {
        return Boolean.getBoolean("forsuredb.addGeneratedAnnotation")
                || Boolean.parseBoolean(options.get("forsuredb.addGeneratedAnnotation"));
    }

    public String applicationPackage() {
        String ret = System.getProperty("forsuredb.applicationPackageName");
        return ret == null ? options.get("forsuredb.applicationPackageName") : ret;
    }

    public String migrationDirectory() {
        String ret = System.getProperty("forsuredb.migrationDirectory");
        return ret == null ? options.get("forsuredb.migrationDirectory") : ret;
    }

    public String resultParameter() {
        String ret = System.getProperty("forsuredb.resultParameter");
        return ret == null ? options.get("forsuredb.resultParameter") : ret;
    }

    public boolean createMigrations() {
        return Boolean.getBoolean("forsuredb.createMigrations")
                || Boolean.parseBoolean(options.get("forsuredb.createMigrations"));
    }

    public String recordContainer() {
        String ret = System.getProperty("forsuredb.recordContainer");
        return ret == null ? options.get("forsuredb.recordContainer") : ret;
    }
}
