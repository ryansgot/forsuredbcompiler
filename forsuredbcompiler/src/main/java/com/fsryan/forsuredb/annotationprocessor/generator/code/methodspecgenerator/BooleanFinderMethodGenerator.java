package com.fsryan.forsuredb.annotationprocessor.generator.code.methodspecgenerator;

import com.fsryan.forsuredb.api.info.ColumnInfo;

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

    /**
     * <p>
     *     Because a boolean is either either an integer of value 0 (false) or 1 (true)
     *     in our representation, the parameterName is ignored.
     * </p>
     * @param parameterName not used
     * @return the literal code used to replace the '?' in the query will always be 1 because the
     * query will be '... boolean_column = ?...' for isBooleanColumn() and '... boolean_column != ?...'
     * for isNotBooleanColumn()
     */
    @Override
    protected String translateParameter(String parameterName) {
        return "1";
    }
}
