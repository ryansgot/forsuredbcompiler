package com.forsuredb.annotationprocessor.generator.code;

import com.forsuredb.annotationprocessor.generator.NewBaseGenerator;
import com.forsuredb.annotationprocessor.util.APLog;
import com.forsuredb.api.RecordContainer;
import com.forsuredb.api.TypedRecordContainer;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;
import java.io.IOException;

public abstract class JavaSourceGenerator extends NewBaseGenerator<JavaFileObject> {

    private final String fqClassName;

    private String packageName;
    private String simpleClassName;
    private Class<?> resultParameter;
    private Class<? extends RecordContainer> recordContainer;

    public JavaSourceGenerator(ProcessingEnvironment processingEnv, String fqClassName) {
        super(processingEnv);
        this.fqClassName = fqClassName;
    }

    @Override
    protected JavaFileObject createFileObject(ProcessingEnvironment processingEnv) throws IOException {
        return processingEnv.getFiler().createSourceFile(getOutputClassName(true));
    }

    protected String getOutputClassName(boolean fullyQualified) {
        if (fullyQualified) {
            return fqClassName;
        }

        if (simpleClassName == null) {
            simpleClassName = CodeUtil.simpleClassNameFrom(fqClassName);
        }
        return simpleClassName;
    }

    protected String getOutputPackageName() {
        if (packageName == null) {
            packageName = CodeUtil.packageNameFrom(fqClassName);
        }
        return packageName;
    }

    protected Class<?> getResultParameter() {
        if (resultParameter == null) {
            try {
                resultParameter = Class.forName(System.getProperty("resultParameter"));
            } catch (ClassNotFoundException cnfe) {
                APLog.e(logTag(), "Could not get result parameter: " + cnfe.getMessage());
                resultParameter = Object.class;
            }
        }
        return resultParameter;
    }

    protected Class<? extends RecordContainer> getRecordContainer() {
        if (recordContainer == null) {
            try {
                recordContainer = Class.forName(System.getProperty("recordContainer")).asSubclass(RecordContainer.class);
            } catch (Exception e) {
                APLog.e(logTag(), "Could not get record container:" + e.getMessage());
                recordContainer = TypedRecordContainer.class;
            }
        }
        return recordContainer;
    }
}
