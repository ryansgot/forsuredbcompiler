package com.fsryan.forsuredb.annotationprocessor.generator.code.methodspecgenerator;

import com.fsryan.forsuredb.annotationprocessor.generator.code.CodeUtil;
import com.fsryan.forsuredb.annotationprocessor.generator.code.JavadocInfo;
import com.fsryan.forsuredb.api.Conjunction;
import com.fsryan.forsuredb.api.info.ColumnInfo;
import com.fsryan.forsuredb.api.Finder;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.fsryan.forsuredb.api.Finder.*;

public abstract class FinderMethodSpecGenerator {

    private static final Map<Integer, String> opToOpNameMap = new ImmutableMap.Builder<Integer, String>()
            .put(OP_EQ, "OP_EQ")
            .put(OP_GE, "OP_GE")
            .put(OP_GT, "OP_GT")
            .put(OP_LE, "OP_LE")
            .put(OP_LIKE, "OP_LIKE")
            .put(OP_LT, "OP_LT")
            .put(OP_NE, "OP_NE")
            .build();

    private final ColumnInfo column;
    private final ParameterizedTypeName conjunctionTypeName;
    private final ParameterizedTypeName betweenTypeName;

    protected FinderMethodSpecGenerator(ColumnInfo column, ParameterizedTypeName conjunctionTypeName, ParameterizedTypeName betweenTypeName) {
        this.column = column;
        this.conjunctionTypeName = conjunctionTypeName;
        this.betweenTypeName = betweenTypeName;
    }

    public static FinderMethodSpecGenerator create(ColumnInfo column, ParameterizedTypeName conjunctionTypeName, ParameterizedTypeName betweenTypeName) {
        if (column == null || Strings.isNullOrEmpty(column.getQualifiedType()) || Strings.isNullOrEmpty(column.getColumnName())) {
            return new EmptyGenerator(column, conjunctionTypeName, betweenTypeName);
        }

        switch (column.getQualifiedType()) {
            case "java.util.Date":
                return new DateFinderMethodGenerator(column, conjunctionTypeName, betweenTypeName);
            case "java.lang.String":
                return new StringFinderMethodGenerator(column, conjunctionTypeName, betweenTypeName);
            case "java.math.BigDecimal":
                // intentionally fall through
            case "double":
                // intentionally fall through
            case "float":
                // intentionally fall through
            case "long":
                // intentionally fall through
            case "int":
                return new NumberFinderMethodGenerator(column, conjunctionTypeName, betweenTypeName);
            case "boolean":
                return new BooleanFinderMethodGenerator(column, conjunctionTypeName, betweenTypeName);
        }

        return new EmptyGenerator(column, conjunctionTypeName, betweenTypeName);
    }

    public final List<MethodSpec> generate() {
        if (column == null || Strings.isNullOrEmpty(column.getQualifiedType()) || Strings.isNullOrEmpty(column.getColumnName())) {
            return Collections.emptyList();
        }

        final String methodNameInsertion = CodeUtil.snakeToCamel(column.getColumnName(), true);

        List<MethodSpec> retList = new ArrayList<>();
        if (hasBeforeAfterGrammar()) {
            retList.addAll(beforeAfterMethodSpecs("by" + methodNameInsertion));
        }
        if (hasIsIsNotGrammar()) {
            retList.addAll(isIsNotMethodSpecs(methodNameInsertion));
        }
        if (hasOnNotOnGrammar()) {
            retList.add(createSpec(conjunctionTypeName, "by" + methodNameInsertion + "On", "exactMatch", Finder.OP_EQ));
            retList.add(createSpec(conjunctionTypeName, "byNot" + methodNameInsertion + "On", "exclusion", OP_NE));
        }
        if (hasGreaterThanLessThanGrammar()) {
            retList.addAll(greaterThanLessThanMethodSpecs("by" + methodNameInsertion));
        }
        if (hasLikeGrammar()) {
            retList.add(createSpec(conjunctionTypeName, "by" + methodNameInsertion + "Like", "like", Finder.OP_LIKE));
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

    private List<MethodSpec> beforeAfterMethodSpecs(String methodNamePrefix) {
        List<MethodSpec> retList = Lists.newArrayList(
                createSpec(conjunctionTypeName, methodNamePrefix + "Before", "nonInclusiveUpperBound", OP_LT),
                createSpec(conjunctionTypeName, methodNamePrefix + "After", "nonInclusiveLowerBound", OP_GT),
                createSpec(conjunctionTypeName, methodNamePrefix + "BeforeInclusive", "inclusiveUpperBound", OP_LE),
                createSpec(conjunctionTypeName, methodNamePrefix + "AfterInclusive", "inclusiveLowerBound", Finder.OP_GE)
        );

        if (hasBetweenGrammar()) {
            retList.add(createSpec(betweenTypeName, methodNamePrefix + "Between", "nonInclusiveLowerBound", OP_GT));
            retList.add(createSpec(betweenTypeName, methodNamePrefix + "BetweenInclusive", "inclusiveLowerBound", Finder.OP_GE));
        }

        return retList;
    }

    private List<MethodSpec> isIsNotMethodSpecs(String methodNameInsertion) {
        List<MethodSpec> retList = new ArrayList<>();
        retList.add(createSpec(conjunctionTypeName, "by" + methodNameInsertion, "exactMatch", Finder.OP_EQ));
        retList.add(createSpec(conjunctionTypeName,
                column.getQualifiedType().equals("boolean") ? "byNot" + methodNameInsertion : "by" + methodNameInsertion + "Not",
                "exclusion",
                OP_NE));
        return retList;
    }

    private List<MethodSpec> greaterThanLessThanMethodSpecs(String methodNamePrefix) {
        List<MethodSpec> retList = Lists.newArrayList(
                createSpec(conjunctionTypeName, methodNamePrefix + "LessThan", "nonInclusiveUpperBound", OP_LT),
                createSpec(conjunctionTypeName, methodNamePrefix + "GreaterThan", "nonInclusiveLowerBound", OP_GT),
                createSpec(conjunctionTypeName, methodNamePrefix + "LessThanInclusive", "inclusiveUpperBound", OP_LE),
                createSpec(conjunctionTypeName, methodNamePrefix + "GreaterThanInclusive", "inclusiveLowerBound", OP_GE)
        );

        if (hasBetweenGrammar()) {
            retList.add(createSpec(betweenTypeName, methodNamePrefix + "Between", "nonInclusiveLowerBound", OP_GT));
            retList.add(createSpec(betweenTypeName, methodNamePrefix + "BetweenInclusive", "inclusiveLowerBound", OP_GE));
        }

        return retList;
    }

    private MethodSpec createSpec(ParameterizedTypeName returnType, String methodName, String parameterName, int op) {
        JavadocInfo jd = javadocInfoFor(returnType, parameterName);
        MethodSpec.Builder codeBuilder = MethodSpec.methodBuilder(methodName)
                .addJavadoc(jd.stringToFormat(), jd.replacements())
                .addModifiers(Modifier.PUBLIC)
                .addStatement("addToBuf($S, $L, $L)", column.getColumnName(), opToOpNameMap.get(op), translateParameter(parameterName))
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
            jdBuilder.returns("a $L that allows you to provide an upper bound for this criteria", JavadocInfo.inlineClassLink(Finder.Between.class));
        } else {
            jdBuilder.returns("a $L that allows you to continue adding more query criteria", JavadocInfo.inlineClassLink(Conjunction.AndOr.class));
        }
        return jdBuilder.addLine().build();
    }

    private static class EmptyGenerator extends FinderMethodSpecGenerator {

        protected EmptyGenerator(ColumnInfo column, ParameterizedTypeName conjunctionTypeName, ParameterizedTypeName betweenTypeName) {
            super(column, conjunctionTypeName, betweenTypeName);
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
