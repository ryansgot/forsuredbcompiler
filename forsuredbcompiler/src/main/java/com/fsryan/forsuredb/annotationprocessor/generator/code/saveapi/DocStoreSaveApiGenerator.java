package com.fsryan.forsuredb.annotationprocessor.generator.code.saveapi;


import com.fsryan.forsuredb.api.FSDocStoreSaveApi;
import com.fsryan.forsuredb.api.info.ColumnInfo;
import com.fsryan.forsuredb.api.info.TableInfo;
import com.google.common.collect.Sets;
import com.squareup.javapoet.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.util.Set;

/*package*/ class DocStoreSaveApiGenerator extends SaveApiGenerator {

    private static final Set<String> DOC_STORE_READ_ONLY_COLUMNS = Sets.newHashSet("class_name", "doc");

    protected DocStoreSaveApiGenerator(ProcessingEnvironment processingEnv, TableInfo table) {
        super(processingEnv, table);
    }

    @Override
    public void addFields(TypeSpec.Builder codeBuilder) {
        super.addFields(codeBuilder);
        codeBuilder.addField(FieldSpec.builder(Class.class, "BASE_CLASS", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer(CodeBlock.builder()
                        .add("$L.class", table.getDocStoreParameterization())
                        .build())
                .build());
    }

    @Override
    protected ParameterizedTypeName createSuperinterfaceParameterizedTypeName(TableInfo table) {
        return ParameterizedTypeName.get(ClassName.get(FSDocStoreSaveApi.class), ClassName.bestGuess(getResultParameter()), ClassName.bestGuess(table.getDocStoreParameterization()));
    }

    @Override
    protected boolean isBlockedFromSetterMethods(ColumnInfo column) {
        return DOC_STORE_READ_ONLY_COLUMNS.contains(column.getColumnName()) || super.isBlockedFromSetterMethods(column);
    }
}
