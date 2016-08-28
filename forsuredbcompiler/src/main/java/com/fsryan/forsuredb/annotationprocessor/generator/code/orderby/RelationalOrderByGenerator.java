package com.fsryan.forsuredb.annotationprocessor.generator.code.orderby;

import com.fsryan.forsuredb.api.RelationalOrderBy;
import com.fsryan.forsuredb.api.RelationalResolver;
import com.fsryan.forsuredb.api.info.TableInfo;
import com.squareup.javapoet.ClassName;

import javax.annotation.processing.ProcessingEnvironment;

/*package*/ class RelationalOrderByGenerator extends OrderByGenerator {
    protected RelationalOrderByGenerator(ProcessingEnvironment processingEnv, TableInfo table) {
        super(processingEnv, table);
    }

    @Override
    protected ClassName extendsFromClassName() {
        return ClassName.get(RelationalOrderBy.class);
    }

    @Override
    protected ClassName resolverClassName() {
        return ClassName.get(RelationalResolver.class);
    }

    @Override
    protected Class<?> conjunctionClass() {
        return RelationalOrderBy.Conjunction.class;
    }
}
