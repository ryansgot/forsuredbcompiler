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

    public String applicationPackage() {
        String ret = System.getProperty("applicationPackageName");
        return ret == null ? options.get("applicationPackageName") : ret;
    }

    public String migrationDirectory() {
        String ret = System.getProperty("migrationDirectory");
        return ret == null ? options.get("migrationDirectory") : ret;
    }

    public String resultParameter() {
        String ret = System.getProperty("resultParameter");
        return ret == null ? options.get("resultParameter") : ret;
    }

    public boolean createMigrations() {
        return Boolean.getBoolean("createMigrations")
                || Boolean.parseBoolean(options.get("createMigrations"));
    }

    public String recordContainer() {
        String ret = System.getProperty("recordContainer");
        return ret == null ? options.get("recordContainer") : ret;
    }
}
