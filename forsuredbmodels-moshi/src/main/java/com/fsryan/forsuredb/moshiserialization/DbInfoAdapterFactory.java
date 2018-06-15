package com.fsryan.forsuredb.moshiserialization;

import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.ForeignKeyInfo;
import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.migration.Migration;
import com.fsryan.forsuredb.migration.MigrationSet;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

class DbInfoAdapterFactory implements JsonAdapter.Factory {
    @Nullable
    @Override
    public JsonAdapter<?> create(Type type, Set<? extends Annotation> annotations, Moshi moshi) {

        if (ColumnInfo.class.equals(type)) {
            return new ColumnInfoAdapter(moshi);
        }
        if (TableInfo.class.equals(type)) {
            return new TableInfoAdapter(moshi);
        }
        if (Migration.class.equals(type)) {
            return new MigrationAdapter(moshi);
        }
        if (TableForeignKeyInfo.class.equals(type)) {
            return new TableForeignKeyInfoAdapter(moshi);
        }
        if (MigrationSet.class.equals(type)) {
            return new MigrationSetAdapter(moshi);
        }
        if (ForeignKeyInfo.class.equals(type)) {
            return new ForeignKeyInfoAdapter();
        }
        return null;
    }
}
