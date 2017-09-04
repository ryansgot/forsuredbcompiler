package com.fsryan.forsuredb.annotationprocessor.generator.code;

import com.fsryan.forsuredb.annotationprocessor.TableContext;
import com.fsryan.forsuredb.annotations.FSTable;
import com.fsryan.forsuredb.api.info.TableInfo;
import com.fsryan.forsuredb.api.FSGetApi;
import com.fsryan.forsuredb.api.ForSureInfoFactory;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.util.List;

public class ForSureGenerator extends JavaSourceGenerator {

    private final List<TableInfo> tablesSortedByName;
    private final TableInfo exampleTable;

    public ForSureGenerator(ProcessingEnvironment processingEnv, String packageName, TableContext targetContext) {
        super(processingEnv, packageName + ".ForSure");
        tablesSortedByName = TableDataUtil.tablesSortedByName(targetContext);
        exampleTable = tablesSortedByName.size() > 0 ? tablesSortedByName.get(0) : null;
    }

    @Override
    protected String getCode() {
        final ParameterizedTypeName infoFactoryTypeName = ParameterizedTypeName.get(ClassName.get(ForSureInfoFactory.class),
                ClassName.bestGuess(getResultParameter()),
                ClassName.bestGuess(getRecordContainer()));
        JavadocInfo jd = classJavadoc();
        TypeSpec.Builder codeBuilder = TypeSpec.classBuilder(getOutputClassName(false))
                .addJavadoc(jd.stringToFormat(), jd.replacements())
                .addModifiers(Modifier.PUBLIC);
        addFields(codeBuilder, infoFactoryTypeName);
        addConstructor(codeBuilder, infoFactoryTypeName);
        addInitMethod(codeBuilder, infoFactoryTypeName);
        addResolverMethods(codeBuilder);
        addThrowIfUninitializedMethod(codeBuilder);
        return JavaFile.builder(getOutputPackageName(), codeBuilder.build()).indent(JAVA_INDENT).build().toString();
    }

    private JavadocInfo classJavadoc() {
        if (exampleTable == null) {
            return JavadocInfo.builder()
                    .startParagraph()
                    .addLine("This is an auto-generated class. DO NOT modify it!")
                    .endParagraph()
                    .startParagraph()
                    .addLine("You did not mark any extensions of $L", JavadocInfo.inlineClassLink(FSGetApi.class))
                    .addLine("with the $L annotation", JavadocInfo.inlineClassLink(FSTable.class))
                    .addLine("This class will do nothing.")
                    .endParagraph()
                    .startParagraph()
                    .addLine("A class like the following will cause the magic to start happening:")
                    .startCode()
                    .addLine("    public interface MyTable extends FSGetApi {")
                    .addLine("@FSColumn($S) int my_int_column(Retriever retriever);", "my_int_column")
                    .addLine("}")
                    .endCode()
                    .endParagraph()
                    .addLine(JavadocInfo.AUTHOR_STRING)
                    .addLine()
                    .build();
        }
        return JavadocInfo.builder()
                .startParagraph()
                .addLine("This is an auto-generated class. DO NOT modify it!")
                .endParagraph()
                .startParagraph()
                .addLine("The entry point into any querying of the database tables you")
                .addLine("defined by extending the $L interface.", JavadocInfo.inlineClassLink(FSGetApi.class))
                .addLine("Common usage for getting data will include the following:")
                .startCode()
                .addLine("Retriever retriever = ForSure.$L().find()", CodeUtil.snakeToCamel(exampleTable.getTableName()) + "Table")
                .addLine(".byId(1)")
                .addLine(".then()")
                .addLine(".get();")
                .addLine("if (retriever.moveToFirst()) {")
                .indentJava()
                .addLine("do {")
                .indentJava()
                .addLine("long id = tableApi.id(retriever);")
                .addLine("Date modified = tableApi.modified(retriever);")
                .addLine("Date created = tableApi.created(retriever);")
                .addLine("boolean deleted = tableApi.deleted(retriever);")
                .addLine("// Any of the methods you added to your extension of FSGetApi")
                .unindentJava()
                .addLine("} while (retriever.moveToNext())")
                .unindentJava()
                .addLine("}")
                .addLine("retriever.close();")
                .endCode()
                .endParagraph()
                .startParagraph()
                .addLine("Common usage for updating an existing record into the table:")
                .startCode()
                .addLine("SaveResult<$L> result = ForSure.$L().find()", getResultParameter(), CodeUtil.snakeToCamel(exampleTable.getTableName()) + "Table")
                .addLine(".byId(1)")
                .addLine(".then()")
                .addLine(".set()")
                .addLine(".id(2)")
                .addLine(".save();")
                .endCode()
                .endParagraph()
                .startParagraph()
                .addLine("Common usage for inserting a new record into the table")
                .addLine("(with only default data):")
                .startCode()
                .addLine("SaveResult<$L> result = ForSure.$L().set()", getResultParameter(), CodeUtil.snakeToCamel(exampleTable.getTableName()) + "Table")
                .addLine(".save();")
                .endCode()
                .endParagraph()
                .startParagraph()
                .addLine("Common usage for flipping the deleted bit on a record:")
                .startCode()
                .addLine("SaveResult<$L> result = ForSure.$L().find()", getResultParameter(), CodeUtil.snakeToCamel(exampleTable.getTableName()) + "Table")
                .addLine(".byId(1)")
                .addLine(".then()")
                .addLine(".set()")
                .addLine(".softDelete();")
                .endCode()
                .endParagraph()
                .startParagraph()
                .addLine("Common usage for permanently the deleting a record:")
                .startCode()
                .addLine("SaveResult<$L> result = ForSure.$L().find()", getResultParameter(), CodeUtil.snakeToCamel(exampleTable.getTableName()) + "Table")
                .addLine(".byId(1)")
                .addLine(".then()")
                .addLine(".set()")
                .addLine(".hardDelete();")
                .endCode()
                .endParagraph()
                .addLine(JavadocInfo.AUTHOR_STRING)
                .addLine()
                .build();
    }

    private void addFields(TypeSpec.Builder codeBuilder, ParameterizedTypeName infoFactoryTypeName) {
        codeBuilder.addField(ClassName.bestGuess(getOutputClassName(true)), "instance", Modifier.PRIVATE, Modifier.STATIC)
                .addField(infoFactoryTypeName, "infoFactory", Modifier.PRIVATE);
    }

    private void addConstructor(TypeSpec.Builder codeBuilder, ParameterizedTypeName infoFactoryTypeName) {
        codeBuilder.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameter(infoFactoryTypeName, "infoFactory")
                .addStatement("this.infoFactory = infoFactory")
                .build());
    }

    private void addInitMethod(TypeSpec.Builder codeBuilder, ParameterizedTypeName infoFactoryTypeName) {
        codeBuilder.addMethod(MethodSpec.methodBuilder("init")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(infoFactoryTypeName, "infoFactory")
                .addCode(CodeBlock.builder()
                        .beginControlFlow("if (instance == null)")
                        .addStatement("instance = new ForSure(infoFactory)")
                        .endControlFlow()
                        .build())
                .build());
    }

    private void addResolverMethods(TypeSpec.Builder codeBuilder) {
        for (TableInfo table : tablesSortedByName) {
            ClassName resolverTypeName = ClassName.bestGuess(table.getQualifiedClassName() + "Resolver.Base");
            JavadocInfo jd = javadocInfoFor(table);
            codeBuilder.addMethod(MethodSpec.methodBuilder(CodeUtil.snakeToCamel(table.getTableName()) + "Table")
                    .addJavadoc(jd.stringToFormat(), jd.replacements())
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(resolverTypeName)
                    .addStatement("throwIfUninitialized()")
                    .addStatement("return new $T(instance.infoFactory)", resolverTypeName)
                    .build());
        }
    }

    private JavadocInfo javadocInfoFor(TableInfo table) {
        return JavadocInfo.builder()
                .startParagraph()
                .addLine("Access the querying mechanisms for the $L table.", table.getTableName())
                .endParagraph()
                .addLine("@see $L", table.getQualifiedClassName())
                .addLine("@see $L", table.getQualifiedClassName() + "Setter")
                .addLine("@see $L", table.getQualifiedClassName() + "Finder")
                .addLine("@see $L", table.getQualifiedClassName() + "Resolver")
                .addLine()
                .build();
    }

    private void addThrowIfUninitializedMethod(TypeSpec.Builder codeBuilder) {
        codeBuilder.addMethod(MethodSpec.methodBuilder("throwIfUninitialized")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addCode(CodeBlock.builder()
                        .beginControlFlow("if (instance == null)")
                        .addStatement("throw new IllegalStateException($S)", "Must init ForSure before use")
                        .endControlFlow()
                        .build())
                .build());
    }
}
