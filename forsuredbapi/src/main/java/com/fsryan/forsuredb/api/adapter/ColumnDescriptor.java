package com.fsryan.forsuredb.api.adapter;

import java.lang.reflect.Type;

@lombok.Data
@lombok.AllArgsConstructor
/*package*/ class ColumnDescriptor {
    private final String columnName;
    private final Type type;
}
