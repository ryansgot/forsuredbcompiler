package com.fsryan.forsuredb.annotationprocessor.generator.code.orderby;

import com.fsryan.forsuredb.api.info.TableInfo;
import com.squareup.javapoet.ClassName;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.List;

/*package*/ class DocStoreOrderByGenerator extends OrderByGenerator {
    protected DocStoreOrderByGenerator(ProcessingEnvironment processingEnv, TableInfo table) {
        super(processingEnv, table);
    }

    @Override
    protected List<ClassName> createParameterClasses(TableInfo table) {
        List<ClassName> ret = super.createParameterClasses(table);
        ret.add(0, ClassName.bestGuess(table.getDocStoreParameterization().toString()));
        return ret;
    }
}
