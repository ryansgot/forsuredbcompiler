package com.fsryan.forsuredb.annotationprocessor.generator.code;

import com.fsryan.forsuredb.annotationprocessor.TableContext;
import com.fsryan.forsuredb.annotationprocessor.util.Pair;
import com.fsryan.forsuredb.api.*;
import com.fsryan.forsuredb.api.info.ColumnInfo;
import com.fsryan.forsuredb.api.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.api.info.TableInfo;
import com.google.common.base.Strings;
import com.squareup.javapoet.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.util.*;

public class ResolverGenerator extends JavaSourceGenerator {

    private static final String methodNameToColumnNameMapName = "methodNameToColumnNameMap";

    private final TableInfo table;
    private final TableContext targetContext;
    private final List<Pair<TableInfo, TableForeignKeyInfo>> parentJoins;
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
        adMethodNameToColumnNameMapMethod(codeBuilder);
        addJoinMethods(codeBuilder);
        addAbstractMethodImplementations(codeBuilder);
        return JavaFile.builder(getOutputPackageName(), codeBuilder.build()).indent(JAVA_INDENT).build().toString();
    }

    private static List<Pair<TableInfo, TableForeignKeyInfo>> createParentJoins(TableInfo table, TableContext targetContext) {
        List<Pair<TableInfo, TableForeignKeyInfo>> ret = new ArrayList<>();
        for (TableInfo otherTable : targetContext.allTables()) {
            if (!otherTable.referencesOtherTable() || table.getTableName().equals(otherTable.getTableName())) {
                continue;
            }
            for (TableForeignKeyInfo foreignKey : otherTable.getForeignKeys()) {
                if (!foreignKey.getForeignTableName().equals(table.getTableName())) {
                    continue;
                }
                ret.add(new Pair<>(otherTable, foreignKey));
            }
        }
        return ret;
    }

    private void addJoinResolverClasses(TypeSpec.Builder codeBuilder) {
        if (!hasJoins()) {
            return;
        }
        for (TableForeignKeyInfo foreignKey : table.getForeignKeys()) {
            final TableInfo referencedTable = targetContext.getTable(foreignKey.getForeignTableName());
            codeBuilder.addType(createJoinResolverClass(referencedTable, foreignKey));
        }
        for (Pair<TableInfo, TableForeignKeyInfo> parentJoin : parentJoins) {
            codeBuilder.addType(createJoinResolverClass(parentJoin.first, parentJoin.second));
        }
    }

    private TypeSpec createJoinResolverClass(TableInfo referencedTable, TableForeignKeyInfo foreignKey) {
        String innerClassName = CodeUtil.snakeToCamel("Join_" + referencedTable.getTableName(), true);
        JavadocInfo jd = JavadocInfo.builder()
                .startParagraph()
                .addLine("Changes contexts from the $L table's Resolver context to the $L table's Resolver context", table.getTableName(), referencedTable.getTableName())
                .addLine("You can exit the $L table's Resolver context by calling the then() method.", referencedTable.getTableName())
                .endParagraph()
                .addLine()
                .build();
        return TypeSpec.classBuilder(innerClassName)
                .addJavadoc(jd.stringToFormat(), jd.replacements())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addTypeVariable(TypeVariableName.get("T", generatedClassName))
                .superclass(ParameterizedTypeName.get(ClassName.bestGuess(referencedTable.getQualifiedClassName() + "Resolver"), ParameterizedTypeName.get(ClassName.bestGuess(CodeUtil.snakeToCamel("Join_" + referencedTable.getTableName(), true)), TypeVariableName.get("T"))))
                .addField(TypeVariableName.get("T"), "parent", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addParameter(ParameterizedTypeName.get(ClassName.get(ForSureInfoFactory.class), ClassName.bestGuess(getResultParameter()), ClassName.bestGuess(getRecordContainer())), "infoFactory")
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
                        .addStatement("$N.addJoin($N)", "parent", "join")
                        .build())
                .build();
    }

    private boolean hasJoins() {
        return table.referencesOtherTable() || !parentJoins.isEmpty();
    }

    private void adMethodNameToColumnNameMapMethod(TypeSpec.Builder codeBuilder) {
        codeBuilder.addMethod(MethodSpec.methodBuilder("methodNameToColumnNameMap")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(Map.class, String.class, String.class))
                .addStatement("return $N", methodNameToColumnNameMapName)
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
        columnNameToMethodNameMapField(codeBuilder);
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
                .addMethod(MethodSpec.methodBuilder("isDistinct")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(boolean.class)
                        .addStatement("return false")
                        .build())
                .build();
    }

    private void columnNameToMethodNameMapField(TypeSpec.Builder codeBuilder) {
        codeBuilder.addField(FieldSpec.builder(ParameterizedTypeName.get(Map.class, String.class, String.class), methodNameToColumnNameMapName)
                .initializer("new $T()", ParameterizedTypeName.get(HashMap.class, String.class, String.class))
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .build());

        CodeBlock.Builder mapBlockBuilder = CodeBlock.builder();
        for (ColumnInfo column : columnsSortedByName) {
            mapBlockBuilder.addStatement("$N.put($S, $S)", methodNameToColumnNameMapName, column.getMethodName(), column.getColumnName());
        }
        codeBuilder.addStaticBlock(mapBlockBuilder.build());
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
        for (TableForeignKeyInfo foreignKey : table.getForeignKeys()) {
            JavadocInfo jd = javadocFor(foreignKey, table.getTableName());
            TableInfo targetTable = targetContext.getTable(foreignKey.getForeignTableName());
            codeBuilder.addMethod(createMethodSpecForJoin(targetTable, createJoinSpec(foreignKey, table.getTableName()), jd));
        }

        // add join methods where this table is the parent in the join relationship
        for (TableInfo targetTable : TableDataUtil.tablesSortedByName(targetContext, table)) {
            for (TableForeignKeyInfo foreignKey : targetTable.getForeignKeys()) {
                if (!table.getTableName().equals(foreignKey.getForeignTableName())) {
                    continue;
                }
                final CodeBlock joinSpec = createJoinSpec(foreignKey, targetTable.getTableName());
                JavadocInfo jd = javadocFor(foreignKey, targetTable.getTableName());
                codeBuilder.addMethod(createMethodSpecForJoin(targetTable, joinSpec, jd));
            }
        }
    }

    private JavadocInfo javadocFor(TableForeignKeyInfo foreignKey, String childTable) {
        StringBuilder onPart = new StringBuilder();
        for (Map.Entry<String, String> entry : foreignKey.getLocalToForeignColumnMap().entrySet()) {
            onPart.append(childTable).append(".").append(entry.getKey())
                    .append(" = ")
                    .append(foreignKey.getForeignTableName()).append(".").append(entry.getValue())
                    .append(" AND ");
        }
        return JavadocInfo.builder()
                .startParagraph()
                .addLine("Add a join to $L on $L",
                        table.getTableName().equals(foreignKey.getForeignTableName()) ? childTable : foreignKey.getForeignTableName(),
                        onPart.delete(onPart.length() - 5, onPart.length()).toString()
                )
                .addLine("to the query")
                .endParagraph()
                .addLine()
                .build();
    }

    // TODO: make sure the column maps to the correct table
    private CodeBlock createJoinSpec(TableForeignKeyInfo foreignKey, String childTableName) {
        CodeBlock.Builder builder = CodeBlock.builder();
        builder.addStatement("final $T localToForeignColumnMap = new $T($L)",
                ParameterizedTypeName.get(Map.class, String.class, String.class),
                ParameterizedTypeName.get(HashMap.class, String.class, String.class),
                foreignKey.getLocalToForeignColumnMap().size()
        );
        for (Map.Entry<String, String> entry : foreignKey.getLocalToForeignColumnMap().entrySet()) {
            builder.addStatement("$N.put($S, $S)", "localToForeignColumnMap", entry.getKey(), entry.getValue());
        }
        return builder.addStatement("addJoin(new $T($N, $S, $S, $N))",
                        FSJoin.class,
                        "type",
                        foreignKey.getForeignTableName(),
                        childTableName,
                        "localToForeignColumnMap")
                .build();
    }

    private MethodSpec createMethodSpecForJoin(TableInfo joinedTable, CodeBlock joinCodeBlock, JavadocInfo jd) {
        final String apiClassSimpleName = CodeUtil.simpleClassNameFrom(joinedTable.getQualifiedClassName());
        final ClassName returnClass = ClassName.bestGuess(CodeUtil.snakeToCamel("Join_" + joinedTable.getTableName(), true));
        return MethodSpec.methodBuilder("join" + apiClassSimpleName)
                .addJavadoc(jd.stringToFormat(), jd.replacements())
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(returnClass, TypeVariableName.get("T")))
                .addParameter(FSJoin.Type.class, "type", Modifier.FINAL)
                .addCode(joinCodeBlock)
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
        ret.add(ClassName.bestGuess(getRecordContainer()));
        ret.add(getClassName);
        ret.add(setClassName);
        ret.add(ParameterizedTypeName.get((ClassName) finderClassName, TypeVariableName.get("T")));
        ret.add(ParameterizedTypeName.get((ClassName) orderByClassName, TypeVariableName.get("T")));
        return ret;
    }
}
