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

import javax.annotation.Nullable;
import java.util.*;


/**
 * <p>
 *     The complete description of a table. Each table will have the
 *     {@link #DEFAULT_COLUMNS default columns}. You should call the builder()
 *     method in order to begin building a TableInfo instance.
 * </p>
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
    /*package*/ static final Map<String, ColumnInfo> DEFAULT_COLUMNS = new HashMap<>();
    static {
        DEFAULT_COLUMNS.put("_id", ColumnInfo.builder().columnName("_id")
                .methodName("id")
                .primaryKey(true)
                .qualifiedType("long")
                .build());
        DEFAULT_COLUMNS.put("created", ColumnInfo.builder().columnName("created")
                .methodName("created")
                .qualifiedType(Date.class.getName())
                .defaultValue("CURRENT_TIMESTAMP")
                .build());
        DEFAULT_COLUMNS.put("modified", ColumnInfo.builder().columnName("modified")
                .methodName("modified")
                .qualifiedType(Date.class.getName())
                .defaultValue("CURRENT_TIMESTAMP")
                .build());
        DEFAULT_COLUMNS.put("deleted", ColumnInfo.builder().columnName("deleted")
                .methodName("deleted")
                .qualifiedType("boolean")
                .defaultValue("0")
                .build());
    }

    /*package*/ static final Map<String, ColumnInfo> DOC_STORE_COLUMNS = new HashMap<>();
    static {
        DOC_STORE_COLUMNS.put("class_name", ColumnInfo.builder()
                .methodName("className")
                .qualifiedType(String.class.getName())
                .columnName("class_name")
                .build());
        DOC_STORE_COLUMNS.put("doc", ColumnInfo.builder()
                .methodName("doc")
                .qualifiedType(String.class.getName())
                .columnName("doc")
                .build());
        DOC_STORE_COLUMNS.put("blob_doc", ColumnInfo.builder()
                .methodName("blobDoc")
                .qualifiedType(byte[].class.getCanonicalName())
                .columnName("blob_doc")
                .build());
    }

    @AutoValue.Builder
    public static abstract class Builder {
        public abstract Builder columnMap(Map<String, ColumnInfo> columnMap);  // column_info_map
        public abstract Builder tableName(String tableName); //table_name
        public abstract Builder qualifiedClassName(String qualifiedClassName);    // qualified_class_name
        public abstract Builder staticDataAsset(@Nullable String staticDataAsset);   // static_data_asset
        public abstract Builder staticDataRecordName(@Nullable String staticDataRecordName);  // static_data_record_name
        public abstract Builder docStoreParameterization(@Nullable String docStoreParameterization);  // doc_store_parameterization
        public abstract Builder primaryKey(Set<String> primaryKey);   // primary_key
        public abstract Builder primaryKeyOnConflict(@Nullable String primaryKeyOnConflict);  // primary_key_on_conflict
        public abstract Builder foreignKeys(@Nullable Set<TableForeignKeyInfo> foreignKeys); // foreign_keys
        public abstract TableInfo build();
    }

    /**
     * <p>Builder for compatibility with previous old schemas. This will go away in major version 1.0.0
     */
    public static class BuilderCompat {

        private BuilderCompat() {}

        private String tableName;
        private String qualifiedClassName;
        private Map<String, ColumnInfo> columnMap = new HashMap<>();
        private Set<String> primaryKey = new HashSet<>();
        private Set<TableForeignKeyInfo> foreignKeys = new HashSet<>();
        private final Builder builder = new AutoValue_TableInfo.Builder();

        public BuilderCompat columnMap(Map<String, ColumnInfo> columnMap) {
            this.columnMap.clear();
            if (columnMap != null) {
                this.columnMap.putAll(columnMap);
            }
            return this;
        }

        public BuilderCompat tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public BuilderCompat qualifiedClassName(String qualifiedClassName) {
            this.qualifiedClassName = qualifiedClassName;
            return this;
        }

        public BuilderCompat staticDataAsset(@Nullable String staticDataAsset) {
            builder.staticDataAsset(staticDataAsset);
            return this;
        }

        public BuilderCompat staticDataRecordName(@Nullable String staticDataRecordName) {
            builder.staticDataRecordName(staticDataRecordName);
            return this;
        }

        public BuilderCompat docStoreParameterization(@Nullable String docStoreParameterization) {
            builder.docStoreParameterization(docStoreParameterization);
            return this;
        }

        public BuilderCompat primaryKey(Set<String> primaryKey) {
            this.primaryKey.clear();
            if (primaryKey != null) {
                this.primaryKey.addAll(primaryKey);
            }
            return this;
        }

        public BuilderCompat primaryKeyOnConflict(@Nullable String primaryKeyOnConflict) {
            builder.primaryKeyOnConflict(primaryKeyOnConflict);
            return this;
        }

        public BuilderCompat foreignKeys(@Nullable Set<TableForeignKeyInfo> foreignKeys) {
            this.foreignKeys.clear();
            if (foreignKeys != null) {
                this.foreignKeys.addAll(foreignKeys);
            }
            return this;
        }

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
                    Map<String, String> localToForeignColumnMap = new HashMap<>(1);
                    localToForeignColumnMap.put(column.getColumnName(), legacyForeignKey.columnName());
                    actualForeignKeys.add(TableForeignKeyInfo.builder()
                            .foreignTableApiClassName(legacyForeignKey.apiClassName())
                            .foreignTableName(legacyForeignKey.tableName())
                            .localToForeignColumnMap(localToForeignColumnMap)
                            .updateChangeAction(legacyForeignKey.updateAction())
                            .deleteChangeAction(legacyForeignKey.deleteAction())
                            .build());
                }
            }

            columnMap.putAll(DEFAULT_COLUMNS);
            return builder.tableName(createTableName(tableName, qualifiedClassName))
                    .columnMap(columnMap)
                    .qualifiedClassName(qualifiedClassName)
                    .foreignKeys(actualForeignKeys)
                    .primaryKey(actualPrimaryKey)
                    .build();
        }
    }

    public static BuilderCompat builder() {
        return new BuilderCompat();
    }

    @Nullable public abstract Map<String, ColumnInfo> columnMap();  // column_info_map
    public abstract String tableName(); //table_name
    public abstract String qualifiedClassName();    // qualified_class_name
    @Nullable public abstract String staticDataAsset();   // static_data_asset
    @Nullable public abstract String staticDataRecordName();  // static_data_record_name
    @Nullable public abstract String docStoreParameterization();  // doc_store_parameterization
    public abstract Set<String> primaryKey();   // primary_key
    @Nullable public abstract String primaryKeyOnConflict();  // primary_key_on_conflict
    @Nullable public abstract Set<TableForeignKeyInfo> foreignKeys(); // foreign_keys
    public abstract Builder toBuilder();

    public static Set<String> defaultColumnNames() {
        return DEFAULT_COLUMNS.keySet();
    }

    public static Map<String, ColumnInfo> defaultColumns() {
        return new HashMap<>(DEFAULT_COLUMNS);
    }

    public static Map<String, ColumnInfo> docStoreColumns() {
        return new HashMap<>(DOC_STORE_COLUMNS);
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
        return columnMap().containsKey(columnName);
    }

    public ColumnInfo getColumn(String columnName) {
        return columnName == null ? null : columnMap().get(columnName);
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
