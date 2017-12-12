package com.fsryan.forsuredb.sqlitelib;

import com.fsryan.forsuredb.api.FSProjection;
import com.fsryan.forsuredb.api.sqlgeneration.DBMSIntegrator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/*package*/ class ProjectionHelper {

    private final DBMSIntegrator sqlGenerator;

    ProjectionHelper(DBMSIntegrator sqlGenerator) {
        this.sqlGenerator = sqlGenerator;
    }

    public boolean isDistinct(@Nonnull Iterable<FSProjection> projections) {
        for (FSProjection projection : projections) {
            if (projection.isDistinct()) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public String[] formatProjection(@Nullable FSProjection projection, @Nullable FSProjection... projections) {
        final int size = (projection == null ? 0 : 1) + (projections == null ? 0 : projections.length);
        if (size == 0) {
            return null;
        }

        List<FSProjection> ps = new ArrayList<>(size);
        if (projection != null) {
            ps.add(projection);
        }
        if (projections != null) {
            for (FSProjection p : projections) {
                ps.add(p);
            }
        }

        return formatProjection(ps);
    }

    @Nullable
    public String[] formatProjection(@Nullable List<FSProjection> projections) {
        if (projections == null || projections.size() == 0) {
            return null;
        }
        List<String> formattedProjectionList = new ArrayList<>();
        for (FSProjection projection : projections) {
            appendProjectionToList(formattedProjectionList, projection);
        }
        return formattedProjectionList.toArray(new String[formattedProjectionList.size()]);
    }

    private void appendProjectionToList(List<String> listToAddTo, FSProjection projection) {
        if (projection == null || projection.columns() == null || projection.columns().length == 0) {
            return;
        }

        for (String column : projection.columns()) {
            final String unambiguousName = sqlGenerator.unambiguousColumn(projection.tableName(), column);
            final String retrievalName = sqlGenerator.unambiguousRetrievalColumn(projection.tableName(), column);
            listToAddTo.add(unambiguousName + " AS " + retrievalName);
        }
    }
}
