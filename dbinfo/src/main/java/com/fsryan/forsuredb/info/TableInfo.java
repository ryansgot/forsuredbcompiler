/*
    forsuredb-dbinfo, value classes for the forsuredb project

    Copyright 2017 Ryan Scott

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
package com.fsryan.forsuredb.info;

import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;


/**
 * <p>The complete description of a table. Each table will have the
 * {@link #defaultColumns()} default columns}. You should call the builder()
 * method in order to begin building a TableInfo instance.
 * @author Ryan Scott
 */
@AutoValue
public abstract class TableInfo {

    public static final String DEFAULT_PRIMARY_KEY_COLUMN = "_id";

    /**
     * <p>
     *     By default, each table has the following columns:
     * </p>
     * <ul>
     *     <li>_id: an integer primary key</li>
     *     <li>created: a datetime describing when the record was created</li>
     *     <li>modified: a datetime describing when the record was last modified</li>
     *     <li>deleted: an integer (either 0 or 1) describing whether the record is deleted</li>
     * </ul>
     */

    @AutoValue.Builder
    public static abstract class Builder {

        private final Map<String, ColumnInfo> columnMap = new HashMap<>();
        private final Set<TableForeignKeyInfo> foreignKeys = new HashSet<>();
        private final Set<String> primaryKey = new HashSet<>();

        public abstract Builder tableName(String tableName);                                            // table_name
        public abstract Builder qualifiedClassName(@Nonnull String qualifiedClassName);                 // qualified_class_name
        public abstract Builder staticDataAsset(@Nullable String staticDataAsset);                      // static_data_asset
        public abstract Builder staticDataRecordName(@Nullable String staticDataRecordName);            // static_data_record_name
        public abstract Builder docStoreParameterization(@Nullable String docStoreParameterization);    // doc_store_parameterization
        public abstract Builder primaryKeyOnConflict(@Nullable String primaryKeyOnConflict);            // primary_key_on_conflict

        public Builder addColumn(ColumnInfo column) {
            if (column != null) {
                columnMap.put(column.methodName(), column);
            }
            return this;
        }

        public Builder addAllColumns(Collection<ColumnInfo> columns) {
            if (columns != null) {
                for (ColumnInfo c : columns) {
                    addColumn(c);
                }
            }
            return this;
        }

        public Builder clearColumns() {
            columnMap.clear();
            return this;
        }

        public Builder addForeignKey(TableForeignKeyInfo tfki) {
            if (tfki != null) {
                foreignKeys.add(tfki);
            }
            return this;
        }

        public Builder addAllForeignKeys(Collection<TableForeignKeyInfo> tfkis) {
            if (tfkis != null) {
                foreignKeys.addAll(tfkis);
            }
            return this;
        }

        public Builder resetPrimaryKey(Set<String> primaryKey) {
            this.primaryKey.clear();
            if (primaryKey != null) {
                this.primaryKey.addAll(primaryKey);
            }
            return this;
        }

        /**
         * <p>Maintains compatibility with old schemas. This will go away in
         * major version 1.0.0
         */
        public TableInfo build() {
            // This nasty code preserves backwards compatibility.
            // primary key properties were serialized on columns pre 0.11.0
            // in actuality, primary keys are table properties.
            Set<String> actualPrimaryKey = new HashSet<>();
            if (!primaryKey.isEmpty()) {
                actualPrimaryKey.addAll(primaryKey);
            } else if (!columnMap.isEmpty()) {
                for (ColumnInfo column : columnMap.values()) {
                    if (column.primaryKey()) {
                        actualPrimaryKey.add(column.getColumnName());
                    }
                }
            }
            if (actualPrimaryKey.isEmpty()) {
                actualPrimaryKey.add(DEFAULT_PRIMARY_KEY_COLUMN);
            }

            // This nasty code preserves backwards compatibility.
            // foreign key properties were serialized on columns pre 0.11.0
            // in actuality, primary keys are table properties.
            // TODO: remove this code when no longer necessary
            Set<TableForeignKeyInfo> actualForeignKeys = new HashSet<>();
            if (!foreignKeys.isEmpty()) {
                actualForeignKeys.addAll(foreignKeys);
            } else if (!columnMap.isEmpty()) {
                for (ColumnInfo column : columnMap.values()) {
                    ForeignKeyInfo legacyForeignKey = column.foreignKeyInfo();
                    if (legacyForeignKey == null) {
                        continue;
                    }
                    actualForeignKeys.add(TableForeignKeyInfo.builder()
                            .foreignTableApiClassName(legacyForeignKey.apiClassName())
                            .foreignTableName(legacyForeignKey.tableName())
                            .mapLocalToForeignColumn(column.getColumnName(), legacyForeignKey.columnName())
                            .updateChangeAction(legacyForeignKey.updateAction())
                            .deleteChangeAction(legacyForeignKey.deleteAction())
                            .build());
                }
            }

            for (Map.Entry<String, ColumnInfo> defaultColumnEntry : defaultColumns().entrySet()) {
                ColumnInfo toBuildColumn = columnMap.get(defaultColumnEntry.getKey());
                if (toBuildColumn == null) {
                    columnMap.put(defaultColumnEntry.getKey(), defaultColumnEntry.getValue());
                }
            }
            return tableName(createTableName(tableName(), qualifiedClassName()))
                    .columnMap(columnMap)
                    .foreignKeys(actualForeignKeys)
                    .primaryKey(actualPrimaryKey)
                    .autoBuild();
        }

        // should only be invoked in build()
        abstract Builder columnMap(Map<String, ColumnInfo> columnMap);                                  // column_info_map
        abstract Builder foreignKeys(@Nonnull Set<TableForeignKeyInfo> foreignKeys);                    // foreign_keys
        abstract Builder primaryKey(@Nonnull Set<String> primaryKey);                                   // primary_key
        abstract TableInfo autoBuild();

        // for internal access at build time
        abstract String tableName();
        abstract String qualifiedClassName();
    }

    public static Builder builder() {
        return new AutoValue_TableInfo.Builder();
    }

    public static Set<String> defaultColumnNames() {
        return new HashSet<>(Arrays.asList("_id", "created", "deleted", "modified"));
    }

    public static Map<String, ColumnInfo> defaultColumns() {
        Map<String, ColumnInfo> ret = new HashMap<>(4);
        ret.put("id", ColumnInfo.builder()
                .columnName("_id")
                .methodName("id")
                .primaryKey(true)
                .qualifiedType("long")
                .build());
        ret.put("created", ColumnInfo.builder()
                .columnName("created")
                .methodName("created")
                .qualifiedType(Date.class.getName())
                .defaultValue("CURRENT_TIMESTAMP")
                .build());
        ret.put("modified", ColumnInfo.builder()
                .columnName("modified")
                .methodName("modified")
                .qualifiedType(Date.class.getName())
                .defaultValue("CURRENT_TIMESTAMP")
                .build());
        ret.put("deleted", ColumnInfo.builder()
                .columnName("deleted")
                .methodName("deleted")
                .qualifiedType("boolean")
                .defaultValue("0")
                .build());
        return ret;
    }

    public static Map<String, ColumnInfo> docStoreColumns() {
        HashMap<String, ColumnInfo> ret = new HashMap<>(3);
        ret.put("className", ColumnInfo.builder()
                .methodName("className")
                .qualifiedType(String.class.getName())
                .columnName("class_name")
                .build());
        ret.put("doc", ColumnInfo.builder()
                .methodName("doc")
                .qualifiedType(String.class.getName())
                .columnName("doc")
                .build());
        ret.put("blobDoc", ColumnInfo.builder()
                .methodName("blobDoc")
                .qualifiedType(byte[].class.getCanonicalName())
                .columnName("blob_doc")
                .build());
        return ret;
    }

    @Nonnull public abstract Map<String, ColumnInfo> columnMap();  // column_info_map
    public abstract String tableName(); //table_name
    public abstract String qualifiedClassName();    // qualified_class_name
    @Nullable public abstract String staticDataAsset();   // static_data_asset
    @Nullable public abstract String staticDataRecordName();  // static_data_record_name
    @Nullable public abstract String docStoreParameterization();  // doc_store_parameterization
    public abstract Set<String> primaryKey();   // primary_key
    @Nullable public abstract String primaryKeyOnConflict();  // primary_key_on_conflict
    @Nullable public abstract Set<TableForeignKeyInfo> foreignKeys(); // foreign_keys

    public Builder toBuilder() {
        return autoToBuilder()
                .addAllColumns(columnMap().values())
                .addAllForeignKeys(foreignKeys())
                .resetPrimaryKey(primaryKey());
    }

    public boolean isValid() {
        return (qualifiedClassName() != null && !qualifiedClassName().isEmpty()) || (tableName() != null && !tableName().isEmpty());
    }

    public String getSimpleClassName() {
        final String qName = qualifiedClassName();
        if (qName == null || qName.isEmpty()) {
            return "";
        }

        int dotIdx = qName.lastIndexOf('.');
        return dotIdx < 0 ? qName : qName.substring(dotIdx + 1);
    }

    public String getPackageName() {
        final String qName = qualifiedClassName();
        if (qName == null || qName.isEmpty()) {
            return "";
        }

        int dotIdx = qName.lastIndexOf('.');
        return dotIdx < 0 ? qName : qName.substring(0, dotIdx);
    }

    public boolean isDocStore() {
        return docStoreParameterization() != null;
    }

    /**
     * <p>Note that the accuracy of this method depends upon how it was created. For example,
     * if the {@link TableInfo} was created from some serialized object, then the return will
     * be the state at the time the object was serialized rather than at the time the object
     * is used.
     * @return true if this table has static data, false otherwise
     */
    public boolean hasStaticData() {
        return staticDataAsset() != null && !staticDataAsset().isEmpty();
    }

    public boolean hasColumn(String columnName) {
        return getColumn(columnName) != null;
    }

    @Nullable
    public ColumnInfo getColumn(String columnName) {
        if (columnName == null) {
            return null;
        }

        for (ColumnInfo column : getColumns()) {
            if (columnName.equals(column.getColumnName())) {
                return column;
            }
        }
        return null;
    }

    public Collection<ColumnInfo> getColumns() {
        return columnMap().values();
    }

    public List<ColumnInfo> getForeignKeyColumns() {
        List<ColumnInfo> retList = new ArrayList<>();
        for (ColumnInfo column : getColumns()) {
            if (isForeignKeyColumn(column)) {
                retList.add(column);
            }
        }
        Collections.sort(retList);
        return retList;
    }

    public List<ColumnInfo> getNonForeignKeyColumns() {
        List<ColumnInfo> retList = new ArrayList<>();
        for (ColumnInfo column : getColumns()) {
            if (!isForeignKeyColumn(column)) {
                retList.add(column);
            }
        }
        Collections.sort(retList);
        return retList;
    }

    public Set<String> getPrimaryKey() {
        if (primaryKey() != null && !primaryKey().isEmpty()) {
            return new HashSet<>(primaryKey());
        }

        Set<String> ret = new HashSet<>();
        for (ColumnInfo column : getColumns()) {
            if (DEFAULT_PRIMARY_KEY_COLUMN.equals(column.getColumnName())) {
                continue;
            }
            if (column.primaryKey()) {
                ret.add(column.getColumnName());
            }
        }
        if (ret.isEmpty()) {
            ret.add(DEFAULT_PRIMARY_KEY_COLUMN);
        }
        return ret;
    }

    public boolean referencesOtherTable() {
        return (foreignKeys() != null && !foreignKeys().isEmpty()) || !getForeignKeyColumns().isEmpty();
    }

    public boolean isForeignKeyColumn(String columnName) {
        return isForeignKeyColumn(getColumn(columnName));
    }

    public TableForeignKeyInfo getForeignKeyForColumn(String columnName) {
        if (foreignKeys() == null) {
            return null;
        }
        for (TableForeignKeyInfo foreignKey : foreignKeys()) {
            if (foreignKey.localToForeignColumnMap().keySet().contains(columnName)) {
                return foreignKey;
            }
        }
        return null;
    }

    abstract Builder autoToBuilder();

    private boolean isForeignKeyColumn(ColumnInfo column) {
        if (column == null) {
            return false;
        }
        if (column.isForeignKey()) {
            return true;
        }
        if (foreignKeys() == null || foreignKeys().isEmpty()) {
            return false;
        }
        for (TableForeignKeyInfo foreignKey : foreignKeys()) {
            if (foreignKey.localToForeignColumnMap().containsKey(column.getColumnName())) {
                return true;
            }
        }
        return false;
    }

    private static String createTableName(String tableName, String qualifiedClassName) {
        if (tableName != null && !tableName.isEmpty()) {
            return tableName;
        }
        String[] split = qualifiedClassName.split("\\.");
        return split[split.length - 1];
    }
}
