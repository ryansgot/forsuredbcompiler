/*
   forsuredbcompiler, an annotation processor and code generator for the forsuredb project

   Copyright 2015 Ryan Scott

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
package com.fsryan.forsuredb.api.info;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;

import java.util.*;

/**
 * <p>
 *     The complete description of a table. Each table will have the
 *     {@link #DEFAULT_COLUMNS default columns}. You should call the builder()
 *     method in order to begin building a TableInfo instance.
 * </p>
 * @author Ryan Scott
 */
@lombok.ToString
@lombok.EqualsAndHashCode
@lombok.Builder(builderClassName = "Builder")
public class TableInfo {

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
    public static final Map<String, ColumnInfo> DEFAULT_COLUMNS = new HashMap<>();
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

    public static final Map<String, ColumnInfo> DOC_STORE_COLUMNS = new HashMap<>();
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

    @Getter @SerializedName("column_info_map") private final Map<String, ColumnInfo> columnMap;
    @Getter @SerializedName("table_name") private final String tableName;
    @Getter @SerializedName("qualified_class_name") private final String qualifiedClassName;
    @Getter @SerializedName("static_data_asset") private final String staticDataAsset;
    @Getter @SerializedName("static_data_record_name") private final String staticDataRecordName;
    @Getter @SerializedName("doc_store_parameterization") private final String docStoreParameterization;
    @SerializedName("primary_key") private final Set<String> primaryKey;
    @Getter @SerializedName("primary_key_on_conflict") private final String primaryKeyOnConflict;
    @Getter @SerializedName("foreign_keys") private final Set<TableForeignKeyInfo> foreignKeys;

    private TableInfo(Map<String, ColumnInfo> columnMap,
                      String tableName,
                      String qualifiedClassName,
                      String staticDataAsset,
                      String staticDataRecordName,
                      String docStoreParameterization,
                      Set<String> primaryKey,
                      String primaryKeyOnConflict,
                      Set<TableForeignKeyInfo> foreignKeys) {
        this.tableName = createTableName(tableName, qualifiedClassName);
        this.qualifiedClassName = qualifiedClassName;

        this.columnMap = new HashMap<>();
        this.columnMap.putAll(DEFAULT_COLUMNS);
        if (columnMap != null) {
            this.columnMap.putAll(columnMap);
        }

        this.staticDataAsset = staticDataAsset;
        this.staticDataRecordName = staticDataRecordName;
        this.docStoreParameterization = docStoreParameterization;

        // This nasty code preserves backwards compatibility.
        // primary key properties were serialized on columns pre 0.11.0
        // in actuality, primary keys are table properties.
        // TODO: remove this code when no longer necessary
        this.primaryKey = new HashSet<>();
        if (primaryKey != null && !primaryKey.isEmpty()) {
            this.primaryKey.addAll(primaryKey);
        } else if (columnMap != null) {
            for (ColumnInfo column : columnMap.values()) {
                if (column.isPrimaryKey()) {
                    this.primaryKey.add(column.getColumnName());
                }
            }
        } else {
            this.primaryKey.add(DEFAULT_PRIMARY_KEY_COLUMN);
        }
        this.primaryKeyOnConflict = primaryKeyOnConflict;

        // This nasty code preserves backwards compatibility.
        // foreign key properties were serialized on columns pre 0.11.0
        // in actuality, primary keys are table properties.
        // TODO: remove this code when no longer necessary
        this.foreignKeys = new HashSet<>();
        if (foreignKeys != null && !foreignKeys.isEmpty()) {
            this.foreignKeys.addAll(foreignKeys);
        } else if (columnMap != null) {
            for (ColumnInfo column : columnMap.values()) {
                ForeignKeyInfo legacyForeignKey = column.getForeignKeyInfo();
                if (legacyForeignKey == null) {
                    continue;
                }
                this.foreignKeys.add(new TableForeignKeyInfo.Builder()
                        .foreignTableApiClassName(legacyForeignKey.getApiClassName())
                        .foreignTableName(legacyForeignKey.getTableName())
                        .mapLocalToForeignColumn(column.getColumnName(), legacyForeignKey.getColumnName())
                        .updateChangeAction(legacyForeignKey.getUpdateAction().toString())
                        .deleteChangeAction(legacyForeignKey.getDeleteAction().toString())
                        .build());
            }
        }
    }

    public boolean isValid() {
        return (qualifiedClassName != null && !qualifiedClassName.isEmpty()) || (tableName != null && !tableName.isEmpty());
    }

    public String getSimpleClassName() {
        if (qualifiedClassName == null || qualifiedClassName.isEmpty()) {
            return "";
        }
        String[] split = qualifiedClassName.split("\\.");
        return split[split.length - 1];
    }

    public String getPackageName() {
        if (qualifiedClassName == null || qualifiedClassName.isEmpty()) {
            return null;
        }
        String[] split = qualifiedClassName.split("\\.");
        StringBuffer buf = new StringBuffer(split[0]);
        for (int i = 1; i < split.length - 1; i++) {
            buf.append(".").append(split[i]);
        }
        return buf.toString();
    }

    public boolean isDocStore() {
        return docStoreParameterization != null;
    }

    public boolean hasStaticData() {
        return staticDataAsset != null
                && !staticDataAsset.isEmpty()
                && staticDataRecordName != null
                && !staticDataRecordName.isEmpty();
    }

    public boolean hasColumn(String columnName) {
        return columnMap.containsKey(columnName);
    }

    public ColumnInfo getColumn(String columnName) {
        return columnName == null ? null : columnMap.get(columnName);
    }

    public Collection<ColumnInfo> getColumns() {
        return columnMap.values();
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
        if (primaryKey != null && !primaryKey.isEmpty()) {
            return new HashSet<>(primaryKey);
        }

        Set<String> ret = new HashSet<>();
        for (ColumnInfo column : getColumns()) {
            if (DEFAULT_PRIMARY_KEY_COLUMN.equals(column.getColumnName())) {
                continue;
            }
            if (column.isPrimaryKey()) {
                ret.add(column.getColumnName());
            }
        }
        if (ret.isEmpty()) {
            ret.add(DEFAULT_PRIMARY_KEY_COLUMN);
        }
        return ret;
    }

    public boolean referencesOtherTable() {
        return (foreignKeys != null && !foreignKeys.isEmpty()) || !getForeignKeyColumns().isEmpty();
    }

    public boolean isForeignKeyColumn(String columnName) {
        return isForeignKeyColumn(getColumn(columnName));
    }

    public TableForeignKeyInfo getForeignKeyForColumn(String columnName) {
        if (foreignKeys == null) {
            return null;
        }
        for (TableForeignKeyInfo foreignKey : foreignKeys) {
            if (foreignKey.getLocalToForeignColumnMap().keySet().contains(columnName)) {
                return foreignKey;
            }
        }
        return null;
    }

    public Builder newBuilder() {
        return builder()
                .tableName(tableName)
                .foreignKeys(foreignKeys)
                .primaryKey(primaryKey)
                .primaryKeyOnConflict(primaryKeyOnConflict)
                .staticDataAsset(staticDataAsset)
                .staticDataRecordName(staticDataRecordName)
                .columnMap(columnMap)
                .docStoreParameterization(docStoreParameterization)
                .qualifiedClassName(qualifiedClassName);
    }

    private boolean isForeignKeyColumn(ColumnInfo column) {
        if (column == null) {
            return false;
        }
        if (column.isForeignKey()) {
            return true;
        }
        if (foreignKeys == null || foreignKeys.isEmpty()) {
            return false;
        }
        for (TableForeignKeyInfo foreignKey : foreignKeys) {
            if (foreignKey.getLocalToForeignColumnMap().containsKey(column.getColumnName())) {
                return true;
            }
        }
        return false;
    }

    private String createTableName(String tableName, String qualifiedClassName) {
        if (tableName != null && !tableName.isEmpty()) {
            return tableName;
        }
        String[] split = qualifiedClassName.split("\\.");
        return split[split.length - 1];
    }
}
