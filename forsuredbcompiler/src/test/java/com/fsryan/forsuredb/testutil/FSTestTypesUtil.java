package com.fsryan.forsuredb.testutil;

import com.fsryan.forsuredb.annotations.*;
import com.fsryan.forsuredb.api.FSDocStoreGetApi;
import com.fsryan.forsuredb.api.FSGetApi;

import java.lang.annotation.Annotation;

public abstract class FSTestTypesUtil {

    public static FSDefault createFSDefault(String value) {
        return new FSDefault() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return FSDefault.class;
            }

            @Override
            public String value() {
                return value;
            }
        };
    }

    public static FSTable createFSTable(String value) {
        return new FSTable() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return FSTable.class;
            }

            @Override
            public String value() {
                return value;
            }
        };
    }

    public static FSPrimaryKey createFSPrimaryKey(final String onConflict, final String... primaryKey) {
        return new FSPrimaryKey() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return FSPrimaryKey.class;
            }

            @Override
            public String[] value() {
                return primaryKey;
            }

            @Override
            public String onConflict() {
                return onConflict;
            }
        };
    }

    public static FSStaticData createFSStaticData(final String asset) {
        return new FSStaticData() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return FSStaticData.class;
            }

            @Override
            public String value() {
                return asset;
            }
        };
    }

    public static ForeignKey createLegacyForeignKey(final Class<? extends FSGetApi> foreignTableCls, final String foreignColumn, ForeignKey.ChangeAction updateAction, ForeignKey.ChangeAction deleteAction) {
        return new ForeignKey() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return ForeignKey.class;
            }

            @Override
            public Class<? extends FSGetApi> apiClass() {
                return foreignTableCls;
            }

            @Override
            public String columnName() {
                return foreignColumn;
            }

            @Override
            public ChangeAction updateAction() {
                return updateAction;
            }

            @Override
            public ChangeAction deleteAction() {
                return deleteAction;
            }
        };
    }

    public static FSForeignKey createFSForeignKey(
            final Class<? extends FSGetApi> foreignTableCls,
            final String foreignColumn,
            final String updateAction,
            final String deleteAction,
            final String compositeId) {
        return new FSForeignKey() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return FSForeignKey.class;
            }

            @Override
            public Class<? extends FSGetApi> apiClass() {
                return foreignTableCls;
            }

            @Override
            public String columnName() {
                return foreignColumn;
            }

            @Override
            public String compositeId() {
                return compositeId;
            }

            @Override
            public String updateAction() {
                return updateAction;
            }

            @Override
            public String deleteAction() {
                return deleteAction;
            }
        };
    }

    public static Index createLegacyIndex(final boolean unique) {
        return new Index() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return Index.class;
            }

            @Override
            public boolean unique() {
                return unique;
            }
        };
    }

    public static FSIndex createFSIndex(final boolean unique, final String sortOrder, final String compositeId) {
        return new FSIndex() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return FSIndex.class;
            }

            @Override
            public boolean unique() {
                return unique;
            }

            @Override
            public String compositeId() {
                return compositeId;
            }

            @Override
            public String sortOrder() {
                return sortOrder;
            }
        };
    }

    public static TestTypeMirror docStoreGetApi() {
        return TestTypeMirror.forName(FSDocStoreGetApi.class.getName());
    }
}
