package com.fsryan.forsuredb.api.info;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;

import java.util.List;

@lombok.ToString
@lombok.EqualsAndHashCode
@lombok.Builder(builderClassName = "Builder")
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class JoinInfo {

    @Getter @SerializedName("parent_table") private final TableInfo parentTable;
    @Getter @SerializedName("parent_columns") private final List<ColumnInfo> parentColumns;
    @Getter @SerializedName("child_table") private final TableInfo childTable;
    @Getter @SerializedName("child_columns") private final List<ColumnInfo> childColumns;

    public boolean isValid() {
        return parentTable != null
                && parentColumns != null
                && !parentColumns.isEmpty()
                && childTable != null
                && childColumns != null
                && !childColumns.isEmpty();
    }
}
