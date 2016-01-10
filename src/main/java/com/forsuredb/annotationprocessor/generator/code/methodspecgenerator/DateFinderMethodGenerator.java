package com.forsuredb.annotationprocessor.generator.code.methodspecgenerator;

import com.forsuredb.annotationprocessor.info.ColumnInfo;

/*package*/ class DateFinderMethodGenerator extends FinderMethodSpecGenerator {
    public DateFinderMethodGenerator(ColumnInfo column) {
        super(column);
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
