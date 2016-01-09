package com.forsuredb.annotationprocessor.generator.code;

import com.forsuredb.annotationprocessor.generator.NewBaseGenerator;
import com.forsuredb.annotationprocessor.info.TableInfo;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.Collection;

public class NewTableCreatorGenerator extends NewBaseGenerator<JavaFileObject> {

    private static final String CLASS_NAME = "TableGenerator";
    private static final String METHOD_NAME = "generate";
    private static final String LIST_VARIABLE_NAME = "retList";

    private final String appPackageName;
    private final Collection<TableInfo> tables;

    public NewTableCreatorGenerator(ProcessingEnvironment processingEnv, String appPackageName, Collection<TableInfo> tables) {
        super(processingEnv);
        this.appPackageName =appPackageName;
        this.tables = tables;
    }

    @Override
    protected JavaFileObject createFileObject(ProcessingEnvironment processingEnv) throws IOException {
        return processingEnv.getFiler().createSourceFile(getOutputClassName(true));
    }

    @Override
    protected String getCode() {
        return null;
    }

    private String getOutputClassName(boolean fullyQualified) {
        return fullyQualified ? appPackageName + "." + CLASS_NAME : CLASS_NAME;
    }
}
