package com.fsryan.forsuredb.annotationprocessor.generator.code.finder;

import com.fsryan.forsuredb.api.RelationalFinder;
import com.fsryan.forsuredb.api.RelationalResolver;
import com.fsryan.forsuredb.api.info.TableInfo;
import com.squareup.javapoet.ClassName;

import javax.annotation.processing.ProcessingEnvironment;

/*package*/ class RelationalFinderGenerator extends FinderGenerator {
    protected RelationalFinderGenerator(ProcessingEnvironment processingEnv, TableInfo table) {
        super(processingEnv, table);
    }

    @Override
    protected ClassName extendsFromClassName() {
        return ClassName.get(RelationalFinder.class);
    }

    @Override
    protected ClassName resolverClassName() {
        return ClassName.get(RelationalResolver.class);
    }

    @Override
    protected Class<?> conjunctionClass() {
        return RelationalFinder.Conjunction.class;
    }

    @Override
    protected Class<?> betweenClass() {
        return RelationalFinder.Between.class;
    }
}
