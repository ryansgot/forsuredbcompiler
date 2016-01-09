package com.forsuredb.annotationprocessor.generator.code;

import com.forsuredb.annotationprocessor.generator.NewBaseGenerator;
import com.forsuredb.annotationprocessor.util.APLog;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;
import java.io.IOException;

public abstract class JavaSourceGenerator extends NewBaseGenerator<JavaFileObject> {

    private final String fqClassName;

    private String packageName;
    private String simpleClassName;
    private Class<?> resultParameter;

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
            resultParameter = createResultParameter();
        }
        return resultParameter;
    }

    private Class<?> createResultParameter() {
        try {
            return Class.forName(System.getProperty("resultParameter"));
        } catch (ClassNotFoundException cnfe) {
            APLog.e(logTag(), "Could not get result parameter: " + cnfe.getMessage());
        }
        return Object.class;
    }
}
