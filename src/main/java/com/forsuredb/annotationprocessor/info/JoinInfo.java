package com.forsuredb.annotationprocessor.info;

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

    @Getter private final TableInfo parentTable;
    @Getter private final ColumnInfo parentColumn;
    @Getter private final TableInfo childTable;
    @Getter private final ColumnInfo childColumn;

    public boolean isValid() {
        return parentTable != null && parentColumn != null && childTable != null && childColumn != null;
    }
}
