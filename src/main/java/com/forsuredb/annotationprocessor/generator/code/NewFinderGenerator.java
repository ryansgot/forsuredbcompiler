package com.forsuredb.annotationprocessor.generator.code;

import com.forsuredb.annotationprocessor.generator.code.methodspecgenerator.FinderMethodSpecGenerator;
import com.forsuredb.annotationprocessor.info.ColumnInfo;
import com.forsuredb.annotationprocessor.info.TableInfo;
import com.forsuredb.api.Finder;
import com.forsuredb.api.Resolver;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.util.List;

public class NewFinderGenerator extends JavaSourceGenerator {

    private List<ColumnInfo> columnsSortedByName;
    private final ClassName[] parameterClasses;
    private final TableInfo table;

    public NewFinderGenerator(ProcessingEnvironment processingEnv, TableInfo table) {
        super(processingEnv, table.getQualifiedClassName() + "Finder");
        this.table = table;
        columnsSortedByName = TableDataUtil.columnsSortedByName(table);
        parameterClasses = new ClassName[] {
                ClassName.get(getResultParameter()),                            // U (the resultParameter)
                ClassName.get(getRecordContainer()),                            // R extends RecordContainer
                ClassName.bestGuess(table.getQualifiedClassName()),             // G extends FSGetApi
                ClassName.bestGuess(table.getQualifiedClassName() + "Setter"),  // S extends FSSaveApi<U>
                ClassName.bestGuess(getOutputClassName(true))                   // F extends Finder<U, G, S, F>
        };
    }

    @Override
    protected String getCode() {
        JavadocInfo jd = JavadocInfo.builder()
                .startParagraph()
                .addLine("This is an auto-generated class. DO NOT modify it!")
                .endParagraph()
                .startParagraph()
                .addLine("Provides methods for creating a query that will query the $L", table.getTableName())
                .addLine("table. These methods can be chained to produce just about any query you")
                .addLine("may want, for example:")
                .startCode()
                .addLine("$L().find()", CodeUtil.snakeToCamel(table.getTableName()))
                .addLine(".byIdBetweenInclusive($L)", CodeUtil.javaExampleOf("long"))
                .addLine(".andInclusive($L)", CodeUtil.javaExampleOf("long"))
                .addLine(".byCreatedBefore($L)", CodeUtil.javaExampleOf("java.util.Date"))
                .addLine(".andFinally()")
                .addLine(".get();")
                .endCode()
                .addLine("The above will create the following query:")
                .addLine("SELECT * FROM $L where _id >= $L AND _id <= $L AND created < [the system time]", table.getTableName(), CodeUtil.javaExampleOf("long").toString().replace("L", ""), CodeUtil.javaExampleOf("long").toString().replace("L", ""))
                .endParagraph()
                .addLine(JavadocInfo.AUTHOR_STRING)
                .addLine("@see Resolver")
                .addLine()
                .build();
        TypeSpec.Builder codeBuilder = TypeSpec.classBuilder(getOutputClassName(false))
                .addJavadoc(jd.stringToFormat(), jd.replacements())
                .addModifiers(Modifier.PUBLIC)
                .superclass(ParameterizedTypeName.get(ClassName.get(Finder.class), parameterClasses));
        addConstructor(codeBuilder);
        addQueryBuilderMethods(codeBuilder);
        return JavaFile.builder(getOutputPackageName(), codeBuilder.build()).indent(JAVA_INDENT).build().toString();
    }

    private void addConstructor(TypeSpec.Builder codeBuilder) {
        codeBuilder.addMethod(MethodSpec.constructorBuilder()
                .addParameter(ParameterizedTypeName.get(ClassName.get(Resolver.class), parameterClasses), "resolver")
                .addStatement("super(resolver)")
                .build());
    }

    private void addQueryBuilderMethods(TypeSpec.Builder codeBuilder) {
        for (ColumnInfo column : columnsSortedByName) {
            for (MethodSpec methodSpec : FinderMethodSpecGenerator.create(column).generate(parameterClasses)) {
                codeBuilder.addMethod(methodSpec);
            }
        }
    }
}
