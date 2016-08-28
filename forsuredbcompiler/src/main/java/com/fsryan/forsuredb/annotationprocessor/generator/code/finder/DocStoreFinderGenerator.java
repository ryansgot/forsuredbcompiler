package com.fsryan.forsuredb.annotationprocessor.generator.code.finder;

import com.fsryan.forsuredb.api.DocStoreFinder;
import com.fsryan.forsuredb.api.DocStoreResolver;
import com.fsryan.forsuredb.api.info.TableInfo;
import com.squareup.javapoet.ClassName;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.List;

/*packae*/ class DocStoreFinderGenerator extends FinderGenerator {
    public DocStoreFinderGenerator(ProcessingEnvironment processingEnv, TableInfo table) {
        super(processingEnv, table);
    }

    @Override
    protected ClassName extendsFromClassName() {
        return ClassName.get(DocStoreFinder.class);
    }

    @Override
    protected ClassName resolverClassName() {
        return ClassName.get(DocStoreResolver.class);
    }

    @Override
    protected Class<?> conjunctionClass() {
        return DocStoreFinder.Conjunction.class;
    }

    @Override
    protected Class<?> betweenClass() {
        return DocStoreFinder.Between.class;
    }

    @Override
    protected List<ClassName> createParameterClasses(TableInfo table) {
        List<ClassName> ret = super.createParameterClasses(table);
        ret.add(0, ClassName.bestGuess(table.getDocStoreParameterization()));
        return ret;
    }
}
