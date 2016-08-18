package com.fsryan.forsuredb.annotationprocessor.generator.code.finder;

import com.fsryan.forsuredb.api.info.TableInfo;

import javax.annotation.processing.ProcessingEnvironment;

/*package*/ class RelationalFinderGenerator extends FinderGenerator {
    protected RelationalFinderGenerator(ProcessingEnvironment processingEnv, TableInfo table) {
        super(processingEnv, table);
    }
}
