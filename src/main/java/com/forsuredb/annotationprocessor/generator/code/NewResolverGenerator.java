package com.forsuredb.annotationprocessor.generator.code;

import com.forsuredb.annotationprocessor.TableContext;
import com.forsuredb.annotationprocessor.info.ColumnInfo;
import com.forsuredb.annotationprocessor.info.ForeignKeyInfo;
import com.forsuredb.annotationprocessor.info.TableInfo;
import com.forsuredb.api.FSJoin;
import com.forsuredb.api.FSProjection;
import com.forsuredb.api.ForSureInfoFactory;
import com.forsuredb.api.Resolver;
import com.google.common.base.Strings;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

public class NewResolverGenerator extends JavaSourceGenerator {

    private final TableInfo table;
    private final TableContext targetContext;
    private List<ColumnInfo> columnsSortedByName;
    private final ClassName resultParameterClassName;
    private final ClassName recordContainerClassName;
    private final ClassName getClassName;
    private final ClassName setClassName;
    private final ClassName finderClassName;

    public NewResolverGenerator(ProcessingEnvironment processingEnv, TableInfo table, TableContext targetContext) {
        super(processingEnv, table.getQualifiedClassName() + "Resolver");
        this.table = table;
        this.targetContext = targetContext;
        columnsSortedByName = TableDataUtil.columnsSortedByName(table);
        resultParameterClassName = ClassName.get(getResultParameter());
        recordContainerClassName = ClassName.get(getRecordContainer());
        getClassName = ClassName.bestGuess(table.getQualifiedClassName());
        setClassName = ClassName.bestGuess(table.getQualifiedClassName() + "Setter");
        finderClassName = ClassName.bestGuess(table.getQualifiedClassName() + "Finder");
    }

    @Override
    protected String getCode() {
        TypeSpec.Builder codeBuilder = TypeSpec.classBuilder(getOutputClassName(false))
//                .addJavadoc(jd.stringToFormat(), jd.replacements())
                .addModifiers(Modifier.PUBLIC)
                .superclass(ParameterizedTypeName.get(ClassName.get(Resolver.class),
                        resultParameterClassName,
                        recordContainerClassName,
                        getClassName,
                        setClassName,
                        finderClassName));
        addFields(codeBuilder);
        addConstructor(codeBuilder);
        addJoinMethods(codeBuilder);
        addAbstractMethodImplementations(codeBuilder);
        return JavaFile.builder(getOutputPackageName(), codeBuilder.build()).indent(JAVA_INDENT).build().toString();
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
            codeBuilder.addMethod(createMethodSpecForJoin(column.getForeignKeyInfo().getApiClassName(), createChildTableJoinSpec(column)));
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
                codeBuilder.addMethod(createMethodSpecForJoin(targetTable.getQualifiedClassName(), joinSpec));
            }
        }
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

    private MethodSpec createMethodSpecForJoin(String joinedTableApiClass, TypeSpec childTableJoinSpec) {
        final String apiClassSimpleName = CodeUtil.simpleClassNameFrom(joinedTableApiClass);
        return MethodSpec.methodBuilder("join" + apiClassSimpleName)
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.bestGuess(getOutputClassName(false)))
                .addParameter(FSJoin.Type.class, "type", Modifier.FINAL)
                .addStatement("addJoin($L, $L.PROJECTION)", childTableJoinSpec, apiClassSimpleName)
                .addStatement("return this")
                .build();
    }

    private void addAbstractMethodImplementations(TypeSpec.Builder codeBuilder) {
        codeBuilder.addMethod(MethodSpec.methodBuilder("getApiClass")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PROTECTED)
                        .returns(ParameterizedTypeName.get(ClassName.get(Class.class), getClassName))
                        .addStatement("return $L.class", CodeUtil.simpleClassNameFrom(table.getQualifiedClassName()))
                        .build())
                .addMethod(MethodSpec.methodBuilder("setApiClass")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PROTECTED)
                        .returns(ParameterizedTypeName.get(ClassName.get(Class.class), setClassName))
                        .addStatement("return $L.class", CodeUtil.simpleClassNameFrom(table.getQualifiedClassName() + "Setter"))
                        .build())
                .addMethod(MethodSpec.methodBuilder("projection")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PROTECTED)
                        .returns(FSProjection.class)
                        .addStatement("return PROJECTION")
                        .build())
                .addMethod(MethodSpec.methodBuilder("newFinderInstance")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PROTECTED)
                        .returns(finderClassName)
                        .addStatement("return new $T(this)", finderClassName)
                        .build())
                .addMethod(MethodSpec.methodBuilder("tableName")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PROTECTED)
                        .returns(String.class)
                        .addStatement("return TABLE_NAME")
                        .build());
    }
}