package com.fsryan.forsuredb.api;

import com.fsryan.forsuredb.api.adapter.SaveResultFactory;
import com.fsryan.forsuredb.api.sqlgeneration.Sql;

import java.text.DateFormat;
import java.util.List;

public abstract class BaseSetter<U, R extends RecordContainer> implements FSSaveApi<U> {

    protected DateFormat dateFormat;
    private final FSQueryable<U, R> queryable;
    private final FSSelection selection;
    private final List<FSOrdering> orderings;
    protected final R recordContainer;

    public BaseSetter(FSQueryable<U, R> queryable,
                      FSSelection selection,
                      List<FSOrdering> orderings,
                      R recordContainer) {
        this(Sql.generator().getDateFormat(), queryable, selection, orderings, recordContainer);
    }

    // intended for use in testing
    protected BaseSetter(DateFormat dateFormat,
               FSQueryable<U, R> queryable,
               FSSelection selection,
               List<FSOrdering> orderings,
               R recordContainer) {
        this.dateFormat = dateFormat;
        this.queryable = queryable;
        this.selection = selection;
        this.orderings = orderings;
        this.recordContainer = recordContainer;
        this.recordContainer.clear();
    }

    @Override
    public SaveResult<U> save() {
        try {
            if (selection == null) {
                final U inserted = queryable.insert(recordContainer);
                return SaveResultFactory.create(inserted, inserted == null ? 0 : 1, null);
            }
            return queryable.upsert(recordContainer, selection, orderings);
        } catch (Exception e) {
            return SaveResultFactory.create(null, 0, e);
        } finally {
            recordContainer.clear();
        }
    }

    @Override
    public SaveResult<U> softDelete() {
        recordContainer.clear();
        recordContainer.put("deleted", 1);
        try {
            int rowsAffected = queryable.update(recordContainer, selection, orderings);
            return SaveResultFactory.create(null, rowsAffected, null);
        } finally {
            recordContainer.clear();
        }
    }

    @Override
    public int hardDelete() {
        try {
            return queryable.delete(selection, orderings);
        } finally {
            recordContainer.clear();
        }
    }
}
