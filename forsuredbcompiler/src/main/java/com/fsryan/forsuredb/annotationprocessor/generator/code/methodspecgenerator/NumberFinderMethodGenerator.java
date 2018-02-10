package com.fsryan.forsuredb.annotationprocessor.generator.code.methodspecgenerator;

import com.fsryan.forsuredb.info.ColumnInfo;
import com.squareup.javapoet.ParameterizedTypeName;

/*package*/ class NumberFinderMethodGenerator extends FinderMethodSpecGenerator {
    public NumberFinderMethodGenerator(ColumnInfo column, ParameterizedTypeName conjuntionTypeName, ParameterizedTypeName betweenTypeName) {
        super(column, conjuntionTypeName, betweenTypeName);
    }

    @Override
    protected boolean hasBeforeAfterGrammar() {
        return false;
    }

    @Override
    protected boolean hasBetweenGrammar() {
        return true;
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
        return true;
    }

    @Override
    protected boolean hasLikeGrammar() {
        return false;
    }
}
