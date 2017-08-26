package com.fsryan.forsuredb.api.adapter;

import com.fsryan.forsuredb.api.FSOrdering;
import com.fsryan.forsuredb.api.FSQueryable;
import com.fsryan.forsuredb.api.FSSelection;
import com.fsryan.forsuredb.api.RecordContainer;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/*package*/ class RelationalSaveHandler<U, R extends RecordContainer> extends SaveHandler<U, R> {
    public RelationalSaveHandler(
            FSQueryable<U, R> queryable,
            FSSelection selection,
            List<FSOrdering> orderings,
            R recordContainer,
            Map<Method, ColumnDescriptor> columnTypeMap
    ) {
        super(queryable, selection, orderings, recordContainer, columnTypeMap);
    }
}
