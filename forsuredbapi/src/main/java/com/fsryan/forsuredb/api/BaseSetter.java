package com.fsryan.forsuredb.api;

import com.fsryan.forsuredb.api.adapter.SaveResultFactory;
import com.fsryan.forsuredb.api.sqlgeneration.Sql;

import java.text.DateFormat;
import java.util.List;

public abstract class BaseSetter<U, R extends RecordContainer, S extends BaseSetter<U, R, S>> {

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

    public S id(long id) {
        recordContainer.put("_id", id);
        return (S) this;
    }

    public S deleted(boolean deleted) {
        recordContainer.put("deleted", deleted ? 1 : 0);
        return (S) this;
    }

    /**
     * <p>Performs either an insertion or an update of the fields you have set.
     * The operation will be an . . .
     * <ul>
     *   <li>
     *     Insertion if you either did not set any selection criteria or the
     *     search criteria you specified matches no records.
     *   </li>
     *   <li>
     *     Update if you both set selection criteria and that criteria matches
     *     at least one record.
     *   </li>
     * </ul>
     * @return A descriptor of the result of the save operation
     */
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

    /**
     * <p>Attempts an update to the database. However, any fields that you've
     * set prior to calling this method will not be saved.
     * @return A descriptor of the result of the softDelete operation
     */
    public SaveResult<U> softDelete() {
        recordContainer.clear();
        deleted(true);
        try {
            int rowsAffected = queryable.update(recordContainer, selection, orderings);
            return SaveResultFactory.create(null, rowsAffected, null);
        } catch (Exception e) {
            return SaveResultFactory.create(null, 0, e);
        } finally {
            recordContainer.clear();
        }
    }

    /**
     * <p>A hard delete actually deletes the record(s) from the database. If
     * there is a foreign key pointing to any of the matching records, then the
     * {@link com.fsryan.forsuredb.annotations.ForeignKey.ChangeAction} will be
     * executed.
     * @return the number of rows deleted
     */
    public int hardDelete() {
        try {
            return queryable.delete(selection, orderings);
        } catch (Exception e) {
            return 0;
        } finally {
            recordContainer.clear();
        }
    }
}
