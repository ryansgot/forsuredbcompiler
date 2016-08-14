package com.fsryan.forsuredb.api.info;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;

@lombok.ToString
@lombok.EqualsAndHashCode
@lombok.Builder(builderClassName = "Builder")
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class JoinInfo {

    @Getter @SerializedName("parent_table") private final TableInfo parentTable;
    @Getter @SerializedName("parent_column") private final ColumnInfo parentColumn;
    @Getter @SerializedName("child_table") private final TableInfo childTable;
    @Getter @SerializedName("child_column") private final ColumnInfo childColumn;

    public boolean isValid() {
        return parentTable != null && parentColumn != null && childTable != null && childColumn != null;
    }
}
