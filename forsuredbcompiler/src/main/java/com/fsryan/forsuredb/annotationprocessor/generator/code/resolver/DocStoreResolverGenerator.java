package com.fsryan.forsuredb.annotationprocessor.generator.code.resolver;

import com.fsryan.forsuredb.annotationprocessor.TableContext;
import com.fsryan.forsuredb.api.DocStoreResolver;
import com.fsryan.forsuredb.api.info.TableInfo;
import com.squareup.javapoet.ClassName;

import javax.annotation.processing.ProcessingEnvironment;

/*package*/ class DocStoreResolverGenerator extends ResolverGenerator {
    public DocStoreResolverGenerator(ProcessingEnvironment processingEnv, TableInfo table, TableContext targetContext) {
        super(processingEnv, table, targetContext);
    }

    @Override
    protected ClassName extendsFromClassName() {
        return ClassName.get(DocStoreResolver.class);
    }
}
