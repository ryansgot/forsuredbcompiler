package com.fsryan.forsuredb.annotationprocessor.generator.code;

import com.fsryan.forsuredb.annotationprocessor.generator.code.methodspecgenerator.FinderMethodSpecGenerator;
import com.fsryan.forsuredb.api.Conjunction;
import com.fsryan.forsuredb.api.DocStoreFinder;
import com.fsryan.forsuredb.api.Finder;
import com.fsryan.forsuredb.api.info.ColumnInfo;
import com.fsryan.forsuredb.api.info.TableInfo;
import com.fsryan.forsuredb.annotationprocessor.generator.BaseGenerator;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.util.List;

public class FinderGenerator extends JavaSourceGenerator {

    private List<ColumnInfo> columnsSortedByName;
    private final ClassName resolverClass;
    private final ClassName generatedClassName;
    private final TableInfo table;
    private final ParameterizedTypeName paramaterizedFinderType;
    private final ParameterizedTypeName parameterizedResolverType;

    public FinderGenerator(ProcessingEnvironment processingEnv, TableInfo table) {
        super(processingEnv, table.getQualifiedClassName() + "Finder");
        columnsSortedByName = TableDataUtil.columnsSortedByName(table);
        resolverClass = ClassName.bestGuess(table.getQualifiedClassName() + "Resolver");
        generatedClassName = ClassName.bestGuess(table.getQualifiedClassName() + "Finder");
        paramaterizedFinderType = ParameterizedTypeName.get(generatedClassName, TypeVariableName.get("R"));
        parameterizedResolverType = ParameterizedTypeName.get(resolverClass, TypeVariableName.get("R"));
        this.table = table;
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
                .addLine(".then()")
                .addLine(".get();")
                .endCode()
                .addLine("The above will create the following query:")
                .addLine("SELECT * FROM $L where _id >= $L AND _id <= $L AND created < [the system time]", table.getTableName(), CodeUtil.javaExampleOf("long").toString().replace("L", ""), CodeUtil.javaExampleOf("long").toString().replace("L", ""))
                .endParagraph()
                .addLine(JavadocInfo.AUTHOR_STRING)
                .addLine("@see Resolver")
                .addLine()
                .build();

        ClassName superClassName = ClassName.get(table.isDocStore() ? DocStoreFinder.class : Finder.class);
        TypeSpec.Builder codeBuilder = TypeSpec.classBuilder(getOutputClassName(false))
                .addJavadoc(jd.stringToFormat(), jd.replacements())
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(TypeVariableName.get("R", resolverClass))
                .superclass(ParameterizedTypeName.get(superClassName, TypeVariableName.get("R"), paramaterizedFinderType));

        addConstructor(codeBuilder);
        addQueryBuilderMethods(codeBuilder);
        return JavaFile.builder(getOutputPackageName(), codeBuilder.build()).indent(BaseGenerator.JAVA_INDENT).build().toString();
    }

    private void addConstructor(TypeSpec.Builder codeBuilder) {
        codeBuilder.addMethod(MethodSpec.constructorBuilder()
                .addParameter(TypeVariableName.get("R"), "resolver")
                .addStatement("super(resolver)")
                .build());
    }

    private void addQueryBuilderMethods(TypeSpec.Builder codeBuilder) {
        ParameterizedTypeName conjunctionTypeName = ParameterizedTypeName.get(ClassName.get(Conjunction.AndOr.class), TypeVariableName.get("R"), paramaterizedFinderType);
        ParameterizedTypeName betweenTypeName = ParameterizedTypeName.get(ClassName.get(Finder.Between.class), TypeVariableName.get("R"), paramaterizedFinderType);
        for (ColumnInfo column : columnsSortedByName) {
            if (!column.isSearchable() || TableInfo.defaultColumns().containsKey(column.getColumnName())) {
                continue;
            }
            for (MethodSpec methodSpec : FinderMethodSpecGenerator.create(column, conjunctionTypeName, betweenTypeName).generate()) {
                codeBuilder.addMethod(methodSpec);
            }
        }
    }
}
