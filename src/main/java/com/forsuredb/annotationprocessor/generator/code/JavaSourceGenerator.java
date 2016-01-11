package com.forsuredb.annotationprocessor.generator.code;

import com.forsuredb.annotationprocessor.generator.BaseGenerator;
import com.forsuredb.annotationprocessor.util.APLog;
import com.forsuredb.api.RecordContainer;
import com.forsuredb.api.TypedRecordContainer;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;
import java.io.IOException;

public abstract class JavaSourceGenerator extends BaseGenerator<JavaFileObject> {

    private final String fqClassName;

    private String packageName;
    private String simpleClassName;
    private String resultParameter;
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

    protected String getResultParameter() {
        if (resultParameter == null) {
            resultParameter = System.getProperty("resultParameter");
            resultParameter = resultParameter == null ? "java.lang.Object" : resultParameter;
        }
        return resultParameter;
    }

    protected Class<? extends RecordContainer> getRecordContainerClass() {
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
