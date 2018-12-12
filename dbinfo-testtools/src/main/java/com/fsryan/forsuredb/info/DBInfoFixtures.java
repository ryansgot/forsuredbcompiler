package com.fsryan.forsuredb.info;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static com.fsryan.forsuredb.test.tools.CollectionUtil.setOf;

public abstract class DBInfoFixtures {

    /**
     * @param tableName the name of the table
     * @param columns the additional columns ot add
     * @return a {@link TableInfo.Builder} set up with the default columns and
     * all of the columns passed in. The primary key is the default primary
     * key. It uses the table name passed in as well as the default qualified
     * class name.
     * @see TableInfoUtil#tableFQClassName(String)
     * @see TableInfo#DEFAULT_PRIMARY_KEY_COLUMN
     */
    public static TableInfo.Builder tableBuilder(String tableName, ColumnInfo... columns) {
        return TableInfo.builder()
                .tableName(tableName)
                .qualifiedClassName(TableInfoUtil.tableFQClassName(tableName))
                .resetPrimaryKey(setOf(TableInfo.DEFAULT_PRIMARY_KEY_COLUMN))
                .addColumn(ColumnInfoUtil.idCol())
                .addColumn(ColumnInfoUtil.createdCol())
                .addColumn(ColumnInfoUtil.deletedCol())
                .addColumn(ColumnInfoUtil.modifiedCol())
                .addAllColumns(Arrays.asList(columns));
    }

    /**
     * <p>Legacy implementations used to set non-composite primary key on a
     * single column only.
     * @param tableName the name of the table
     * @param columns any additional columns to add to the table
     * @return the default {@link TableInfo.Builder} that builds a default
     * {@link TableInfo} in the legacy condition with _id as the primary key
     * @see #tableBuilder(String, ColumnInfo...)
     */
    public static TableInfo.Builder legacyPKTableBuilder(String tableName, ColumnInfo... columns) {
        return tableBuilder(tableName, columns)
                .resetPrimaryKey(Collections.emptySet());
    }

    // ColumnInfo

    public static ColumnInfo.Builder booleanCol() {
        return colBuilder(boolean.class);
    }

    public static ColumnInfo.Builder bigDecimalCol() {
        return colBuilder(BigDecimal.class);
    }

    public static ColumnInfo.Builder bigIntegerCol() {
        return colBuilder(BigInteger.class);
    }

    public static ColumnInfo.Builder dateCol() {
        return colBuilder(Date.class);
    }

    public static ColumnInfo.Builder doubleCol() {
        return colBuilder(double.class);
    }

    public static ColumnInfo.Builder floatCol() {
        return colBuilder(float.class);
    }

    public static ColumnInfo.Builder intCol() {
        return colBuilder(int.class);
    }

    public static ColumnInfo.Builder longCol() {
        return colBuilder(long.class);
    }

    public static ColumnInfo.Builder stringCol() {
        return colBuilder(String.class);
    }

    /**
     * <p>Returns
     * @param cls
     * @return a {@link ColumnInfo.Builder} of for a column of the type passed
     * in with the default name for that type. The method name is also the
     * default method name for that type.
     * @see ColumnInfoUtil#defaultColNameMap
     * @see ColumnInfoUtil#defaultMethodNameMap
     */
    public static ColumnInfo.Builder colBuilder(Class cls) {
        return ColumnInfo.builder()
                .columnName(ColumnInfoUtil.colNameByType(cls))
                .qualifiedType(cls.getName())
                .orderable(true)
                .searchable(true)
                .methodName(ColumnInfoUtil.colMethodNameByType(cls));
    }

    // ForeignKeyInfo

    public static ForeignKeyInfo idCascadeFKI(@Nonnull String foreignKeyTableName) {
        return cascadeFKI(foreignKeyTableName, "_id").build();
    }

    public static ForeignKeyInfo.Builder cascadeFKI(@Nonnull String foreignKeyTableName, @Nonnull String foreignColumnName) {
        return fki(foreignKeyTableName)
                .updateAction("CASCADE")
                .deleteAction("CASCADE")
                .columnName(foreignColumnName);
    }

    public static ForeignKeyInfo idNoActionFKI(@Nonnull String foreignKeyTableName) {
        return noActionFKI(foreignKeyTableName, "_id").build();
    }

    public static ForeignKeyInfo.Builder noActionFKI(@Nonnull String foreignKeyTableName, @Nonnull String foreignColumnName) {
        return fki(foreignKeyTableName)
                .updateAction("NO ACTION")
                .deleteAction("NO ACTION")
                .columnName(foreignColumnName);
    }

    public static ForeignKeyInfo idSetNullFKI(@Nonnull String foreignKeyTableName) {
        return setNullFKI(foreignKeyTableName, "_id").build();
    }

    public static ForeignKeyInfo.Builder setNullFKI(@Nonnull String foreignKeyTableName, @Nonnull String foreignColumnName) {
        return fki(foreignKeyTableName)
                .updateAction("SET NULL")
                .deleteAction("SET NULL")
                .columnName(foreignColumnName);
    }

    public static ForeignKeyInfo idSetDefaultFKI(@Nonnull String foreignKeyTableName) {
        return setDefaultFKI(foreignKeyTableName, "_id").build();
    }

    public static ForeignKeyInfo.Builder setDefaultFKI(@Nonnull String foreignKeyTableName, @Nonnull String foreignColumnName) {
        return fki(foreignKeyTableName)
                .updateAction("SET DEFAULT")
                .deleteAction("SET DEFAULT")
                .columnName(foreignColumnName);
    }

    public static ForeignKeyInfo idRestrictFKI(@Nonnull String foreignKeyTableName) {
        return restrictFKI(foreignKeyTableName, "_id").build();
    }

    public static ForeignKeyInfo.Builder restrictFKI(@Nonnull String foreignKeyTableName, @Nonnull String foreignColumnName) {
        return fki(foreignKeyTableName)
                .updateAction("RESTRICT")
                .deleteAction("RESTRICT")
                .columnName(foreignColumnName);
    }

    public static ForeignKeyInfo.Builder fki(@Nonnull String foreignKeyTableName) {
        return ForeignKeyInfo.builder()
                .tableName(foreignKeyTableName)
                .apiClassName(TableInfoUtil.tableFQClassName(foreignKeyTableName));
    }

    // TableForeignKeyInfo

    /**
     * @param foreignTableName the name of the referenced table
     * @return a {@link TableForeignKeyInfo.Builder} that will fail to build
     * unless you call
     * {@link TableForeignKeyInfo.Builder#localToForeignColumnMap(Map)} on it
     * first.
     */
    public static TableForeignKeyInfo.Builder foreignKeyTo(String foreignTableName) {
        return TableForeignKeyInfo.builder()
                .foreignTableName(foreignTableName)
                .foreignTableApiClassName(TableInfoUtil.tableFQClassName(foreignTableName))
                .updateChangeAction("")
                .deleteChangeAction("");
    }

    /**
     * @param foreignTableName the name of the referenced table
     * @return a {@link TableForeignKeyInfo.Builder} whose update and change
     * actions are both CASCADE
     */
    public static TableForeignKeyInfo.Builder cascadeForeignKeyTo(String foreignTableName) {
        return TableForeignKeyInfo.builder()
                .foreignTableName(foreignTableName)
                .foreignTableApiClassName(TableInfoUtil.tableFQClassName(foreignTableName))
                .updateChangeAction("CASCADE")
                .deleteChangeAction("CASCADE");
    }
}