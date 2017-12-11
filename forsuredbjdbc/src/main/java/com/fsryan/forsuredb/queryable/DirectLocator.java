package com.fsryan.forsuredb.queryable;

import com.fsryan.forsuredb.api.FSJoin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DirectLocator {

    public final String table;
    public final long id;

    private List<FSJoin> joins;

    public DirectLocator(@Nonnull String table) {
        this(table, 0L);
    }

    public DirectLocator(@Nonnull String table, long id) {
        this.table = table;
        this.id = id;
    }

    public void addJoins(@Nullable List<FSJoin> joins) {
        if (joins == null) {
            return;
        }
        if (this.joins == null) {
            this.joins = new ArrayList<>(joins.size());
        }
        this.joins.addAll(joins);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DirectLocator that = (DirectLocator) o;

        if (id != that.id) return false;
        if (!table.equals(that.table)) return false;
        return getJoins().equals(that.getJoins());
    }

    @Override
    public int hashCode() {
        int result = table.hashCode();
        result = 31 * result + (int) (id ^ (id >>> 32));
        result = 31 * result + getJoins().hashCode();
        return result;
    }

    @Nonnull
    public List<FSJoin> getJoins() {
        return joins == null ? Collections.emptyList() : joins;
    }

    public boolean forSpecficRecord() {
        return id > 0;
    }
}
