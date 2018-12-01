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
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *     An alternative means of serializing foreign keys on the table. The old way,
 *     {@link ForeignKeyInfo} gets serialized on columns. The old way was inflexible
 *     because it did not allow for composite keys--at least not in any sensible way.
 * </p>
 */
@AutoValue
public abstract class TableForeignKeyInfo {

    public static Builder builder() {
        return new AutoValue_TableForeignKeyInfo.Builder()
                .foreignTableName("");
    }

    // not known until later on . . . so hack to set this value on an otherwise immutable class
    public abstract String foreignTableApiClassName();    // foreign_table_api_class_name
    public abstract String foreignTableName();  // foreign_table_name
    public abstract Map<String, String> localToForeignColumnMap();    // local_to_foreign_column_map
    public abstract String updateChangeAction();    // update_action
    public abstract String deleteChangeAction();    // delete_action

    public Builder toBuilder() {
        return autoToBuilder().addAllLocalToForeignColumns(localToForeignColumnMap());
    }

    abstract Builder autoToBuilder();

    @AutoValue.Builder
    public static abstract class Builder {

        private final Map<String, String> columnNameMap = new HashMap<>(4);

        public abstract Builder foreignTableApiClassName(String foreignTableApiClassName);      // foreign_table_api_class_name
        public abstract Builder foreignTableName(String foreignTableName);                      // foreign_table_name
        public abstract Builder updateChangeAction(String updateAction);                        // update_action
        public abstract Builder deleteChangeAction(String deleteAction);                        // delete_action

        public abstract String foreignTableApiClassName();

        @Nonnull
        public Map<String, String> localToForeignColumnMap() {
            return new HashMap<>(columnNameMap);
        }

        public Builder mapLocalToForeignColumn(@Nonnull String localColumnName, @Nonnull String foreignColumnName) {
            columnNameMap.put(localColumnName, foreignColumnName);
            return this;
        }

        public Builder addAllLocalToForeignColumns(Map<String, String> localToForeignColumnMap) {
            if (localToForeignColumnMap != null) {
                columnNameMap.putAll(localToForeignColumnMap);
            }
            return this;
        }

        public TableForeignKeyInfo build() {
            if (columnNameMap.size() < 1) {
                throw new IllegalStateException("Cannot create " + TableForeignKeyInfo.class + "with empty columnNameMap");
            }
            return localToForeignColumnMap(columnNameMap).autoBuild();
        }

        abstract Builder localToForeignColumnMap(Map<String, String> localToForeignColumnMap);    // local_to_foreign_column_map
        abstract TableForeignKeyInfo autoBuild();
    }
}
