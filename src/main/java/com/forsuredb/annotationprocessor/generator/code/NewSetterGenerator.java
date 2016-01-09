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
        JavadocInfo javadoc = createSetterJavadoc();
        TypeSpec.Builder codeBuilder = TypeSpec.interfaceBuilder(getOutputClassName(false))
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(FSSaveApi.class, resultParameter))
                .addJavadoc(javadoc.stringToFormat(), javadoc.replacements());
        for (ColumnInfo column : table.getColumns()) {
            try {
                codeBuilder.addMethod(methodSpecFor(column));
            } catch (ClassNotFoundException cnfe) {
                APLog.e(logTag(), "failed to find class: " + cnfe.getMessage());
            }
        }
        return JavaFile.builder(table.getPackageName(), codeBuilder.build()).indent("    ").build().toString();
    }

    private JavadocInfo createSetterJavadoc() {
        JavadocInfo.Builder jib = JavadocInfo.builder()
                .addLine("<p>")
                .indent()
                .addLine("This is an auto-generated class. DO NOT modify it!")
                .unindent()
                .addLine("</p>")
                .indent()
                .addLine("$L is an automatically generated interface describing the", getOutputClassName(false))
                .addLine("contract for a fluent API for building queries to update or delete one")
                .addLine("or more records from the $L table.", table.getTableName())
                .addLine("You DO NOT need to implement this interface in order to use it.")
                .unindent()
                .addLine("</p>")
                .addLine("<p>")
                .indent()
                .addLine("Below is an example usage:")
                .startCode()
                .addLine("$L().set()", CodeUtil.snakeToCamel(table.getTableName()));
        for (ColumnInfo column : table.getColumns()) {
            if ("modified".equals(column.getColumnName()) || "created".equals(column.getColumnName())) {
                continue;
            }
            jib.addLine(".$L($L)", column.getMethodName(), CodeUtil.javaExampleOf(column.getQualifiedType()));
        }
        return jib.addLine(".save()")
                .endCode()
                .unindent()
                .addLine("</p>")
                .addLine("<p>")
                .addLine("@author <a href=$S>forsuredbcompiler</a>", "https://github.com/ryansgot/forsuredbcompiler")
                .addLine("@see FSSaveApi")
                .addLine()
                .build();
    }

    private MethodSpec methodSpecFor(ColumnInfo column) throws ClassNotFoundException {
        JavadocInfo javadoc = JavadocInfo.builder()
                .addLine("<p>")
                .indent()
                .addLine("Set the value of the $L column to be updated", column.getColumnName())
                .unindent()
                .addLine("</p>")
                .addLine()
                .build();
        return MethodSpec.methodBuilder(column.getMethodName())
                .addJavadoc(javadoc.stringToFormat(), javadoc.replacements())
                .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(FSColumn.class)
                        .addMember("value", "$S", column.getColumnName())
                        .build())
                .returns(ClassName.get(table.getPackageName(), getOutputClassName(false)))
                .addParameter(CodeUtil.typeFromName(column.getQualifiedType()), column.getMethodName())
                .build();
    }

    private String getOutputClassName(boolean fullyQualified) {
        return (fullyQualified ? table.getQualifiedClassName() : table.getSimpleClassName()) + "Setter";
    }
}
