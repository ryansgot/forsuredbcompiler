package com.forsuredb.annotationprocessor.generator.code;

import com.forsuredb.annotationprocessor.generator.NewBaseGenerator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;
import java.io.IOException;

public abstract class JavaSourceGenerator extends NewBaseGenerator<JavaFileObject> {

    private final String fqClassName;
    private String packageName;
    private String simpleClassName;

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
}
