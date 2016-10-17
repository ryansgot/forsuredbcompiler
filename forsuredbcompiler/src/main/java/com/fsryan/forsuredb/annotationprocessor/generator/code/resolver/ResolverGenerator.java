package com.fsryan.forsuredb.annotationprocessor.generator.code.resolver;

import com.fsryan.forsuredb.annotationprocessor.TableContext;
import com.fsryan.forsuredb.annotationprocessor.generator.code.CodeUtil;
import com.fsryan.forsuredb.annotationprocessor.generator.code.JavaSourceGenerator;
import com.fsryan.forsuredb.annotationprocessor.generator.code.JavadocInfo;
import com.fsryan.forsuredb.annotationprocessor.generator.code.TableDataUtil;
import com.fsryan.forsuredb.api.info.ColumnInfo;
import com.fsryan.forsuredb.api.info.ForeignKeyInfo;
import com.fsryan.forsuredb.api.info.TableInfo;
import com.fsryan.forsuredb.api.FSJoin;
import com.fsryan.forsuredb.api.FSProjection;
import com.fsryan.forsuredb.api.ForSureInfoFactory;
import com.fsryan.forsuredb.api.Resolver;
import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.squareup.javapoet.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class ResolverGenerator extends JavaSourceGenerator {

    private final TableInfo table;
    private final TableContext targetContext;
    private List<ColumnInfo> columnsSortedByName;
    private final ClassName[] parameterNames;
    private final ClassName getClassName;
    private final ClassName setClassName;
    private final ClassName finderClassName;
    private final ClassName orderByClassName;

    protected ResolverGenerator(ProcessingEnvironment processingEnv, TableInfo table, TableContext targetContext) {
        super(processingEnv, table.getQualifiedClassName() + "Resolver");
        this.table = table;
        this.targetContext = targetContext;
        columnsSortedByName = TableDataUtil.columnsSortedByName(table);
        getClassName = ClassName.bestGuess(table.getQualifiedClassName());
        setClassName = ClassName.bestGuess(table.getQualifiedClassName() + "Setter");
        finderClassName = ClassName.bestGuess(table.getQualifiedClassName() + "Finder");
        orderByClassName = ClassName.bestGuess(table.getQualifiedClassName() + "OrderBy");
        parameterNames = createParameterNames(table).toArray(new ClassName[0]);
    }

    /**
     * <p>
     *     If you override this method, then you must call the super class method.
     * </p>
     * @param table the table information for which the Finder class extension should be generated
     * @return a List of ClassName describing the type parameters of the Resolver class extension
     */
    protected List<ClassName> createParameterNames(TableInfo table) {
        List<ClassName> ret = new LinkedList<>();
        ret.add(ClassName.bestGuess(getResultParameter()));
        ret.add(ClassName.get(getRecordContainerClass()));
        ret.add(getClassName);
        ret.add(setClassName);
        ret.add(finderClassName);
        ret.add(orderByClassName);
        return ret;
    }

    public static ResolverGenerator getFor(ProcessingEnvironment processingEnv, TableInfo table, TableContext targetContext) {
        return table.isDocStore() ? new DocStoreResolverGenerator(processingEnv, table, targetContext)
                : new RelationalResolverGenerator(processingEnv, table, targetContext);
    }

    @Override
    protected String getCode() {
        JavadocInfo jd = classJavadoc();
        TypeSpec.Builder codeBuilder = TypeSpec.classBuilder(getOutputClassName(false))
                .addJavadoc(jd.stringToFormat(), jd.replacements())
                .addModifiers(Modifier.PUBLIC)
                .superclass(ParameterizedTypeName.get(extendsFromClassName(), parameterNames));
        addFields(codeBuilder);
        addConstructor(codeBuilder);
        addColumnMethodNameMapMethod(codeBuilder);
        addJoinMethods(codeBuilder);
        addAbstractMethodImplementations(codeBuilder);
        return JavaFile.builder(getOutputPackageName(), codeBuilder.build()).indent(JAVA_INDENT).build().toString();
    }

    protected abstract ClassName extendsFromClassName();

    private void addColumnMethodNameMapMethod(TypeSpec.Builder codeBuilder) {
        codeBuilder.addMethod(MethodSpec.methodBuilder("columnNameToMethodNameBiMap")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(BiMap.class, String.class, String.class))
                .addStatement("return $L", "COLUMN_TO_METHOD_NAME_BI_MAP")
                .build());
    }

    private JavadocInfo classJavadoc() {
        return JavadocInfo.builder()
                .startParagraph()
                .addLine("This is an auto-generated class. DO NOT modify it!")
                .endParagraph()
                .startParagraph()
                .addLine("Entry point for querying the $L table. You can access", table.getTableName())
                .addLine("this $L via the generated static", JavadocInfo.inlineClassLink(Resolver.class))
                .addLine("method in the ForSure class:")
                .startCode()
                .addLine("ForSure.$L().find()", CodeUtil.snakeToCamel(table.getTableName()))
                .addLine(".byIdLessThan($L)", CodeUtil.javaExampleOf("long"))
                .addLine(".then()")
                .addLine(".get();")
                .endCode()
                .endParagraph()
                .addLine(JavadocInfo.AUTHOR_STRING)
                .addLine("@see Resolver")
                .addLine()
                .build();
    }

    private void addFields(TypeSpec.Builder codeBuilder) {
        codeBuilder.addField(FieldSpec.builder(String.class, "TABLE_NAME")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("$S", table.getTableName())
                        .build());
        String[] columnNames = orderedColumnNames();
        codeBuilder.addField(FieldSpec.builder(String[].class, "columns")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("{" + Strings.repeat("$S,", columnNames.length) + "}", columnNames)
                        .build())
                .addField(FieldSpec.builder(FSProjection.class, "PROJECTION")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer(CodeBlock.builder()
                                .add("$L", anonymousFSProjection())
                                .build())
                        .build());
        codeBuilder.addField(columnNameToMethodNameMapField());
    }

    private String[] orderedColumnNames() {
        List<String> retList = new ArrayList<>();
        for (ColumnInfo column : columnsSortedByName) {
            retList.add(column.getColumnName());
        }
        return retList.toArray(new String[retList.size()]);
    }

    private TypeSpec anonymousFSProjection() {
        return TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(FSProjection.class)
                .addMethod(MethodSpec.methodBuilder("tableName")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(String.class)
                        .addStatement("return TABLE_NAME")
                        .build())
                .addMethod(MethodSpec.methodBuilder("columns")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(String[].class)
                        .addStatement("return columns")
                        .build())
                .build();
    }

    private FieldSpec columnNameToMethodNameMapField() {
        CodeBlock.Builder mapBlockBuilder = CodeBlock.builder()
                .add("new $T()", ParameterizedTypeName.get(ImmutableBiMap.Builder.class, String.class, String.class));
        for (ColumnInfo column : columnsSortedByName) {
            mapBlockBuilder.add("$L($S, $S)", "\n        .put", column.getColumnName(), column.getMethodName());
        }
        mapBlockBuilder.add("\n        .build()");
        return FieldSpec.builder(ParameterizedTypeName.get(ImmutableBiMap.class, String.class, String.class), "COLUMN_TO_METHOD_NAME_BI_MAP", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer(mapBlockBuilder.build())
                .build();
    }

    private void addConstructor(TypeSpec.Builder codeBuilder) {
        codeBuilder.addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ForSureInfoFactory.class, "infoFactory")
                        .addStatement("super(infoFactory)")
                        .build());
    }

    private void addJoinMethods(TypeSpec.Builder codeBuilder) {
        // Add join methods where this table is the child in the join relationship
        for (ColumnInfo column : columnsSortedByName) {
            if (TableInfo.DEFAULT_COLUMNS.containsKey(column.getColumnName()) || !column.isForeignKey()) {
                continue;
            }
            JavadocInfo jd = javadocFor(column.getForeignKeyInfo().getTableName(),
                    column.getForeignKeyInfo().getColumnName(),
                    table.getTableName(),
                    column.getColumnName());
            codeBuilder.addMethod(createMethodSpecForJoin(column.getForeignKeyInfo().getApiClassName(),
                            createChildTableJoinSpec(column),
                            jd));
        }

        // add join methods where this table is the parent in the join relationship
        for (TableInfo targetTable : TableDataUtil.tablesSortedByName(targetContext, table)) {
            for (ColumnInfo column : TableDataUtil.columnsSortedByName(targetTable.getColumns())) {
                if (TableInfo.DEFAULT_COLUMNS.containsKey(column.getColumnName())
                        || !column.isForeignKey()
                        || !table.getTableName().equals(column.getForeignKeyInfo().getTableName())) {
                    continue;
                }
                final TypeSpec joinSpec = createParentTableJoinSpec(column, targetTable.getTableName());
                JavadocInfo jd = javadocFor(table.getTableName(),
                                column.getForeignKeyInfo().getColumnName(),
                                targetTable.getTableName(),
                                column.getColumnName());
                codeBuilder.addMethod(createMethodSpecForJoin(targetTable.getQualifiedClassName(), joinSpec, jd));
            }
        }
    }

    private JavadocInfo javadocFor(String parentTable, String parentColumn, String childTable, String childColumn) {
        return JavadocInfo.builder()
                .startParagraph()
                .addLine("Add a join to $L on $L.$L = $L.$L",
                        table.getTableName().equals(parentTable) ? childTable : parentTable,
                        childTable,
                        childColumn,
                        parentTable,
                        parentColumn)
                .addLine("to the query")
                .endParagraph()
                .addLine()
                .build();
    }

    private TypeSpec createChildTableJoinSpec(ColumnInfo column) {
        final ForeignKeyInfo fki = column.getForeignKeyInfo();
        return createJoinSpec(fki.getTableName(), fki.getColumnName(), table.getTableName(), column.getColumnName());
    }

    private TypeSpec createParentTableJoinSpec(ColumnInfo column, String childTableName) {
        final ForeignKeyInfo fki = column.getForeignKeyInfo();
        return createJoinSpec(table.getTableName(), fki.getColumnName(), childTableName, column.getColumnName());
    }

    private TypeSpec createJoinSpec(String parentTableName, String parentColumnName, String childTableName, String childColumnName) {
        return TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(FSJoin.class)
                .addMethod(MethodSpec.methodBuilder("type")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(FSJoin.Type.class)
                        .addStatement("return type")
                        .build())
                .addMethod(MethodSpec.methodBuilder("parentTable")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(String.class)
                        .addStatement("return $S", parentTableName)
                        .build())
                .addMethod(MethodSpec.methodBuilder("parentColumn")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(String.class)
                        .addStatement("return $S", parentColumnName)
                        .build())
                .addMethod(MethodSpec.methodBuilder("childTable")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(String.class)
                        .addStatement("return $S", childTableName)
                        .build())
                .addMethod(MethodSpec.methodBuilder("childColumn")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(String.class)
                        .addStatement("return $S", childColumnName)
                        .build())
                .build();
    }

    private MethodSpec createMethodSpecForJoin(String joinedTableApiClass, TypeSpec childTableJoinSpec, JavadocInfo jd) {
        final String apiClassSimpleName = CodeUtil.simpleClassNameFrom(joinedTableApiClass);
        return MethodSpec.methodBuilder("join" + apiClassSimpleName)
                .addJavadoc(jd.stringToFormat(), jd.replacements())
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.bestGuess(getOutputClassName(false)))
                .addParameter(FSJoin.Type.class, "type", Modifier.FINAL)
                .addStatement("addJoin($L, $L.PROJECTION)", childTableJoinSpec, apiClassSimpleName + "Resolver")
                .addStatement("return this")
                .build();
    }

    private void addAbstractMethodImplementations(TypeSpec.Builder codeBuilder) {
        codeBuilder.addMethod(MethodSpec.methodBuilder("getApiClass")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ParameterizedTypeName.get(ClassName.get(Class.class), getClassName))
                        .addStatement("return $L.class", CodeUtil.simpleClassNameFrom(table.getQualifiedClassName()))
                        .build())
                .addMethod(MethodSpec.methodBuilder("setApiClass")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ParameterizedTypeName.get(ClassName.get(Class.class), setClassName))
                        .addStatement("return $L.class", CodeUtil.simpleClassNameFrom(table.getQualifiedClassName() + "Setter"))
                        .build())
                .addMethod(MethodSpec.methodBuilder("projection")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(FSProjection.class)
                        .addStatement("return PROJECTION")
                        .build())
                .addMethod(MethodSpec.methodBuilder("newFinderInstance")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PROTECTED)
                        .returns(finderClassName)
                        .addStatement("return new $T(this)", finderClassName)
                        .build())
                .addMethod(MethodSpec.methodBuilder("newOrderByInstance")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PROTECTED)
                        .returns(orderByClassName)
                        .addStatement("return new $T(this)", orderByClassName)
                        .build())
                .addMethod(MethodSpec.methodBuilder("tableName")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(String.class)
                        .addStatement("return TABLE_NAME")
                        .build());
    }
}
