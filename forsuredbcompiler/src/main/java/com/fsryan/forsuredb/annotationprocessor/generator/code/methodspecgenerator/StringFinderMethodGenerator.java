package com.fsryan.forsuredb.annotationprocessor.generator.code.methodspecgenerator;

import com.fsryan.forsuredb.info.ColumnInfo;
import com.squareup.javapoet.ParameterizedTypeName;

/*package*/ class StringFinderMethodGenerator extends FinderMethodSpecGenerator {
    public StringFinderMethodGenerator(ColumnInfo column, ParameterizedTypeName conjunctionTypeName, ParameterizedTypeName betweenTypeName) {
        super(column, conjunctionTypeName, betweenTypeName);
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
        return true;
    }
}
