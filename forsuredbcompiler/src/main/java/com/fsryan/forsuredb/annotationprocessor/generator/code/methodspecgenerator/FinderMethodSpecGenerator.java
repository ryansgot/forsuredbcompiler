package com.fsryan.forsuredb.annotationprocessor.generator.code.methodspecgenerator;

import com.fsryan.forsuredb.annotationprocessor.generator.code.CodeUtil;
import com.fsryan.forsuredb.annotationprocessor.generator.code.JavadocInfo;
import com.fsryan.forsuredb.api.info.ColumnInfo;
import com.fsryan.forsuredb.api.Between;
import com.fsryan.forsuredb.api.Finder;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class FinderMethodSpecGenerator {

    private final ColumnInfo column;

    protected FinderMethodSpecGenerator(ColumnInfo column) {
        this.column = column;
    }

    public static FinderMethodSpecGenerator create(ColumnInfo column) {
        if (column == null || Strings.isNullOrEmpty(column.getQualifiedType()) || Strings.isNullOrEmpty(column.getColumnName())) {
            return new EmptyGenerator(column);
        }

        switch (column.getQualifiedType()) {
            case "java.util.Date":
                return new DateFinderMethodGenerator(column);
            case "java.lang.String":
                return new StringFinderMethodGenerator(column);
            case "java.math.BigDecimal":
                // intentionally fall through
            case "double":
                // intentionally fall through
            case "float":
                // intentionally fall through
            case "long":
                // intentionally fall through
            case "int":
                return new NumberFinderMethodGenerator(column);
            case "boolean":
                return new BooleanFinderMethodGenerator(column);
        }

        return new EmptyGenerator(column);
    }

    public final List<MethodSpec> generate(TypeName[] parameterClasses) {
        if (column == null || Strings.isNullOrEmpty(column.getQualifiedType()) || Strings.isNullOrEmpty(column.getColumnName())) {
            return Collections.emptyList();
        }

        final String methodNameInsertion = CodeUtil.snakeToCamel(column.getColumnName(), true);
        final ParameterizedTypeName conjunctionType = ParameterizedTypeName.get(ClassName.get(Finder.Conjunction.class), parameterClasses);
        final ParameterizedTypeName betweenType = ParameterizedTypeName.get(ClassName.get(Between.class), parameterClasses);

        List<MethodSpec> retList = new ArrayList<>();
        if (hasBeforeAfterGrammar()) {
            retList.addAll(beforeAfterMethodSpecs("by" + methodNameInsertion, conjunctionType, betweenType));
        }
        if (hasIsIsNotGrammar()) {
            retList.addAll(isIsNotMethodSpecs(methodNameInsertion, conjunctionType));
        }
        if (hasOnNotOnGrammar()) {
            retList.add(createSpec(conjunctionType, "by" + methodNameInsertion + "On", "exactMatch", Finder.Operator.EQ));
            retList.add(createSpec(conjunctionType, "byNot" + methodNameInsertion + "On", "exclusion", Finder.Operator.NE));
        }
        if (hasGreaterThanLessThanGrammar()) {
            retList.addAll(greaterThanLessThanMethodSpecs("by" + methodNameInsertion, conjunctionType, betweenType));
        }
        if (hasLikeGrammar()) {
            retList.add(createSpec(conjunctionType, "by" + methodNameInsertion + "Like", "like", Finder.Operator.LIKE));
        }

        return retList;
    }

    /*
     * Methods which determine the grammar type of the generator.
     * This will determine whether the generated MethodSpec list
     * contains methods like byIdGreaterThan(long nonInclusiveLowerBound)
     */

    protected abstract boolean hasBeforeAfterGrammar();
    protected abstract boolean hasBetweenGrammar();
    protected abstract boolean hasIsIsNotGrammar();
    protected abstract boolean hasOnNotOnGrammar();
    protected abstract boolean hasGreaterThanLessThanGrammar();
    protected abstract boolean hasLikeGrammar();

    protected String translateParameter(String parameterName) {
        return parameterName;
    }

    private List<MethodSpec> beforeAfterMethodSpecs(String methodNamePrefix, ParameterizedTypeName conjunctionType, ParameterizedTypeName betweenType) {
        List<MethodSpec> retList = Lists.newArrayList(
                createSpec(conjunctionType, methodNamePrefix + "Before", "nonInclusiveUpperBound", Finder.Operator.LT),
                createSpec(conjunctionType, methodNamePrefix + "After", "nonInclusiveLowerBound", Finder.Operator.GT),
                createSpec(conjunctionType, methodNamePrefix + "BeforeInclusive", "inclusiveUpperBound", Finder.Operator.LE),
                createSpec(conjunctionType, methodNamePrefix + "AfterInclusive", "inclusiveLowerBound", Finder.Operator.GE)
        );

        if (hasBetweenGrammar()) {
            retList.add(createSpec(betweenType, methodNamePrefix + "Between", "nonInclusiveLowerBound", Finder.Operator.GT));
            retList.add(createSpec(betweenType, methodNamePrefix + "BetweenInclusive", "inclusiveLowerBound", Finder.Operator.GE));
        }

        return retList;
    }

    private List<MethodSpec> isIsNotMethodSpecs(String methodNameInsertion, ParameterizedTypeName conjunctionType) {
        List<MethodSpec> retList = new ArrayList<>();
        retList.add(createSpec(conjunctionType, "by" + methodNameInsertion, "exactMatch", Finder.Operator.EQ));
        retList.add(createSpec(conjunctionType,
                column.getQualifiedType().equals("boolean") ? "byNot" + methodNameInsertion : "by" + methodNameInsertion + "Not",
                "exclusion",
                Finder.Operator.NE));
        return retList;
    }

    private List<MethodSpec> greaterThanLessThanMethodSpecs(String methodNamePrefix, ParameterizedTypeName conjunctionType, ParameterizedTypeName betweenType) {
        List<MethodSpec> retList = Lists.newArrayList(
                createSpec(conjunctionType, methodNamePrefix + "LessThan", "nonInclusiveUpperBound", Finder.Operator.LT),
                createSpec(conjunctionType, methodNamePrefix + "GreaterThan", "nonInclusiveLowerBound", Finder.Operator.GT),
                createSpec(conjunctionType, methodNamePrefix + "LessThanInclusive", "inclusiveUpperBound", Finder.Operator.LE),
                createSpec(conjunctionType, methodNamePrefix + "GreaterThanInclusive", "inclusiveLowerBound", Finder.Operator.GE)
        );

        if (hasBetweenGrammar()) {
            retList.add(createSpec(betweenType, methodNamePrefix + "Between", "nonInclusiveLowerBound", Finder.Operator.GT));
            retList.add(createSpec(betweenType, methodNamePrefix + "BetweenInclusive", "inclusiveLowerBound", Finder.Operator.GE));
        }

        return retList;
    }

    private MethodSpec createSpec(ParameterizedTypeName returnType, String methodName, String parameterName, Finder.Operator op) {
        JavadocInfo jd = javadocInfoFor(returnType, parameterName);
        MethodSpec.Builder codeBuilder = MethodSpec.methodBuilder(methodName)
                .addJavadoc(jd.stringToFormat(), jd.replacements())
                .addModifiers(Modifier.PUBLIC)
                .addStatement("addToBuf($S, Finder.Operator.$L, $L)", column.getColumnName(), op.name(), translateParameter(parameterName))
                .returns(returnType);

        if (!column.getQualifiedType().equals("boolean")) {
            codeBuilder.addParameter(CodeUtil.typeFromName(column.getQualifiedType()), parameterName);
        }

        if (returnType.rawType.simpleName().equals("Between")) {
            codeBuilder.addStatement("return createBetween($L.class, $S)", column.getQualifiedType(), column.getColumnName());
        } else {
            codeBuilder.addStatement("return conjunction");
        }

        return codeBuilder.build();
    }

    private JavadocInfo javadocInfoFor(ParameterizedTypeName returnType, String parameterName) {
        JavadocInfo.Builder jdBuilder = JavadocInfo.builder()
                .startParagraph()
                .addLine("add criteria to a query that requires $L for $L", parameterName, column.getColumnName())
                .endParagraph()
                .param(parameterName);
        if (returnType.rawType.simpleName().equals("Between")) {
            jdBuilder.returns("a $L that allows you to provide an upper bound for this criteria", JavadocInfo.inlineClassLink(Between.class));
        } else {
            jdBuilder.returns("a $L that allows you to continue adding more query criteria", JavadocInfo.inlineClassLink(Finder.Conjunction.class));
        }
        return jdBuilder.addLine().build();
    }

    private static class EmptyGenerator extends FinderMethodSpecGenerator {

        protected EmptyGenerator(ColumnInfo column) {
            super(column);
        }

        @Override
        protected boolean hasBeforeAfterGrammar() {
            return false;
        }

        @Override
        protected boolean hasBetweenGrammar() {
            return false;
        }

        @Override
        protected boolean hasIsIsNotGrammar() {
            return false;
        }

        @Override
        protected boolean hasOnNotOnGrammar() {
            return false;
        }

        @Override
        protected boolean hasGreaterThanLessThanGrammar() {
            return false;
        }

        @Override
        protected boolean hasLikeGrammar() {
            return false;
        }
    }
}
