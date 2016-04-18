package com.forsuredb.annotationprocessor.info;

import com.google.gson.annotations.SerializedName;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Builder(builderClassName = "Builder")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JoinInfo {

    @Getter @SerializedName("parent_table") private final TableInfo parentTable;
    @Getter @SerializedName("parent_column") private final ColumnInfo parentColumn;
    @Getter @SerializedName("child_table") private final TableInfo childTable;
    @Getter @SerializedName("child_column") private final ColumnInfo childColumn;

    public boolean isValid() {
        return parentTable != null && parentColumn != null && childTable != null && childColumn != null;
    }
}
