package com.forsuredb.annotationprocessor.generator.code.methodspecgenerator;

import com.forsuredb.annotationprocessor.info.ColumnInfo;

/*package*/ class BooleanFinderMethodGenerator extends FinderMethodSpecGenerator {
    public BooleanFinderMethodGenerator(ColumnInfo column) {
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
}
