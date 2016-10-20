package com.fsryan.forsuredb.annotationprocessor.generator.code.orderby;

import com.fsryan.forsuredb.annotationprocessor.generator.code.CodeUtil;
import com.fsryan.forsuredb.annotationprocessor.generator.code.JavaSourceGenerator;
import com.fsryan.forsuredb.annotationprocessor.generator.code.JavadocInfo;
import com.fsryan.forsuredb.annotationprocessor.generator.code.TableDataUtil;
import com.fsryan.forsuredb.api.info.ColumnInfo;
import com.fsryan.forsuredb.api.info.TableInfo;
import com.fsryan.forsuredb.api.OrderBy;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.util.LinkedList;
import java.util.List;

public abstract class OrderByGenerator extends JavaSourceGenerator {

    private final List<ColumnInfo> columnsSortedByName;
    private final ClassName[] parameterClasses;

    protected OrderByGenerator(ProcessingEnvironment processingEnv, TableInfo table) {
        super(processingEnv, table.getQualifiedClassName() + "OrderBy");
        columnsSortedByName = TableDataUtil.columnsSortedByName(table);
        parameterClasses = createParameterClasses(table).toArray(new ClassName[0]);
    }

    public static OrderByGenerator getFor(ProcessingEnvironment processingEnv, TableInfo table) {
        return table.isDocStore() ? new DocStoreOrderByGenerator(processingEnv, table) : new RelationalOrderByGenerator(processingEnv, table);
    }

    protected abstract ClassName extendsFromClassName();
    protected abstract ClassName resolverClassName();
    protected abstract Class<?> conjunctionClass();

    /**
     * <p>
     *     If you override this method, then you must call the super class method.
     * </p>
     * @param table the table information for which the Finder class extension should be generated
     * @return a List of ClassName describing the type parameters of the OrderBy class extension
     */
    protected List<ClassName> createParameterClasses(TableInfo table) {
        List<ClassName> ret = new LinkedList<>();
        ret.add(ClassName.bestGuess(getResultParameter()));                         // U (the resultParameter)
        ret.add(ClassName.get(getRecordContainerClass()));                          // R extends RecordContainer
        ret.add(ClassName.bestGuess(table.getQualifiedClassName()));                // G extends FSGetApi
        ret.add(ClassName.bestGuess(table.getQualifiedClassName() + "Setter"));     // S extends FSSaveApi<U>
        ret.add(ClassName.bestGuess(table.getQualifiedClassName() + "Finder"));     // F extends Finder<U, R, G, S, F, O>
        ret.add(ClassName.bestGuess(getOutputClassName(true)));                     // O extends OrderBy<U, R, G, S, F, O>
        return ret;
    }

    @Override
    protected String getCode() {
        TypeSpec.Builder codeBuilder = TypeSpec.classBuilder(getOutputClassName(false))
//                .addJavadoc(jd.stringToFormat(), jd.replacements())
                .addModifiers(Modifier.PUBLIC)
                .superclass(ParameterizedTypeName.get(extendsFromClassName(), parameterClasses));
        addConstructor(codeBuilder);
        addOrderByMethods(codeBuilder);
        return JavaFile.builder(getOutputPackageName(), codeBuilder.build()).indent(JAVA_INDENT).build().toString();
    }

    private void addConstructor(TypeSpec.Builder codeBuilder) {
        codeBuilder.addMethod(MethodSpec.constructorBuilder()
                .addParameter(ParameterizedTypeName.get(resolverClassName(), parameterClasses), "resolver")
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
                .returns(ParameterizedTypeName.get(ClassName.get(conjunctionClass()), parameterClasses))
                .build();
    }

    private JavadocInfo javadocInfoFor(String columnName) {
        return JavadocInfo.builder()
                .startParagraph()
                .addLine("Order the results of the query by $L", columnName)
                .endParagraph()
                .param("order", "the direction to order the results {@link #ORDER_ASC} (or 0 or more) or {@link #ORDER_DESC} (or -1 or less)")
                .returns("a $L that allows for either adding to the orderBy or continue", JavadocInfo.inlineClassLink(conjunctionClass()))
                .addLine("adding other query parameters")
                .addLine()
                .build();
    }
}
