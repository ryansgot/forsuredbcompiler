package com.fsryan.forsuredb.annotationprocessor.generator.code.methodspecgenerator;

import com.fsryan.forsuredb.api.info.ColumnInfo;

/*package*/ class NumberFinderMethodGenerator<C, B> extends FinderMethodSpecGenerator<C, B> {
    public NumberFinderMethodGenerator(ColumnInfo column, Class<C> conjuntionClass, Class<B> betweenClass) {
        super(column, conjuntionClass, betweenClass);
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
