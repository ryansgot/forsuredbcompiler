package com.fsryan.forsuredb.annotationprocessor.generator.code;

import com.fsryan.forsuredb.annotations.FSColumn;
import com.fsryan.forsuredb.api.info.ColumnInfo;
import com.fsryan.forsuredb.api.info.TableInfo;
import com.fsryan.forsuredb.annotationprocessor.util.APLog;
import com.fsryan.forsuredb.api.FSSaveApi;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import java.util.List;

public class SetterGenerator extends JavaSourceGenerator {

    private final TableInfo table;
    private final List<ColumnInfo> columnsSortedByName;

    public SetterGenerator(ProcessingEnvironment processingEnv, TableInfo table) {
        super(processingEnv, table.getQualifiedClassName() + "Setter");
        this.table = table;
        this.columnsSortedByName = TableDataUtil.columnsSortedByName(table);
    }

    @Override
    protected String getCode() {
        JavadocInfo javadoc = createSetterJavadoc();
        TypeSpec.Builder codeBuilder = TypeSpec.interfaceBuilder(getOutputClassName(false))
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(FSSaveApi.class), ClassName.bestGuess(getResultParameter())))
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
                .addLine(JavadocInfo.AUTHOR_STRING)
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
}
