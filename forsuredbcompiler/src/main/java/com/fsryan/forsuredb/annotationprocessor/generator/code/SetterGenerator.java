package com.fsryan.forsuredb.annotationprocessor.generator.code;

import com.fsryan.forsuredb.annotationprocessor.generator.BaseGenerator;
import com.fsryan.forsuredb.api.*;
import com.fsryan.forsuredb.api.sqlgeneration.DBMSIntegrator;
import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.TableInfo;
import com.google.common.collect.Streams;
import com.squareup.javapoet.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.fsryan.forsuredb.annotationprocessor.generator.code.CodeUtil.typeNameOf;
import static com.fsryan.forsuredb.info.TableInfo.defaultColumns;
import static com.fsryan.forsuredb.info.TableInfo.docStoreColumns;

public abstract class SetterGenerator extends JavaSourceGenerator {

    static final String RECORD_CONTAINER_FIELD = "recordContainer";

    final TypeName fsQueryableType = ParameterizedTypeName.get(
            ClassName.get(FSQueryable.class),
            ClassName.bestGuess(getResultParameter()),
            ClassName.bestGuess(getRecordContainer())
    );

    protected TableInfo table;

    SetterGenerator(ProcessingEnvironment processingEnv, TableInfo table) {
        super(processingEnv, setterClassNameStr(table, true));
        this.table = table;
    }

    public static SetterGenerator getFor(ProcessingEnvironment processingEnv, TableInfo table) {
        return table.isDocStore() ? new DocStore(processingEnv, table) : new Relational(processingEnv, table);
    }

    @Override
    protected String getCode() {
        JavadocInfo jdInfo = JavadocInfo.builder()
                .startParagraph()
                .addLine("A setter api for the $L table", table.tableName())
                .endParagraph()
                .addLine()
                .build();
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(setterClassName())
                .addJavadoc(jdInfo.stringToFormat(), jdInfo.replacements())
                .addAnnotation(GENERATED_ANNOTATION)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(superClassType())
                .addSuperinterface(ClassName.bestGuess(table.qualifiedClassName() + "SaveApi"))
                .addMethod(constructorMethod())
                .addMethods(createSetterMethods())
                .addMethods(addExtraMethods());
        return JavaFile.builder(getOutputPackageName(), classBuilder.build())
                .indent(BaseGenerator.JAVA_INDENT)
                .build()
                .toString();
    }

    private List<MethodSpec> createSetterMethods() {
        List<MethodSpec> ret = new ArrayList<>();
        for (ColumnInfo column : TableDataUtil.columnsSortedByName(table, columnExclusions())) {
            ret.add(methodSpecFor(column));
        }
        return ret;
    }

    protected abstract TypeName superClassType();
    protected abstract List<MethodSpec> addExtraMethods();
    protected abstract ColumnInfo[] columnExclusions();
    protected abstract MethodSpec constructorMethod();

    private ClassName setterClassName() {
        return ClassName.bestGuess(setterClassNameStr(table, false));
    }

    private MethodSpec methodSpecFor(ColumnInfo column) {
        JavadocInfo jdInfo = JavadocInfo.builder()
                .startParagraph()
                .addLine("Set the value of column $L on the record to store", column.columnName())
                .endParagraph()
                .addLine()
                .build();
        return MethodSpec.methodBuilder(column.methodName())
                .addJavadoc(jdInfo.stringToFormat(), jdInfo.replacements())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(setterClassName())
                .addParameter(ParameterSpec.builder(typeNameOf(column), column.methodName()).build())
                .addCode(recordContainerUpdateBlockFor(column))
                .addStatement("return this")
                .build();
    }

    private static CodeBlock recordContainerUpdateBlockFor(ColumnInfo column) {
        CodeBlock.Builder builder = CodeBlock.builder();
        switch (column.qualifiedType()) {
            case "int": // intentionally fall through
            case "java.lang.Integer": // intentionally fall through
            case "long": // intentionally fall through
            case "java.lang.Long": // intentionally fall through
            case "double": // intentionally fall through
            case "java.lang.Double": // intentionally fall through
            case "float": // intentionally fall through
            case "java.lang.Float": // intentionally fall through
            case "byte[]": // intentionally fall through
            case "java.lang.String":
                builder.addStatement("$N.put($S, $N)", RECORD_CONTAINER_FIELD, column.columnName(), column.methodName());
                break;
            case "boolean": // intentionally fall through
            case "java.lang.Boolean":
                builder.addStatement("$N.put($S, $N ? 1 : 0)", RECORD_CONTAINER_FIELD, column.columnName(), column.methodName());
                break;
            case "java.util.Date":
                builder.addStatement("$N.put($S, $N.formatDate($N))", RECORD_CONTAINER_FIELD, column.columnName(), "sqlGenerator", column.methodName());
                break;
            case "java.math.BigDecimal":    // intentionally fall through
            case "java.math.BigInteger":
                builder.addStatement("$N.put($S, $N.toString())", RECORD_CONTAINER_FIELD, column.columnName(), column.methodName());
                break;
            default:
                throw new IllegalStateException("Cannot set value of type: " + column.qualifiedType());
        }

        return builder.build();
    }

    private static String setterClassNameStr(TableInfo table, boolean qualified) {
        return (qualified ? table.qualifiedClassName() : table.getSimpleClassName()) + "Setter";
    }

    static class Relational extends SetterGenerator {

        Relational(ProcessingEnvironment processingEnv, TableInfo table) {
            super(processingEnv, table);
        }

        @Override
        protected TypeName superClassType() {
            return ParameterizedTypeName.get(
                    ClassName.get(BaseSetter.class),
                    ClassName.bestGuess(getResultParameter())
            );
        }

        @Override
        protected List<MethodSpec> addExtraMethods() {
            return Collections.emptyList();
        }

        @Override
        protected ColumnInfo[] columnExclusions() {
            return TableInfo.defaultColumns().values().toArray(new ColumnInfo[0]);
        }

        @Override
        protected MethodSpec constructorMethod() {
            return MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(fsQueryableType, "queryble")
                    .addParameter(FSSelection.class, "selection")
                    .addParameter(ParameterizedTypeName.get(List.class, FSOrdering.class), "orderings")
                    .addParameter(RecordContainer.class, "recordContainer")
                    .addStatement("super(queryable, selection, orderings, recordContainer)")
                    .build();
        }
    }

    static class DocStore extends SetterGenerator {

        private final ClassName baseClass;

        DocStore(ProcessingEnvironment processingEnv, TableInfo table) {
            super(processingEnv, table);
            baseClass = ClassName.bestGuess(table.docStoreParameterization());
        }

        @Override
        protected TypeName superClassType() {
            return ParameterizedTypeName.get(
                    ClassName.get(BaseSetter.class),
                    ClassName.bestGuess(getResultParameter()),
                    baseClass
            );
        }

        @Override
        protected List<MethodSpec> addExtraMethods() {
            MethodSpec.Builder enrichingMethodBuilder = MethodSpec.methodBuilder("enrichRecordContainerFromPropertiesOf")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PROTECTED, Modifier.FINAL)
                    .addParameter(baseClass, "obj", Modifier.FINAL);
            for (ColumnInfo column : TableDataUtil.columnsSortedByName(table, columnExclusions())) {
                throwIfIllegalType(column);

                if (column.valueAccess() == null) {
                    throw new IllegalStateException(String.format("Column %s on table %s must be defined with a valueAccess property so that the column may be updated with the value from the underlying object document when stored.", column.columnName(), table.tableName()));
                }

                StringBuilder buf = new StringBuilder("final Object $N = $N");
                String[] replacements = new String[2 + column.valueAccess().size()];
                replacements[0] = column.methodName();
                replacements[1] = "obj";
                for (int i = 0; i < column.valueAccess().size(); i++) {
                    replacements[i + 2] = column.valueAccess().get(i);
                    buf.append(".$N()");
                }
                enrichingMethodBuilder.addStatement(buf.append(")").toString(), replacements);
                enrichingMethodBuilder.addStatement("$N($S, $N)", "performPropertyEnrichment", column.columnName(), replacements[0]);
            }

            MethodSpec.Builder testingConstructorBuilder = MethodSpec.constructorBuilder()
                    .addParameter(DBMSIntegrator.class, "sqlGenerator")
                    .addParameter(fsQueryableType, "queryble")
                    .addParameter(FSSelection.class, "selection")
                    .addParameter(ParameterizedTypeName.get(List.class, FSOrdering.class), "orderings")
                    .addParameter(RecordContainer.class, "recordContainer")
                    .addStatement("super(queryable, selection, orderings, recordContainer)")
                    .addStatement("this.sqlGenerator = sqlGenerator");

            return Arrays.asList(enrichingMethodBuilder.build(), testingConstructorBuilder.build());
        }

        @Override
        protected ColumnInfo[] columnExclusions() {
            return Streams.concat(defaultColumns().values().stream(), docStoreColumns().values().stream())
                    .collect(Collectors.toList())
                    .toArray(new ColumnInfo[0]);
        }

        @Override
        protected MethodSpec constructorMethod() {
            return MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(fsQueryableType, "queryble")
                    .addParameter(FSSelection.class, "selection")
                    .addParameter(ParameterizedTypeName.get(List.class, FSOrdering.class), "orderings")
                    .addParameter(RecordContainer.class, "recordContainer")
                    .addStatement("this(Sql.generator(), queryable, selection, orderings, recordContainer)")
                    .build();
        }

        private static boolean throwIfIllegalType(ColumnInfo column) {
            switch (column.qualifiedType()) {
                case "int": // intentionally fall through
                case "java.lang.Integer": // intentionally fall through
                case "long": // intentionally fall through
                case "java.lang.Long": // intentionally fall through
                case "float": // intentionally fall through
                case "java.lang.Float": // intentionally fall through
                case "byte[]": // intentionally fall through
                case "java.lang.String":    // intentionally fall through
                case "boolean": // intentionally fall through
                case "java.lang.Boolean":   // intentionally fall through
                case "java.util.Date":  // intentionally fall through
                case "java.math.BigDecimal":    // intentionally fall through
                case "java.math.BigInteger": // intentionally fall through
                default:
                    throw new IllegalStateException("Cannot set value of type: " + column.qualifiedType());
            }
        }
    }
}