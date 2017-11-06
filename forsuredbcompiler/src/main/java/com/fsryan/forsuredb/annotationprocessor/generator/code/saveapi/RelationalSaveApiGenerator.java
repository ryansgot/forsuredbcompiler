package com.fsryan.forsuredb.annotationprocessor.generator.code.saveapi;

import com.fsryan.forsuredb.api.FSSaveApi;
import com.fsryan.forsuredb.info.TableInfo;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;

import javax.annotation.processing.ProcessingEnvironment;

/*package*/ class RelationalSaveApiGenerator extends SaveApiGenerator {
    protected RelationalSaveApiGenerator(ProcessingEnvironment processingEnv, TableInfo table) {
        super(processingEnv, table);
    }

    @Override
    protected ParameterizedTypeName createSuperinterfaceParameterizedTypeName(TableInfo table) {
        return ParameterizedTypeName.get(ClassName.get(FSSaveApi.class), ClassName.bestGuess(getResultParameter()));
    }
}
