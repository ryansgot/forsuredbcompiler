package com.fsryan.forsuredb.annotationprocessor.generator.code;

import com.fsryan.forsuredb.annotationprocessor.generator.BaseGenerator;
import com.fsryan.forsuredb.api.BaseDocStoreGetter;
import com.fsryan.forsuredb.api.BaseGetter;
import com.fsryan.forsuredb.api.Retriever;
import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.TableInfo;
import com.squareup.javapoet.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fsryan.forsuredb.info.TableInfo.defaultColumns;
import static com.fsryan.forsuredb.info.TableInfo.docStoreColumns;

public abstract class GetterGenerator extends JavaSourceGenerator {

    private static final Map<String, String> columnQualifiedTypeToRetrieverMethodNameMap = new HashMap<>();
    static {
        try {
            columnQualifiedTypeToRetrieverMethodNameMap.put(
                    "byte[]",
                    Retriever.class.getDeclaredMethod("getBytes", String.class).getName()
            );
            columnQualifiedTypeToRetrieverMethodNameMap.put(
                    float.class.getName(),
                    Retriever.class.getDeclaredMethod("getFloat", String.class).getName()
            );
            columnQualifiedTypeToRetrieverMethodNameMap.put(
                    Float.class.getName(),
                    Retriever.class.getDeclaredMethod("getFloat", String.class).getName()
            );
            columnQualifiedTypeToRetrieverMethodNameMap.put(
                    double.class.getName(),
                    Retriever.class.getDeclaredMethod("getDouble", String.class).getName()
            );
            columnQualifiedTypeToRetrieverMethodNameMap.put(
                    Double.class.getName(),
                    Retriever.class.getDeclaredMethod("getDouble", String.class).getName()
            );
            columnQualifiedTypeToRetrieverMethodNameMap.put(
                    int.class.getName(),
                    Retriever.class.getDeclaredMethod("getInt", String.class).getName()
            );
            columnQualifiedTypeToRetrieverMethodNameMap.put(
                    Integer.class.getName(),
                    Retriever.class.getDeclaredMethod("getInt", String.class).getName()
            );
            columnQualifiedTypeToRetrieverMethodNameMap.put(
                    boolean.class.getName(),
                    Retriever.class.getDeclaredMethod("getInt", String.class).getName()
            );
            columnQualifiedTypeToRetrieverMethodNameMap.put(
                    Boolean.class.getName(),
                    Retriever.class.getDeclaredMethod("getInt", String.class).getName()
            );
            columnQualifiedTypeToRetrieverMethodNameMap.put(
                    BigInteger.class.getName(),
                    Retriever.class.getDeclaredMethod("getString", String.class).getName()
            );
            columnQualifiedTypeToRetrieverMethodNameMap.put(
                    BigDecimal.class.getName(),
                    Retriever.class.getDeclaredMethod("getString", String.class).getName()
            );
            columnQualifiedTypeToRetrieverMethodNameMap.put(
                    Date.class.getName(),
                    Retriever.class.getDeclaredMethod("getString", String.class).getName()
            );
            columnQualifiedTypeToRetrieverMethodNameMap.put(
                    long.class.getName(),
                    Retriever.class.getDeclaredMethod("getLong", String.class).getName()
            );
            columnQualifiedTypeToRetrieverMethodNameMap.put(
                    Long.class.getName(),
                    Retriever.class.getDeclaredMethod("getLong", String.class).getName()
            );
            columnQualifiedTypeToRetrieverMethodNameMap.put(
                    String.class.getName(),
                    Retriever.class.getDeclaredMethod("getString", String.class).getName()
            );
        } catch (Exception e) {
            throw new RuntimeException("Verify columnQualifiedTypeToRetrieverMethodNameMap", e);
        }
    }
    private static final Map<String, Type> returnTypeMap = new HashMap<>();
    static {
        returnTypeMap.put(int.class.getName(), int.class);
        returnTypeMap.put(Integer.class.getName(), Integer.class);
        returnTypeMap.put(boolean.class.getName(), boolean.class);
        returnTypeMap.put(Boolean.class.getName(), Boolean.class);
        returnTypeMap.put(long.class.getName(), long.class);
        returnTypeMap.put(Long.class.getName(), Long.class);
        returnTypeMap.put(double.class.getName(), double.class);
        returnTypeMap.put(Double.class.getName(), Double.class);
        returnTypeMap.put(float.class.getName(), float.class);
        returnTypeMap.put(Float.class.getName(), Float.class);
        returnTypeMap.put("byte[]", byte[].class);
        returnTypeMap.put(String.class.getName(), String.class);
        returnTypeMap.put(BigInteger.class.getName(), BigInteger.class);
        returnTypeMap.put(BigDecimal.class.getName(), BigDecimal.class);
        returnTypeMap.put(Date.class.getName(), Date.class);
    }

    private static final String singletonInstanceIdentifier = "instance";
    private static final String getterMethodArgName = "retriever";
    private static final ParameterSpec retrieverParameterSpec = ParameterSpec.builder(Retriever.class, getterMethodArgName)
            .build();

    protected TableInfo table;

    protected GetterGenerator(ProcessingEnvironment processingEnv, TableInfo table) {
        super(processingEnv, getterClassNameStr(table, true));
        this.table = table;
    }

    public static GetterGenerator getFor(ProcessingEnvironment processingEnv, TableInfo table) {
        return table.isDocStore() ? new DocStore(processingEnv, table) : new Relational(processingEnv, table);
    }

    protected abstract TypeName getSuperclass();
    protected abstract ColumnInfo[] getColumnExclusions();

    @Override
    protected String getCode() {
        return JavaFile.builder(getOutputPackageName(), classBuilder().build())
                .indent(BaseGenerator.JAVA_INDENT)
                .build()
                .toString();
    }

    private TypeSpec.Builder classBuilder() {
        JavadocInfo jdInfo = JavadocInfo.builder()
                .startParagraph()
                .addLine("A getter API for the $T table", getterClassName(table))
                .endParagraph()
                .addLine()
                .build();
        return TypeSpec.classBuilder(getterClassNameStr(table, false))
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ClassName.bestGuess(table.qualifiedClassName()))
                .addAnnotations(getClassAnnotations())
                .superclass(getSuperclass())
                .addJavadoc(jdInfo.stringToFormat(), jdInfo.replacements())
                .addField(singletonInstanceFieldSpec())
                .addMethod(createConstructorMethod())
                .addMethod(singletonGetterMethodSpec())
                .addMethods(createMethods());
    }

    protected abstract MethodSpec createConstructorMethod();
    protected abstract FieldSpec singletonInstanceFieldSpec();

    private MethodSpec singletonGetterMethodSpec() {
        return MethodSpec.methodBuilder("inst")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(getterClassName(table))
                .addStatement("return $N", singletonInstanceIdentifier)
                .build();
    }

    private List<MethodSpec> createMethods() {
        ColumnInfo[] toExclude = getColumnExclusions();
        return TableDataUtil.columnsSortedByName(table, toExclude)
                .stream()
                .map(GetterGenerator::columnInfoToMethodSpecFunction)
                .collect(Collectors.toList());
    }

    private static MethodSpec columnInfoToMethodSpecFunction(ColumnInfo columnInfo) {
        final JavadocInfo jdInfo = createMethodJavadoc(columnInfo);
        return MethodSpec.methodBuilder(columnInfo.methodName())
                .addJavadoc(jdInfo.stringToFormat(), jdInfo.replacements())
                .addModifiers(Modifier.PUBLIC)
                .addParameter(retrieverParameterSpec)
                .returns(returnTypeMap.get(columnInfo.qualifiedType()))
                .addCode(getterMethodCode(columnInfo))
                .build();
    }

    private static CodeBlock getterMethodCode(ColumnInfo columnInfo) {
        final String columnName = columnInfo.columnName();
        final String retrieverMethodName = columnQualifiedTypeToRetrieverMethodNameMap.get(columnInfo.qualifiedType());
        CodeBlock.Builder builder = CodeBlock.builder()
                .addStatement("$N($N)", "throwIfNullRetriever", getterMethodArgName)
                .addStatement("String $N = $N($S)", "disambiguatedColumn", "disambiguateColumn", columnName);
        if (!columnInfo.hasPrimitiveType()) {
            builder.beginControlFlow("if ($N.isNull($N))", getterMethodArgName, "disambiguatedColumn")
                    .addStatement("return null")
                    .endControlFlow();
        }
        switch (columnInfo.qualifiedType()) {
            case "boolean": // falling through intentionally
            case "java.lang.Boolean":
                builder.addStatement("int $N = $N.$L($N)", "ret", getterMethodArgName, retrieverMethodName, "disambiguatedColumn")
                        .addStatement("return $N == $L", "ret", 1);
                break;
            case "java.math.BigInteger":
                addNumberTypeRetrieverExtraction(builder, columnName, retrieverMethodName, BigInteger.class);
                break;
            case "java.math.BigDecimal":
                addNumberTypeRetrieverExtraction(builder, columnName, retrieverMethodName, BigDecimal.class);
                break;
            case "java.util.Date":
                builder.addStatement("String $N = $N.$L($N)", "ret", getterMethodArgName, retrieverMethodName, "disambiguatedColumn")
                        .addStatement("return $N.$N($N)", "sqlGenerator", "parseDate", "ret");
                break;
            default:
                builder.addStatement("return $N.$L($N)", getterMethodArgName, retrieverMethodName, "disambiguatedColumn");
        }
        return builder.build();
    }

    private static void addNumberTypeRetrieverExtraction(CodeBlock.Builder builder, String columnName, String retrieverMethodName, Class<? extends Number> cls) {
        builder.addStatement("String $N = $N.$L($N)", "val", getterMethodArgName, retrieverMethodName, "disambiguatedColumn")
                .beginControlFlow("try")
                .addStatement("return new $T($N)", ClassName.get(cls), "val")
                .nextControlFlow("catch ($T $N)", ClassName.get(NumberFormatException.class), "nfe")
                .addStatement(
                        "throw new $T($S + $N, $N)",
                        ClassName.get(IllegalArgumentException.class), "Looks like " + columnName + " was not a " +cls + "; actual value = ",
                        "val",
                        "nfe"
                ).endControlFlow();
    }

    private static JavadocInfo createMethodJavadoc(ColumnInfo columnInfo) {
        return JavadocInfo.builder()
                .startParagraph()
                .addLine("retrieve the $L value from the {@link Retriever} passed in", columnInfo.columnName())
                .endParagraph()
                .param("retriever", "The {@link Retriever} that can pull column values from the current record")
                .returns("the value of column $L for the current record", columnInfo.columnName())
                .throwsWarning(IllegalArgumentException.class, "when input retriever is null")
                .addLine()
                .build();
    }

    private static ClassName getterClassName(TableInfo table) {
        return ClassName.bestGuess(getterClassNameStr(table, false));
    }

    private static String getterClassNameStr(TableInfo table, boolean qualified) {
        return (qualified ? table.qualifiedClassName() : table.getSimpleClassName()) + "Getter";
    }

    private static class DocStore extends GetterGenerator {
        DocStore(ProcessingEnvironment processingEnv, TableInfo table) {
            super(processingEnv, table);
        }

        @Override
        protected TypeName getSuperclass() {
            return ParameterizedTypeName.get(
                    ClassName.get(BaseDocStoreGetter.class),
                    ClassName.bestGuess(table.docStoreParameterization())
            );
        }

        @Override
        protected ColumnInfo[] getColumnExclusions() {
            return Stream.concat(defaultColumns().values().stream(), docStoreColumns().values().stream())
                    .toArray(ColumnInfo[]::new);
        }

        @Override
        protected MethodSpec createConstructorMethod() {
            return MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PRIVATE)
                    .addParameter(String.class, "tableName")
                    .addStatement("super($N, $T.class)", "tableName", ClassName.bestGuess(table.docStoreParameterization()))
                    .build();
        }

        @Override
        protected FieldSpec singletonInstanceFieldSpec() {
            return FieldSpec.builder(getterClassName(table), singletonInstanceIdentifier, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .initializer("new $T($S)", getterClassName(table), table.tableName())
                    .build();
        }
    }

    private static class Relational extends GetterGenerator {
        Relational(ProcessingEnvironment processingEnv, TableInfo table) {
            super(processingEnv, table);
        }

        @Override
        protected TypeName getSuperclass() {
            return ClassName.get(BaseGetter.class);
        }

        @Override
        protected ColumnInfo[] getColumnExclusions() {
            return defaultColumns().values().toArray(new ColumnInfo[0]);
        }

        @Override
        protected MethodSpec createConstructorMethod() {
            return MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PRIVATE)
                    .addParameter(String.class, "tableName")
                    .addStatement("super($N)", "tableName")
                    .build();
        }

        @Override
        protected FieldSpec singletonInstanceFieldSpec() {
            return FieldSpec.builder(getterClassName(table), singletonInstanceIdentifier, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .initializer("new $T($S)", getterClassName(table), table.tableName())
                    .build();
        }
    }
}
