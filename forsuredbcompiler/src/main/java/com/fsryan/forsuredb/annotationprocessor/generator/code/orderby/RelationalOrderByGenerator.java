package com.fsryan.forsuredb.annotationprocessor.generator.code.orderby;

import com.fsryan.forsuredb.api.info.TableInfo;

import javax.annotation.processing.ProcessingEnvironment;

/*package*/ class RelationalOrderByGenerator extends OrderByGenerator {
    protected RelationalOrderByGenerator(ProcessingEnvironment processingEnv, TableInfo table) {
        super(processingEnv, table);
    }
}
