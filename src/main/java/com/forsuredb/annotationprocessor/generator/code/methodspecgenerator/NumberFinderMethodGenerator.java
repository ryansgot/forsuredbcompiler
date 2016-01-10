package com.forsuredb.annotationprocessor.generator.code.methodspecgenerator;

import com.forsuredb.annotationprocessor.info.ColumnInfo;

/*package*/ class NumberFinderMethodGenerator extends FinderMethodSpecGenerator {
    public NumberFinderMethodGenerator(ColumnInfo column) {
        super(column);
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
    protected boolean hasGreaterThanLessThanGrammar() {
        return true;
    }

    @Override
    protected boolean hasLikeGrammar() {
        return false;
    }
}
