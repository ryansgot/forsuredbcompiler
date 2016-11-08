package com.fsryan.forsuredb.annotationprocessor.generator.code.resolver;

import com.fsryan.forsuredb.annotationprocessor.TableContext;
import com.fsryan.forsuredb.api.DocStoreResolver;
import com.fsryan.forsuredb.api.info.TableInfo;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.List;

/*package*/ class DocStoreResolverGenerator extends ResolverGenerator {
    public DocStoreResolverGenerator(ProcessingEnvironment processingEnv, TableInfo table, TableContext targetContext) {
        super(processingEnv, table, targetContext);
    }

    @Override
    protected ClassName extendsFromClassName() {
        return ClassName.get(DocStoreResolver.class);
    }

    @Override
    protected List<TypeName> createParameterNames(TableInfo table) {
        List<TypeName> ret = super.createParameterNames(table);
        ret.add(0, ClassName.bestGuess(table.getDocStoreParameterization()));
        return ret;
    }
}
