package com.forsuredb.annotationprocessor.generator.code;

import com.forsuredb.annotation.FSColumn;
import com.forsuredb.annotationprocessor.generator.NewBaseGenerator;
import com.forsuredb.annotationprocessor.info.ColumnInfo;
import com.forsuredb.annotationprocessor.info.TableInfo;
import com.forsuredb.annotationprocessor.util.APLog;
import com.forsuredb.api.FSSaveApi;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.lang.reflect.Type;

public class NewSetterGenerator extends NewBaseGenerator<JavaFileObject> {

    private final TableInfo table;

    public NewSetterGenerator(ProcessingEnvironment processingEnv, TableInfo table) {
        super(processingEnv);
        this.table = table;
    }

    @Override
    protected JavaFileObject createFileObject(ProcessingEnvironment processingEnv) throws IOException {
        return processingEnv.getFiler().createSourceFile(getOutputClassName(true));
    }

    @Override
    protected String getCode() {
        TypeSpec.Builder codeBuilder = TypeSpec.interfaceBuilder(getOutputClassName(false))
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(FSSaveApi.class, resultParameter));
        // TODO: add javadocs
        //codeBuilder.addJavadoc(/**/)
        for (ColumnInfo column : table.getColumns()) {
            try {
                codeBuilder.addMethod(methodSpecFor(column));
            } catch (ClassNotFoundException cnfe) {
                APLog.e(logTag(), "failed to find class: " + cnfe.getMessage());
            }
        }
        return JavaFile.builder(table.getPackageName(), codeBuilder.build()).indent("    ").build().toString();
    }

    private MethodSpec methodSpecFor(ColumnInfo column) throws ClassNotFoundException {
        return MethodSpec.methodBuilder(column.getMethodName())
                // TODO: add javadocs
                .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(FSColumn.class)
                        .addMember("value", "$S", column.getColumnName())
                        .build())
                .returns(ClassName.get(table.getPackageName(), getOutputClassName(false)))
                .addParameter(fromFQTypeName(column.getQualifiedType()), column.getMethodName())
                .build();
    }

    private String getOutputClassName(boolean fullyQualified) {
        return (fullyQualified ? table.getQualifiedClassName() : table.getSimpleClassName()) + "Setter";
    }

    private Type fromFQTypeName(String fqTypeName) {
        switch (fqTypeName) {
            case "char":
                return char.class;
            case "byte":
                return byte.class;
            case "byte[]":
                return byte[].class;
            case "boolean":
                return boolean.class;
            case "short":
                return short.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            default:
                try {
                    return Class.forName(fqTypeName);
                } catch (ClassNotFoundException cnfe) {
                    APLog.e(logTag(), "could not find type for class: " + fqTypeName);
                }
        }

        throw new IllegalStateException("could not find type for class: " + fqTypeName);
    }
}
