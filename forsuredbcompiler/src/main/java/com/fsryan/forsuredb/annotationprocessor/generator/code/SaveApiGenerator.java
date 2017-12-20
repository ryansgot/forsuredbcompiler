package com.fsryan.forsuredb.annotationprocessor.generator.code;

import com.fsryan.forsuredb.annotations.FSColumn;
import com.fsryan.forsuredb.api.FSDocStoreSaveApi;
import com.fsryan.forsuredb.api.FSSaveApi;
import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.annotationprocessor.util.APLog;
import com.google.common.collect.Sets;
import com.squareup.javapoet.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import java.util.List;
import java.util.Set;

public abstract class SaveApiGenerator extends JavaSourceGenerator {

    private static final Set<String> BASE_BLOCKED_COLUMNS = Sets.newHashSet("modified", "created");

    protected final TableInfo table;
    private final List<ColumnInfo> columnsSortedByName;

    protected SaveApiGenerator(ProcessingEnvironment processingEnv, TableInfo table) {
        super(processingEnv, table.qualifiedClassName() + "SaveApi");
        this.table = table;
        this.columnsSortedByName = TableDataUtil.columnsSortedByName(table);
    }

    public static SaveApiGenerator getFor(ProcessingEnvironment processingEnv, TableInfo table) {
        return table.isDocStore()
                ? new DocStore(processingEnv, table)
                : new Relational(processingEnv, table);
    }

    @Override
    protected String getCode() {
        JavadocInfo javadoc = createSetterJavadoc();
        TypeSpec.Builder codeBuilder = TypeSpec.interfaceBuilder(getOutputClassName(false))
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(createSuperinterfaceParameterizedTypeName(table))
                .addJavadoc(javadoc.stringToFormat(), javadoc.replacements());

        addFields(codeBuilder);

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

    /**
     * <p>
     *     If you override this method, then you must callthe super class method
     * </p>
     * @param codeBuilder The {@link TypeSpec.Builder} used to generate this code
     */
    protected void addFields(TypeSpec.Builder codeBuilder) {
        codeBuilder.addField(FieldSpec.builder(String.class, "TABLE_NAME", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer(CodeBlock.builder()
                        .add("$S", table.tableName())
                        .build())
                .build());
    }

    private JavadocInfo createSetterJavadoc() {
        JavadocInfo.Builder jib = JavadocInfo.builder()
                .startParagraph()
                .addLine("This is an auto-generated class. DO NOT modify it!")
                .endParagraph()
                .startParagraph()
                .addLine("$L is an automatically generated interface describing the", getOutputClassName(false))
                .addLine("contract for a fluent API for building queries to update or delete one")
                .addLine("or more records from the $L table.", table.tableName())
                .addLine("You DO NOT need to implement this interface in order to use it.")
                .endParagraph()
                .startParagraph()
                .addLine("Below is an example usage:")
                .startCode()
                .addLine("$L().set()", CodeUtil.snakeToCamel(table.tableName()));
        for (ColumnInfo column : columnsSortedByName) {
            if ("modified".equals(column.getColumnName()) || "created".equals(column.getColumnName())) {
                continue;
            }
            jib.addLine(".$L($L)", column.methodName(), CodeUtil.javaExampleOf(column.getQualifiedType()));
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
        return MethodSpec.methodBuilder(column.methodName())
                .addJavadoc(javadoc.stringToFormat(), javadoc.replacements())
                .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(FSColumn.class)
                        .addMember("value", "$S", column.getColumnName())
                        .build())
                .returns(ClassName.get(table.getPackageName(), getOutputClassName(false)))
                .addParameter(CodeUtil.typeFromName(column.getQualifiedType()), column.methodName())
                .build();
    }

    static class DocStore extends SaveApiGenerator {

        private static final Set<String> DOC_STORE_READ_ONLY_COLUMNS = Sets.newHashSet("class_name", "doc", "blob_doc");

        protected DocStore(ProcessingEnvironment processingEnv, TableInfo table) {
            super(processingEnv, table);
        }

        @Override
        public void addFields(TypeSpec.Builder codeBuilder) {
            super.addFields(codeBuilder);
            codeBuilder.addField(FieldSpec.builder(Class.class, "BASE_CLASS", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer(CodeBlock.builder()
                            .add("$L.class", table.docStoreParameterization())
                            .build())
                    .build());
        }

        @Override
        protected ParameterizedTypeName createSuperinterfaceParameterizedTypeName(TableInfo table) {
            final ClassName raw = ClassName.get(FSDocStoreSaveApi.class);
            final ClassName resultParam = ClassName.bestGuess(getResultParameter());
            final ClassName docStoreParam = ClassName.bestGuess(table.docStoreParameterization());
            return ParameterizedTypeName.get(raw, resultParam, docStoreParam);
        }

        @Override
        protected boolean isBlockedFromSetterMethods(ColumnInfo column) {
            return DOC_STORE_READ_ONLY_COLUMNS.contains(column.getColumnName())
                    || super.isBlockedFromSetterMethods(column);
        }
    }

    static class Relational extends SaveApiGenerator {

        protected Relational(ProcessingEnvironment processingEnv, TableInfo table) {
            super(processingEnv, table);
        }

        @Override
        protected ParameterizedTypeName createSuperinterfaceParameterizedTypeName(TableInfo table) {
            return ParameterizedTypeName.get(ClassName.get(FSSaveApi.class), ClassName.bestGuess(getResultParameter()));
        }
    }
}
