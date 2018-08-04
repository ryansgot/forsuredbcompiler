package com.fsryan.forsuredb.queryable;

import com.fsryan.forsuredb.api.RecordContainer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ApiCorrections {

    /**
     * <p>Handles the empty record insertion possibility by synthesizing an insertion
     * with the default deleted value. This avoids common problems for underlying
     * layers.
     *
     * @param record The {@link RecordContainer} of the record to insert
     * @return a guaranteed nonempty list of columns, possibly updating the record if empty
     */
    public static List<String> correctColumnsForInsert(@Nonnull RecordContainer record) {
        if (record.keySet().isEmpty()) {
            record.put("deleted", 0);
            return Collections.singletonList("deleted");
        }
        return new ArrayList<>(record.keySet());
    }
}
