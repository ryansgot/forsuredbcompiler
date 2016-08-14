package com.fsryan.forsuredb.annotationprocessor.generator.code.methodspecgenerator;

import com.fsryan.forsuredb.api.info.ColumnInfo;

/*package*/ class StringFinderMethodGenerator extends FinderMethodSpecGenerator {
    public StringFinderMethodGenerator(ColumnInfo column) {
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
