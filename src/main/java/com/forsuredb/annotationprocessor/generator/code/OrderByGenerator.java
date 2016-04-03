package com.forsuredb.annotationprocessor.generator.code;

import com.forsuredb.annotationprocessor.info.ColumnInfo;
import com.forsuredb.annotationprocessor.info.TableInfo;
import com.forsuredb.api.Finder;
import com.forsuredb.api.OrderBy;
import com.forsuredb.api.Resolver;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.util.List;

public class OrderByGenerator extends JavaSourceGenerator {

    private final List<ColumnInfo> columnsSortedByName;
    private final ClassName[] parameterClasses;

    public OrderByGenerator(ProcessingEnvironment processingEnv, TableInfo table) {
        super(processingEnv, table.getQualifiedClassName() + "OrderBy");
        columnsSortedByName = TableDataUtil.columnsSortedByName(table);
        parameterClasses = new ClassName[] {
                ClassName.bestGuess(getResultParameter()),                      // U (the resultParameter)
                ClassName.get(getRecordContainerClass()),                       // R extends RecordContainer
                ClassName.bestGuess(table.getQualifiedClassName()),             // G extends FSGetApi
                ClassName.bestGuess(table.getQualifiedClassName() + "Setter"),  // S extends FSSaveApi<U>
                ClassName.bestGuess(table.getQualifiedClassName() + "Finder"),  // F extends Finder<U, R, G, S, F, O>
                ClassName.bestGuess(table.getQualifiedClassName() + "OrderBy")  // O extends OrderBy<U, R, G, S, F, O>
        };
    }

    @Override
    protected String getCode() {
        TypeSpec.Builder codeBuilder = TypeSpec.classBuilder(getOutputClassName(false))
//                .addJavadoc(jd.stringToFormat(), jd.replacements())
                .addModifiers(Modifier.PUBLIC)
                .superclass(ParameterizedTypeName.get(ClassName.get(OrderBy.class), parameterClasses));
        addConstructor(codeBuilder);
        addOrderByMethods(codeBuilder);
        return JavaFile.builder(getOutputPackageName(), codeBuilder.build()).indent(JAVA_INDENT).build().toString();
    }

    private void addConstructor(TypeSpec.Builder codeBuilder) {
        codeBuilder.addMethod(MethodSpec.constructorBuilder()
                .addParameter(ParameterizedTypeName.get(ClassName.get(Resolver.class), parameterClasses), "resolver")
                .addStatement("super(resolver)")
                .build());
    }

    private void addOrderByMethods(TypeSpec.Builder codeBuilder) {
        for (ColumnInfo column : columnsSortedByName) {
            // Parent class OrderBy already contains the methods for the default columns
            if (TableInfo.DEFAULT_COLUMNS.containsKey(column.getColumnName())) {
                continue;
            }
            codeBuilder.addMethod(methodSpecFor(column));
        }
    }

    private MethodSpec methodSpecFor(ColumnInfo column) {
        return MethodSpec.methodBuilder("by" + CodeUtil.snakeToCamel(column.getColumnName(), true))
//                .addJavadoc(jd.stringToFormat(), jd.replacements())
                .addParameter(TypeName.get(OrderBy.Order.class), "order")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("appendOrder($S, $L)", column.getColumnName(), "order")
                .addStatement("return conjunction")
                .returns(ParameterizedTypeName.get(ClassName.get(OrderBy.Conjunction.class), parameterClasses))
                .build();
    }
}
