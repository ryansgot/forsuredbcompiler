package com.fsryan.forsuredb.annotationprocessor.generator.code.methodspecgenerator;

import com.fsryan.forsuredb.api.info.ColumnInfo;

/*package*/ class DateFinderMethodGenerator<C, B> extends FinderMethodSpecGenerator<C, B> {
    public DateFinderMethodGenerator(ColumnInfo column, Class<C> conjuntionClass, Class<B> betweenClass) {
        super(column, conjuntionClass, betweenClass);
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
