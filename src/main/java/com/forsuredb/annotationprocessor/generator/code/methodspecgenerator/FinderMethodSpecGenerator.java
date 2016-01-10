package com.forsuredb.annotationprocessor.generator.code.methodspecgenerator;

import com.forsuredb.annotationprocessor.generator.code.CodeUtil;
import com.forsuredb.annotationprocessor.generator.code.JavadocInfo;
import com.forsuredb.annotationprocessor.info.ColumnInfo;
import com.forsuredb.api.Between;
import com.forsuredb.api.Finder;
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
        }

        return new EmptyGenerator(column);
    }

    public final List<MethodSpec> generate(TypeName[] parameterClasses) {
        if (column == null || Strings.isNullOrEmpty(column.getQualifiedType()) || Strings.isNullOrEmpty(column.getColumnName())) {
            return Collections.emptyList();
        }

        final String methodNamePrefix = "by" + CodeUtil.snakeToCamel(column.getColumnName(), true);
        final ParameterizedTypeName conjunctionType = ParameterizedTypeName.get(ClassName.get(Finder.Conjunction.class), parameterClasses);
        final ParameterizedTypeName betweenType = ParameterizedTypeName.get(ClassName.get(Between.class), parameterClasses);

        List<MethodSpec> retList = new ArrayList<>();
        if (hasBeforeAfterGrammar()) {
            retList.addAll(beforeAfterMethodSpecs(methodNamePrefix, conjunctionType, betweenType));
        }
        if (hasIsIsNotGrammar()) {
            retList.addAll(isIsNotMethodSpecs(methodNamePrefix, conjunctionType, betweenType));
        }
        if (hasGreaterThanLessThanGrammar()) {
            retList.addAll(greaterThanLessThanMethodSpecs(methodNamePrefix, conjunctionType, betweenType));
        }
        if (hasLikeGrammar()) {
            retList.add(createSpec(conjunctionType, methodNamePrefix + "Like", "like", Finder.Operator.LIKE));
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
    protected abstract boolean hasGreaterThanLessThanGrammar();
    protected abstract boolean hasLikeGrammar();

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

    private List<MethodSpec> isIsNotMethodSpecs(String methodNamePrefix, ParameterizedTypeName conjunctionType, ParameterizedTypeName betweenType) {
        return Lists.newArrayList(
                createSpec(conjunctionType, methodNamePrefix + "Is", "exactMatch", Finder.Operator.EQ),
                createSpec(conjunctionType, methodNamePrefix + "IsNot", "exclusion", Finder.Operator.NE)
        );
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
        JavadocInfo jd = JavadocInfo.builder()
                .startParagraph()
                .addLine("add criteria to a query that requires $L for $L", parameterName, column.getColumnName())
                .endParagraph()
                .addLine()
                .build();
        MethodSpec.Builder codeBuilder = MethodSpec.methodBuilder(methodName)
                .addJavadoc(jd.stringToFormat(), jd.replacements())
                .addParameter(CodeUtil.typeFromName(column.getQualifiedType()), parameterName)
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType)
                .addStatement("addToBuf($S, Finder.Operator.$L, $L)", column.getColumnName(), op.name(), parameterName);

        if (returnType.rawType.simpleName().equals("Between")) {
            codeBuilder.addStatement("return createBetween($L.class, $S)", column.getQualifiedType(), column.getColumnName());
        } else {
            codeBuilder.addStatement("return conjunction");
        }

        return codeBuilder.build();
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
        protected boolean hasGreaterThanLessThanGrammar() {
            return false;
        }

        @Override
        protected boolean hasLikeGrammar() {
            return false;
        }
    }
}
