package com.forsuredb.annotationprocessor.generator.code;

import com.forsuredb.annotation.FSColumn;
import com.forsuredb.annotationprocessor.generator.NewBaseGenerator;
import com.forsuredb.annotationprocessor.info.ColumnInfo;
import com.forsuredb.annotationprocessor.info.TableInfo;
import com.forsuredb.annotationprocessor.util.APLog;
import com.forsuredb.api.FSSaveApi;
import com.google.common.collect.Lists;
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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NewSetterGenerator extends NewBaseGenerator<JavaFileObject> {

    private final TableInfo table;
    private final List<ColumnInfo> columnsSortedByName;

    public NewSetterGenerator(ProcessingEnvironment processingEnv, TableInfo table) {
        super(processingEnv);
        this.table = table;
        this.columnsSortedByName = columnsSortedByColumnName();
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
                .addSuperinterface(ParameterizedTypeName.get(FSSaveApi.class, getResultParameter()))
                .addJavadoc(javadoc.stringToFormat(), javadoc.replacements());
        for (ColumnInfo column : columnsSortedByName) {
            try {
                codeBuilder.addMethod(methodSpecFor(column));
            } catch (ClassNotFoundException cnfe) {
                APLog.e(logTag(), "failed to find class: " + cnfe.getMessage());
            }
        }
        return JavaFile.builder(table.getPackageName(), codeBuilder.build()).indent(JAVA_INDENT).build().toString();
    }

    private JavadocInfo createSetterJavadoc() {
        JavadocInfo.Builder jib = JavadocInfo.builder()
                .startParagraph()
                .addLine("This is an auto-generated class. DO NOT modify it!")
                .endParagraph()
                .startParagraph()
                .addLine("$L is an automatically generated interface describing the", getOutputClassName(false))
                .addLine("contract for a fluent API for building queries to update or delete one")
                .addLine("or more records from the $L table.", table.getTableName())
                .addLine("You DO NOT need to implement this interface in order to use it.")
                .endParagraph()
                .startParagraph()
                .addLine("Below is an example usage:")
                .startCode()
                .addLine("$L().set()", CodeUtil.snakeToCamel(table.getTableName()));
        for (ColumnInfo column : columnsSortedByName) {
            if ("modified".equals(column.getColumnName()) || "created".equals(column.getColumnName())) {
                continue;
            }
            jib.addLine(".$L($L)", column.getMethodName(), CodeUtil.javaExampleOf(column.getQualifiedType()));
        }
        return jib.addLine(".save()")
                .endCode()
                .endParagraph()
                .addLine("@author <a href=$S>forsuredbcompiler</a>", "https://github.com/ryansgot/forsuredbcompiler")
                .addLine("@see FSSaveApi")
                .addLine()
                .build();
    }

    private MethodSpec methodSpecFor(ColumnInfo column) throws ClassNotFoundException {
        JavadocInfo javadoc = JavadocInfo.builder()
                .startParagraph()
                .addLine("Set the value of the $L column to be updated", column.getColumnName())
                .endParagraph()
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

    private List<ColumnInfo> columnsSortedByColumnName() {
        List<ColumnInfo> columns = Lists.newArrayList(table.getColumns());
        Collections.sort(columns, new Comparator<ColumnInfo>() {
            @Override
            public int compare(ColumnInfo c1, ColumnInfo c2) {
                return c1.getColumnName().compareToIgnoreCase(c2.getColumnName());
            }
        });
        return columns;
    }
}
