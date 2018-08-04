package com.fsryan.forsuredb.annotationprocessor.generator.code.methodspecgenerator;

import com.fsryan.forsuredb.info.ColumnInfo;
import com.squareup.javapoet.ParameterizedTypeName;

class IsIsNotOnlyFinderMethodGenerator extends FinderMethodSpecGenerator {

    private final boolean allowMultipleExactMatches;

    public IsIsNotOnlyFinderMethodGenerator(ColumnInfo column, ParameterizedTypeName conjuntionTypeName, ParameterizedTypeName betweenTypeName) {
        this(column, conjuntionTypeName, betweenTypeName, false);
    }

    public IsIsNotOnlyFinderMethodGenerator(ColumnInfo column, ParameterizedTypeName conjuntionTypeName, ParameterizedTypeName betweenTypeName, boolean allowMultipleExactMatches) {
        super(column, conjuntionTypeName, betweenTypeName);
        this.allowMultipleExactMatches = allowMultipleExactMatches;
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
        return true;
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

    @Override
    protected boolean allowMultipleExactMatches() {
        return allowMultipleExactMatches;
    }

    @Override
    protected String translateParameter(String parameterName) {
        return allowMultipleExactMatches ? parameterName : "1";
    }
}
