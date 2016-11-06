package com.fsryan.forsuredb.annotationprocessor.generator.code;

import com.fsryan.forsuredb.api.Conjunction;
import com.fsryan.forsuredb.api.OrderBy;
import com.fsryan.forsuredb.api.info.ColumnInfo;
import com.fsryan.forsuredb.api.info.TableInfo;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.util.List;

public class OrderByGenerator extends JavaSourceGenerator {

    private final List<ColumnInfo> columnsSortedByName;
    private final ClassName resolverClassName;
    private final ClassName generatedClassName;

    public OrderByGenerator(ProcessingEnvironment processingEnv, TableInfo table) {
        super(processingEnv, table.getQualifiedClassName() + "OrderBy");
        columnsSortedByName = TableDataUtil.columnsSortedByName(table);
        resolverClassName = ClassName.bestGuess(table.getQualifiedClassName() + "Resolver");
        generatedClassName = ClassName.bestGuess(table.getQualifiedClassName() + "OrderBy");
    }

    @Override
    protected String getCode() {
        TypeSpec.Builder codeBuilder = TypeSpec.classBuilder(getOutputClassName(false))
//                .addJavadoc(jd.stringToFormat(), jd.replacements())
                .addModifiers(Modifier.PUBLIC)
                .superclass(ParameterizedTypeName.get(ClassName.get(OrderBy.class), resolverClassName, generatedClassName));
        addConstructor(codeBuilder);
        addOrderByMethods(codeBuilder);
        return JavaFile.builder(getOutputPackageName(), codeBuilder.build()).indent(JAVA_INDENT).build().toString();
    }

    private void addConstructor(TypeSpec.Builder codeBuilder) {
        codeBuilder.addMethod(MethodSpec.constructorBuilder()
                .addParameter(resolverClassName, "resolver")
                .addStatement("super(resolver)")
                .build());
    }

    private void addOrderByMethods(TypeSpec.Builder codeBuilder) {
        for (ColumnInfo column : columnsSortedByName) {
            // Parent class OrderBy already contains the methods for the default columns
            if (!column.isOrderable() || TableInfo.DEFAULT_COLUMNS.containsKey(column.getColumnName())) {
                continue;
            }
            codeBuilder.addMethod(methodSpecFor(column));
        }
    }

    private MethodSpec methodSpecFor(ColumnInfo column) {
        JavadocInfo jd = javadocInfoFor(column.getColumnName());
        return MethodSpec.methodBuilder("by" + CodeUtil.snakeToCamel(column.getColumnName(), true))
                .addJavadoc(jd.stringToFormat(), jd.replacements())
                .addParameter(int.class, "order")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("appendOrder($S, $L)", column.getColumnName(), "order")
                .addStatement("return conjunction")
                .returns(ParameterizedTypeName.get(ClassName.get(Conjunction.And.class), resolverClassName, generatedClassName))
                .build();
    }

    private JavadocInfo javadocInfoFor(String columnName) {
        return JavadocInfo.builder()
                .startParagraph()
                .addLine("Order the results of the query by $L", columnName)
                .endParagraph()
                .param("order", "the direction to order the results {@link #ORDER_ASC} (or 0 or more) or {@link #ORDER_DESC} (or -1 or less)")
                .returns("a $L that allows for either adding to the orderBy or continue", JavadocInfo.inlineClassLink(Conjunction.And.class))
                .addLine("adding other query parameters")
                .addLine()
                .build();
    }
}
