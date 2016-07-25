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

import com.fsryan.forsuredb.annotations.FSStaticData;
import com.fsryan.forsuredb.annotations.FSTable;
import com.google.common.collect.ImmutableMap;
import com.google.gson.annotations.SerializedName;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import lombok.Getter;

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
    public static final Map<String, ColumnInfo> DEFAULT_COLUMNS = new ImmutableMap.Builder<String, ColumnInfo>()
            .put("_id", ColumnInfo.builder().columnName("_id")
                    .methodName("id")
                    .primaryKey(true)
                    .qualifiedType("long")
                    .build())
            .put("created", ColumnInfo.builder().columnName("created")
                    .methodName("created")
                    .qualifiedType("java.util.Date")
                    .defaultValue("CURRENT_TIMESTAMP")
                    .build())
            .put("modified", ColumnInfo.builder().columnName("modified")
                    .methodName("modified")
                    .qualifiedType("java.util.Date")
                    .defaultValue("CURRENT_TIMESTAMP")
                    .build())
            .put("deleted", ColumnInfo.builder().columnName("deleted")
                    .methodName("deleted")
                    .qualifiedType("boolean")
                    .defaultValue("0")
                    .build())
            .build();

    @Getter @SerializedName("column_info_map") private final Map<String, ColumnInfo> columnMap;
    @Getter @SerializedName("table_name") private final String tableName;
    @Getter @SerializedName("qualified_class_name") private final String qualifiedClassName;
    @Getter @SerializedName("static_data_asset") private final String staticDataAsset;
    @Getter @SerializedName("static_data_record_name") private final String staticDataRecordName;

    private TableInfo(Map<String, ColumnInfo> columnMap,
                      String tableName,
                      String qualifiedClassName,
                      String staticDataAsset,
                      String staticDataRecordName) {
        this.tableName = createTableName(tableName, qualifiedClassName);
        this.qualifiedClassName = qualifiedClassName;

        this.columnMap = new HashMap<>();
        this.columnMap.putAll(DEFAULT_COLUMNS);
        if (columnMap != null) {
            this.columnMap.putAll(columnMap);
        }

        this.staticDataAsset = staticDataAsset;
        this.staticDataRecordName = staticDataRecordName;
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
        List<ColumnInfo> retList = new LinkedList<>();
        for (ColumnInfo column : getColumns()) {
            if (column.isForeignKey()) {
                retList.add(column);
            }
        }
        Collections.sort(retList);
        return retList;
    }

    public List<ColumnInfo> getNonForeignKeyColumns() {
        List<ColumnInfo> retList = new LinkedList<>();
        for (ColumnInfo column : getColumns()) {
            if (!column.isForeignKey()) {
                retList.add(column);
            }
        }
        Collections.sort(retList);
        return retList;
    }

    private String createTableName(String tableName, String qualifiedClassName) {
        if (tableName != null && !tableName.isEmpty()) {
            return tableName;
        }
        String[] split = qualifiedClassName.split("\\.");
        return split[split.length - 1];
    }
}
