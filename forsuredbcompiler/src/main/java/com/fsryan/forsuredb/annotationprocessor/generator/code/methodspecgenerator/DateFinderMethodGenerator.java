package com.fsryan.forsuredb.annotationprocessor.generator.code.methodspecgenerator;

import com.fsryan.forsuredb.info.ColumnInfo;
import com.squareup.javapoet.ParameterizedTypeName;

/*package*/ class DateFinderMethodGenerator extends FinderMethodSpecGenerator {
    public DateFinderMethodGenerator(ColumnInfo column, ParameterizedTypeName conjunctionTypeName, ParameterizedTypeName betweenTypeName) {
        super(column, conjunctionTypeName, betweenTypeName);
    }

    @Override
    protected boolean hasBeforeAfterGrammar() {
        return true;
    }

    @Override
    protected boolean hasBetweenGrammar() {
        return true;
    }

    @Override
    protected boolean hasIsIsNotGrammar() {
        return false;
    }

    @Override
    protected boolean hasOnNotOnGrammar() {
        return true;
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
