package com.forsuredb.annotationprocessor.generator.code;

import com.forsuredb.annotation.FSTable;
import com.forsuredb.annotationprocessor.generator.NewBaseGenerator;
import com.forsuredb.annotationprocessor.info.ColumnInfo;
import com.forsuredb.annotationprocessor.info.TableInfo;
import com.forsuredb.api.FSGetApi;
import com.forsuredb.api.FSTableCreator;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static com.forsuredb.annotationprocessor.generator.code.JavadocInfo.inlineClassLink;

public class NewTableCreatorGenerator extends NewBaseGenerator<JavaFileObject> {

    private static final String CLASS_NAME = "TableGenerator";

    private final String appPackageName;
    private final Collection<TableInfo> tables;

    public NewTableCreatorGenerator(ProcessingEnvironment processingEnv, String appPackageName, Collection<TableInfo> tables) {
        super(processingEnv);
        this.appPackageName =appPackageName;
        this.tables = tables;
    }

    @Override
    protected JavaFileObject createFileObject(ProcessingEnvironment processingEnv) throws IOException {
        return processingEnv.getFiler().createSourceFile(getOutputClassName(true));
    }

    @Override
    protected String getCode() {
        JavadocInfo javadoc = JavadocInfo.builder()
                .startParagraph()
                .addLine("This is an automatically-generated class. DO NOT modify it!")
                .endParagraph()
                .startParagraph()
                .addLine("$L generates the basic information about the tables", getOutputClassName(false))
                .addLine("you have described in your extensions of")
                .addLine("$L. It should be called as you", inlineClassLink(FSGetApi.class))
                .addLine("initialize the database.")
                .endParagraph()
                .addLine("$L", JavadocInfo.AUTHOR_STRING)
                .addLine()
                .build();
        return JavaFile.builder(appPackageName, TypeSpec.classBuilder(getOutputClassName(false))
                        .addModifiers(Modifier.PUBLIC)
                        .addJavadoc(javadoc.stringToFormat(), javadoc.replacements())
                        .addMethod(noArgGenerateMethod())
                        .addMethod(generateMethodWithAuthorityArg())
                        .build())
                .indent("    ")
                .build()
                .toString();
    }

    private MethodSpec generateMethodWithAuthorityArg() {
        JavadocInfo jd = JavadocInfo.builder()
                .startParagraph()
                .addLine("Creates a list of $L", inlineClassLink(FSTableCreator.class))
                .addLine("objects that tell the underlying routing mechanism all it needs to")
                .addLine("know in order to set up the appropriate routes to the tables.")
                .endParagraph()
                .addLine("@param authority The authority for your database resources")
                .addLine("@return A list of $L corresponding", inlineClassLink(FSTableCreator.class))
                .addLine("to all $L extensions annotated with", inlineClassLink(FSGetApi.class))
                .addLine(inlineClassLink(FSTable.class))
                .addLine("@see #generate()")
                .addLine()
                .build();
        MethodSpec.Builder codeBuilder = MethodSpec.methodBuilder("generate")
                .addJavadoc(jd.stringToFormat(), jd.replacements())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ParameterizedTypeName.get(List.class, FSTableCreator.class))
                .addParameter(String.class, "authority")
                .addCode(CodeBlock.builder().add("// provide a reasonable default authority\n")
                        .addStatement("authority = authority == null || authority.isEmpty() ? $S : authority", "com.forsuredb.testapp.content")
                        .addStatement("final $T retList = new $T()", ParameterizedTypeName.get(List.class, FSTableCreator.class), ParameterizedTypeName.get(LinkedList.class, FSTableCreator.class))
                        .build());
        for (TableInfo table : tables) {
            codeBuilder.addStatement(createAddFSTableCreatorLine(table));
        }
        return codeBuilder.addStatement("return retList").build();
    }

    private MethodSpec noArgGenerateMethod() {
        JavadocInfo jd = JavadocInfo.builder()
                .startParagraph()
                .addLine("Creates a list of $L", inlineClassLink(FSTableCreator.class))
                .addLine("objects that tell the underlying routing mechanism all it needs to")
                .addLine("know in order to set up the appropriate routes to the tables.")
                .endParagraph()
                .startParagraph()
                .addLine("This version will use the default authority,")
                .addLine("$S", "com.forsuredb.testapp.content")
                .endParagraph()
                .addLine("@return A list of $L corresponding", inlineClassLink(FSTableCreator.class))
                .addLine("to all $L extensions annotated with", inlineClassLink(FSGetApi.class))
                .addLine(inlineClassLink(FSTable.class))
                .addLine("@see #generate(String)")
                .addLine()
                .build();
        return MethodSpec.methodBuilder("generate")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ParameterizedTypeName.get(List.class, FSTableCreator.class))
                .addJavadoc(jd.stringToFormat(), jd.replacements())
                .addCode(CodeBlock.builder()
                        .addStatement("return generate(null)")
                        .build())
                .build();
    }

    private String createAddFSTableCreatorLine(TableInfo tableInfo) {
        StringBuffer buf = new StringBuffer("retList").append(".add(new FSTableCreator(")
                .append("authority, ")
                .append(tableInfo.getQualifiedClassName()).append(".class");

        if (tableInfo.hasStaticData()) {
            buf.append(", \"").append(tableInfo.getStaticDataAsset()).append("\"")
                    .append(", \"").append(tableInfo.getStaticDataRecordName()).append("\"");
        }

        for (ColumnInfo column : tableInfo.getForeignKeyColumns()) {
            buf.append(", ").append(column.getForeignKeyInfo().getApiClassName()).append(".class");
        }

        return buf.append("))").toString();
    }

    private String getOutputClassName(boolean fullyQualified) {
        return fullyQualified ? appPackageName + "." + CLASS_NAME : CLASS_NAME;
    }
}
