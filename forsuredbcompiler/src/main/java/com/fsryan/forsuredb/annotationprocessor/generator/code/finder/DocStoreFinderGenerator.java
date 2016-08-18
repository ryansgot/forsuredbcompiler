package com.fsryan.forsuredb.annotationprocessor.generator.code.finder;

import com.fsryan.forsuredb.api.info.TableInfo;
import com.squareup.javapoet.ClassName;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.List;

/*packae*/ class DocStoreFinderGenerator extends FinderGenerator {
    public DocStoreFinderGenerator(ProcessingEnvironment processingEnv, TableInfo table) {
        super(processingEnv, table);
    }

    @Override
    protected List<ClassName> createParameterClasses(TableInfo table) {
        List<ClassName> ret = super.createParameterClasses(table);
        ret.add(0, ClassName.bestGuess(table.getDocStoreParameterization().toString()));
        return ret;
    }
}
