package com.fsryan.forsuredb.annotationprocessor.generator.code;

import com.fsryan.forsuredb.annotationprocessor.FSAnnotationProcessor;
import com.fsryan.forsuredb.annotationprocessor.generator.BaseGenerator;
import com.fsryan.forsuredb.api.TypedRecordContainer;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import javax.annotation.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.fsryan.forsuredb.annotationprocessor.util.PropertyRetriever.properties;

public abstract class JavaSourceGenerator extends BaseGenerator<JavaFileObject> {

    private static final AnnotationSpec GENERATED_ANNOTATION = AnnotationSpec.builder(Generated.class)
            .addMember("value", CodeBlock.builder()
                    .add("$S", ClassName.get(FSAnnotationProcessor.class))
                    .build())
            .build();

    private final String fqClassName;

    private String packageName;
    private String simpleClassName;
    private String resultParameter;
    private String recordContainer;

    public JavaSourceGenerator(ProcessingEnvironment processingEnv, String fqClassName) {
        super(processingEnv);
        this.fqClassName = fqClassName;
    }

    @Override
    protected JavaFileObject createFileObject(ProcessingEnvironment processingEnv) throws IOException {
        return processingEnv.getFiler().createSourceFile(getOutputClassName(true));
    }

    protected List<AnnotationSpec> getClassAnnotations() {
        if (properties().addGeneratedAnnotation()) {
            return Arrays.asList(GENERATED_ANNOTATION);
        }
        return Collections.emptyList();
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
            resultParameter = properties().resultParameter();
            resultParameter = resultParameter == null ? "java.lang.Object" : resultParameter;
        }
        return resultParameter;
    }

    protected String getRecordContainer() {
        if (recordContainer == null) {
            recordContainer = properties().recordContainer();
            recordContainer = recordContainer == null || recordContainer.isEmpty()
                    ? TypedRecordContainer.class.getName()
                    : recordContainer;
        }
        return recordContainer;
    }
}
