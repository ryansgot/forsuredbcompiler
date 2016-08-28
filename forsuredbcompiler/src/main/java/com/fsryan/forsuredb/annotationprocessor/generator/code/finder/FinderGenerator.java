package com.fsryan.forsuredb.annotationprocessor.generator.code.finder;

import com.fsryan.forsuredb.annotationprocessor.generator.code.CodeUtil;
import com.fsryan.forsuredb.annotationprocessor.generator.code.JavaSourceGenerator;
import com.fsryan.forsuredb.annotationprocessor.generator.code.JavadocInfo;
import com.fsryan.forsuredb.annotationprocessor.generator.code.TableDataUtil;
import com.fsryan.forsuredb.annotationprocessor.generator.code.methodspecgenerator.FinderMethodSpecGenerator;
import com.fsryan.forsuredb.api.info.ColumnInfo;
import com.fsryan.forsuredb.api.info.TableInfo;
import com.fsryan.forsuredb.annotationprocessor.generator.BaseGenerator;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.util.LinkedList;
import java.util.List;

public abstract class FinderGenerator extends JavaSourceGenerator {

    private List<ColumnInfo> columnsSortedByName;
    private final ClassName[] parameterClasses;
    private final TableInfo table;

    protected FinderGenerator(ProcessingEnvironment processingEnv, TableInfo table) {
        super(processingEnv, table.getQualifiedClassName() + "Finder");
        this.table = table;
        columnsSortedByName = TableDataUtil.columnsSortedByName(table);
        parameterClasses = createParameterClasses(table).toArray(new ClassName[0]);
    }

    public static FinderGenerator getFor(ProcessingEnvironment processingEnv, TableInfo table) {
        return table.isDocStore() ? new DocStoreFinderGenerator(processingEnv, table) : new RelationalFinderGenerator(processingEnv, table);
    }

    protected abstract ClassName extendsFromClassName();
    protected abstract ClassName resolverClassName();
    protected abstract Class<?> conjunctionClass();
    protected abstract Class<?> betweenClass();

    /**
     * <p>
     *     If you override this method, then you must call the super class method.
     * </p>
     * @param table the table information for which the Finder class extension should be generated
     * @return a List of ClassName describing the type parameters of the Finder class extension
     */
    protected List<ClassName> createParameterClasses(TableInfo table) {
        List<ClassName> ret = new LinkedList<>();
        ret.add(ClassName.bestGuess(getResultParameter()));                         // U (the resultParameter)
        ret.add(ClassName.get(getRecordContainerClass()));                          // R extends RecordContainer
        ret.add(ClassName.bestGuess(table.getQualifiedClassName()));                // G extends FSGetApi
        ret.add(ClassName.bestGuess(table.getQualifiedClassName() + "Setter"));     // S extends FSSaveApi<U>
        ret.add(ClassName.bestGuess(getOutputClassName(true)));                     // F extends Finder<U, R, G, S, F, O>
        ret.add(ClassName.bestGuess(table.getQualifiedClassName() + "OrderBy"));    // O extends OrderBy<U, R, G, S, F, O>
        return ret;
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
                .superclass(ParameterizedTypeName.get(extendsFromClassName(), parameterClasses));
        addConstructor(codeBuilder);
        addQueryBuilderMethods(codeBuilder);
        return JavaFile.builder(getOutputPackageName(), codeBuilder.build()).indent(BaseGenerator.JAVA_INDENT).build().toString();
    }

    private void addConstructor(TypeSpec.Builder codeBuilder) {
        codeBuilder.addMethod(MethodSpec.constructorBuilder()
                .addParameter(ParameterizedTypeName.get(resolverClassName(), parameterClasses), "resolver")
                .addStatement("super(resolver)")
                .build());
    }

    private void addQueryBuilderMethods(TypeSpec.Builder codeBuilder) {
        for (ColumnInfo column : columnsSortedByName) {
            if (!column.isSearchable() || TableInfo.DEFAULT_COLUMNS.containsKey(column.getColumnName())) {
                continue;
            }
            for (MethodSpec methodSpec : FinderMethodSpecGenerator.create(column, conjunctionClass(), betweenClass()).generate(parameterClasses)) {
                codeBuilder.addMethod(methodSpec);
            }
        }
    }
}
