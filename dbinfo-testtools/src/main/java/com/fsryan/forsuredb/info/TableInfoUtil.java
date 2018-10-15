package com.fsryan.forsuredb.info;

public abstract class TableInfoUtil {

    public static String DEFAULT_TABLE_NAME = "default";
    public static String DEFAULT_QUALIFIED_CLASS_NAME = TableInfoUtil.class.getName();

    public static TableInfo.BuilderCompat defaultBuilder() {
        return TableInfo.builder()
                .tableName(DEFAULT_TABLE_NAME)
                .qualifiedClassName(DEFAULT_QUALIFIED_CLASS_NAME);
    }
}
