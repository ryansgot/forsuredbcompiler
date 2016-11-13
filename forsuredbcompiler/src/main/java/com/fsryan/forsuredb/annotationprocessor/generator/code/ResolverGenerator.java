package com.fsryan.forsuredb.annotationprocessor.generator.code;

import com.fsryan.forsuredb.annotationprocessor.TableContext;
import com.fsryan.forsuredb.annotationprocessor.util.Pair;
import com.fsryan.forsuredb.api.DocStoreResolver;
import com.fsryan.forsuredb.api.Retriever;
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

public class ResolverGenerator extends JavaSourceGenerator {

    private final TableInfo table;
    private final TableContext targetContext;
    private final List<Pair<TableInfo, ColumnInfo>> parentJoins;
    private List<ColumnInfo> columnsSortedByName;
    private final TypeName[] parameterNames;
    private final TypeName generatedClassName;
    private final TypeName getClassName;
    private final TypeName setClassName;
    private final TypeName finderClassName;
    private final TypeName orderByClassName;

    public ResolverGenerator(ProcessingEnvironment processingEnv, TableInfo table, TableContext targetContext) {
        super(processingEnv, table.getQualifiedClassName() + "Resolver");
        this.table = table;
        this.targetContext = targetContext;
        parentJoins = createParentJoins(table, targetContext);
        columnsSortedByName = TableDataUtil.columnsSortedByName(table);
        generatedClassName = ClassName.bestGuess(table.getQualifiedClassName() + "Resolver");
        getClassName = ClassName.bestGuess(table.getQualifiedClassName());
        setClassName = ClassName.bestGuess(table.getQualifiedClassName() + "Setter");
        finderClassName = ClassName.bestGuess(table.getQualifiedClassName() + "Finder");
        orderByClassName = ClassName.bestGuess(table.getQualifiedClassName() + "OrderBy");
        parameterNames = createParameterNames(table).toArray(new TypeName[0]);
    }

    @Override
    protected String getCode() {
        JavadocInfo jd = classJavadoc();
        ClassName superClassName = ClassName.get(table.isDocStore() ? DocStoreResolver.class : Resolver.class);
        TypeSpec.Builder codeBuilder = TypeSpec.classBuilder(getOutputClassName(false))
                .addJavadoc(jd.stringToFormat(), jd.replacements())
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addTypeVariable(TypeVariableName.get("T", generatedClassName))
                .superclass(ParameterizedTypeName.get(superClassName, parameterNames));
        codeBuilder.addType(TypeSpec.classBuilder("Base")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .superclass(ParameterizedTypeName.get((ClassName) generatedClassName, ClassName.bestGuess("Base")))
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ForSureInfoFactory.class, "infoFactory")
                        .addStatement("super(infoFactory)")
                        .build())
                .build());
        addJoinResolverClasses(codeBuilder);
        addFields(codeBuilder);
        addConstructor(codeBuilder);
        addColumnMethodNameMapMethod(codeBuilder);
        addJoinMethods(codeBuilder);
        addAbstractMethodImplementations(codeBuilder);
        return JavaFile.builder(getOutputPackageName(), codeBuilder.build()).indent(JAVA_INDENT).build().toString();
    }

    private static List<Pair<TableInfo, ColumnInfo>> createParentJoins(TableInfo table, TableContext targetContext) {
        List<Pair<TableInfo, ColumnInfo>> ret = new ArrayList<>();
        for (TableInfo otherTable : targetContext.allTables()) {
            if (!otherTable.referencesOtherTable() || table.getTableName().equals(otherTable.getTableName())) {
                continue;
            }
            for (ColumnInfo column : otherTable.getForeignKeyColumns()) {
                if (!column.getForeignKeyInfo().getTableName().equals(table.getTableName())) {
                    continue;
                }
                ret.add(new Pair<>(otherTable, column));
            }
        }
        return ret;
    }

    private void addJoinResolverClasses(TypeSpec.Builder codeBuilder) {
        if (!hasJoins()) {
            return;
        }
        for (ColumnInfo column : table.getForeignKeyColumns()) {
            final TableInfo referencedTable = targetContext.getTable(column.getForeignKeyInfo().getTableName());
            final ColumnInfo referencedColumn = referencedTable.getColumn(column.getForeignKeyInfo().getColumnName());
            codeBuilder.addType(createJoinResolverClass(referencedTable, referencedColumn));
        }
        for (Pair<TableInfo, ColumnInfo> parentJoin : parentJoins) {
            codeBuilder.addType(createJoinResolverClass(parentJoin.first, parentJoin.second));
        }
    }

    private TypeSpec createJoinResolverClass(TableInfo referencedTable, ColumnInfo referencedColumn) {
        JavadocInfo jd = JavadocInfo.builder()
                .startParagraph()
                .addLine("Changes contexts from the $L table's Resolver context to the $L table's Resolver context", table.getTableName(), referencedTable.getTableName())
                .addLine("You can exit the $L table's Resolver context by calling the then() method.", referencedTable.getTableName())
                .endParagraph()
                .addLine()
                .build();
        TypeSpec.Builder joinClassBuilder = TypeSpec.classBuilder(CodeUtil.snakeToCamel("Join_" + referencedTable.getTableName(), true))
                .addJavadoc(jd.stringToFormat(), jd.replacements())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addTypeVariable(TypeVariableName.get("T", generatedClassName))
                .superclass(ParameterizedTypeName.get(ClassName.bestGuess(referencedTable.getQualifiedClassName() + "Resolver"), ParameterizedTypeName.get(ClassName.bestGuess(CodeUtil.snakeToCamel("Join_" + referencedTable.getTableName(), true)), TypeVariableName.get("T"))))
                .addField(TypeVariableName.get("T"), "parent", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addParameter(ParameterizedTypeName.get(ClassName.get(ForSureInfoFactory.class), ClassName.bestGuess(getResultParameter()), ClassName.get(getRecordContainerClass())), "infoFactory")
                        .addParameter(TypeVariableName.get("T"), "parent")
                        .addCode(CodeBlock.builder()
                                .addStatement("super($N)", "infoFactory")
                                .addStatement("$N.$N = $N", "this", "parent", "parent")
                                .build())
                        .build())
                .addMethod(MethodSpec.methodBuilder("get")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .returns(Retriever.class)
                        .addStatement("return then().get()")
                        .build())
                .addMethod(MethodSpec.methodBuilder("preserveQueryStateAndGet")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .returns(Retriever.class)
                        .addStatement("return then().preserveQueryStateAndGet()")
                        .build())
                .addMethod(MethodSpec.methodBuilder("then")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(TypeVariableName.get("T"))
                        .addStatement("joinResolvers($N, this)", "parent")
                        .addStatement("return $N", "parent")
                        .build())
                .addMethod(MethodSpec.methodBuilder("addJoin")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PROTECTED)
                        .addParameter(FSJoin.class, "join")
                        .addParameter(FSProjection.class, "foreignTableProjection")
                        .addStatement("$N.addJoin($N, $N)", "parent", "join", "foreignTableProjection")
                        .build());
        return joinClassBuilder.build();
    }

    private boolean hasJoins() {
        return table.referencesOtherTable() || !parentJoins.isEmpty();
    }

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
            codeBuilder.addMethod(createMethodSpecForJoin(targetContext.getTable(column.getForeignKeyInfo().getTableName()),
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
                codeBuilder.addMethod(createMethodSpecForJoin(targetTable, joinSpec, jd));
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

    private MethodSpec createMethodSpecForJoin(TableInfo joinedTable, TypeSpec childTableJoinSpec, JavadocInfo jd) {
        final String apiClassSimpleName = CodeUtil.simpleClassNameFrom(joinedTable.getQualifiedClassName());
        final ClassName returnClass = ClassName.bestGuess(CodeUtil.snakeToCamel("Join_" + joinedTable.getTableName(), true));
        return MethodSpec.methodBuilder("join" + apiClassSimpleName)
                .addJavadoc(jd.stringToFormat(), jd.replacements())
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(returnClass, TypeVariableName.get("T")))
                .addParameter(FSJoin.Type.class, "type", Modifier.FINAL)
                .addStatement("addJoin($L, $L.PROJECTION)", childTableJoinSpec, apiClassSimpleName + "Resolver")
                .addStatement("return new $T($L, $L)", returnClass, "infoFactory", "this")
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

    private List<TypeName> createParameterNames(TableInfo table) {
        List<TypeName> ret = new LinkedList<>();
        ret.add(TypeVariableName.get("T"));
        if (table.isDocStore()) {
            ret.add(ClassName.bestGuess(table.getDocStoreParameterization()));
        }
        ret.add(ClassName.bestGuess(getResultParameter()));
        ret.add(ClassName.get(getRecordContainerClass()));
        ret.add(getClassName);
        ret.add(setClassName);
        ret.add(ParameterizedTypeName.get((ClassName) finderClassName, TypeVariableName.get("T")));
        ret.add(ParameterizedTypeName.get((ClassName) orderByClassName, TypeVariableName.get("T")));
        return ret;
    }
}
