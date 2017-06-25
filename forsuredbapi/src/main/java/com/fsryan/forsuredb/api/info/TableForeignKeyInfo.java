package com.fsryan.forsuredb.api.info;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *     An alternative means of serializing foreign keys on the table. The old way,
 *     {@link ForeignKeyInfo} gets serialized on columns. The old way was inflexible
 *     because it did not allow for composite keys--at least not in any sensible way.
 * </p>
 */
@lombok.ToString
@lombok.EqualsAndHashCode
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class TableForeignKeyInfo {

    @Getter @SerializedName("foreign_table_api_class_name") private final String foreignTableApiClassName;
    @Getter @Setter @SerializedName("foreign_table_name") private String foreignTableName;
    @Getter @SerializedName("local_to_foreign_column_map") private final Map<String, String> localToForeignColumnMap;
    @Getter @SerializedName("update_action") private final String updateChangeAction;
    @Getter @SerializedName("delete_action") private final String deleteChangeAction;

    public Builder newBuilder() {
        return new Builder()
                .foreignTableApiClassName(foreignTableApiClassName)
                .foreignTableName(foreignTableName)
                .addAllLocalToForeignColumns(localToForeignColumnMap)
                .updateChangeAction(updateChangeAction)
                .deleteChangeAction(deleteChangeAction);
    }

    public static class Builder {

        private String foreignTableApiClassName;
        private String foreignTableName;
        private final Map<String, String> localToForeignColumnMap = new HashMap<>();
        private String updateChangeAction;
        private String deleteChangeAction;

        public Builder() {}

        public Builder foreignTableApiClassName(String foreignTableApiClassName) {
            this.foreignTableApiClassName = foreignTableApiClassName;
            return this;
        }

        public Builder foreignTableName(String foreignTableName) {
            this.foreignTableName = foreignTableName;
            return this;
        }

        public Builder addAllLocalToForeignColumns(Map<String, String> localToForeignColumnMap) {
            if (localToForeignColumnMap != null) {
                this.localToForeignColumnMap.putAll(localToForeignColumnMap);
            }
            return this;
        }

        public Builder mapLocalToForeignColumn(String localColumn, String foreignColumn) {
            if (localColumn == null || localColumn.isEmpty() || foreignColumn == null || foreignColumn.isEmpty()) {
                throw new IllegalArgumentException("cannot handle null/empty local column '" + localColumn + "' or foreign column '" + foreignColumn + "'");
            }
            localToForeignColumnMap.put(localColumn, foreignColumn);
            return this;
        }

        public Builder updateChangeAction(String updateChangeAction) {
            this.updateChangeAction = updateChangeAction;
            return this;
        }

        public Builder deleteChangeAction(String deleteChangeAction) {
            this.deleteChangeAction = deleteChangeAction;
            return this;
        }

        public TableForeignKeyInfo build() {
            return new TableForeignKeyInfo(foreignTableApiClassName,
                    foreignTableName,
                    localToForeignColumnMap,
                    updateChangeAction,
                    deleteChangeAction);
        }
    }
}
