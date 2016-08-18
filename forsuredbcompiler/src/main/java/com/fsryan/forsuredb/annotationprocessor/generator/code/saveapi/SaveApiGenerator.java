package com.fsryan.forsuredb.annotationprocessor.generator.code.saveapi;

import com.fsryan.forsuredb.annotationprocessor.generator.code.CodeUtil;
import com.fsryan.forsuredb.annotationprocessor.generator.code.JavaSourceGenerator;
import com.fsryan.forsuredb.annotationprocessor.generator.code.JavadocInfo;
import com.fsryan.forsuredb.annotationprocessor.generator.code.TableDataUtil;
import com.fsryan.forsuredb.annotations.FSColumn;
import com.fsryan.forsuredb.api.info.ColumnInfo;
import com.fsryan.forsuredb.api.info.TableInfo;
import com.fsryan.forsuredb.annotationprocessor.util.APLog;
import com.google.common.collect.Sets;
import com.squareup.javapoet.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import java.util.List;
import java.util.Set;

public abstract class SaveApiGenerator extends JavaSourceGenerator {

    private static final Set<String> BASE_BLOCKED_COLUMNS = Sets.newHashSet("modified", "created");

    private final TableInfo table;
    private final List<ColumnInfo> columnsSortedByName;

    protected SaveApiGenerator(ProcessingEnvironment processingEnv, TableInfo table) {
        super(processingEnv, table.getQualifiedClassName() + "Setter");
        this.table = table;
        this.columnsSortedByName = TableDataUtil.columnsSortedByName(table);
    }

    public static SaveApiGenerator getFor(ProcessingEnvironment processingEnv, TableInfo table) {
        return table.isDocStore() ? new DocStoreSaveApiGenerator(processingEnv, table) : new RelationalSaveApiGenerator(processingEnv, table);
    }

    @Override
    protected String getCode() {
        JavadocInfo javadoc = createSetterJavadoc();
        TypeSpec.Builder codeBuilder = TypeSpec.interfaceBuilder(getOutputClassName(false))
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(createSuperinterfaceParameterizedTypeName(table))
                .addJavadoc(javadoc.stringToFormat(), javadoc.replacements());

        codeBuilder.addField(FieldSpec.builder(String.class, "TABLE_NAME", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer(CodeBlock.builder()
                        .add("$S", table.getTableName())
                        .build())
                .build());


        for (ColumnInfo column : columnsSortedByName) {
            if (isBlockedFromSetterMethods(column)) {
                continue;
            }
            try {
                codeBuilder.addMethod(methodSpecFor(column));
            } catch (ClassNotFoundException cnfe) {
                APLog.e(logTag(), "failed to find class: " + cnfe.getMessage());
            }
        }

        return JavaFile.builder(table.getPackageName(), codeBuilder.build()).indent(JAVA_INDENT).build().toString();
    }

    protected abstract ParameterizedTypeName createSuperinterfaceParameterizedTypeName(TableInfo table);

    /**
     * <p>
     *     If you override this method, then you must call the super class method
     * </p>
     * @param column the {@link ColumnInfo} to check whether there should be a setter method
     * @return true of the column is blocked
     */
    protected boolean isBlockedFromSetterMethods(ColumnInfo column) {
        return BASE_BLOCKED_COLUMNS.contains(column.getColumnName());
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
