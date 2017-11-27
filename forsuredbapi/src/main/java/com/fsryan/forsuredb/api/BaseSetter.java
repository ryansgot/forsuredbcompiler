package com.fsryan.forsuredb.api;

import com.fsryan.forsuredb.api.adapter.SaveResultFactory;

import java.util.List;

public abstract class BaseSetter<U> implements FSSaveApi<U> {

    private final FSQueryable<U, RecordContainer> queryable;
    private final FSSelection selection;
    private final List<FSOrdering> orderings;
    protected final RecordContainer recordContainer;

    public BaseSetter(FSQueryable<U, RecordContainer> queryable,
                      FSSelection selection,
                      List<FSOrdering> orderings,
                      RecordContainer recordContainer) {
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
        int rowsAffected = queryable.update(recordContainer, selection, orderings);
        return SaveResultFactory.create(null, rowsAffected, null);
    }

    @Override
    public int hardDelete() {
        recordContainer.clear();
        return queryable.delete(selection, orderings);
    }
}
