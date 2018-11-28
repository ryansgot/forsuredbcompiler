package com.fsryan.forsuredb.migration;

public class MigrationFixtures {

    public static Migration.Builder migration(Migration.Type type) {
        return Migration.builder().type(type);
    }

    public static Migration createTableMigration(String tableName) {
        return migration(Migration.Type.CREATE_TABLE).tableName(tableName).build();
    }

    public static Migration.Builder addColumnMigration(String tableName) {
        return migration(Migration.Type.ALTER_TABLE_ADD_COLUMN).tableName(tableName);
    }

    public static Migration.Builder addIndexMigration(String tableName) {
        return migration(Migration.Type.ADD_INDEX).tableName(tableName);
    }

    public static Migration.Builder addForeignKeyReferenceMigration(String tableName) {
        return migration(Migration.Type.ADD_FOREIGN_KEY_REFERENCE).tableName(tableName);
    }

    public static Migration.Builder updateForeignKeysMigration(String tableName) {
        return migration(Migration.Type.UPDATE_FOREIGN_KEYS).tableName(tableName);
    }

    public static Migration.Builder updatePrimaryKeyMigration(String tableName) {
        return migration(Migration.Type.UPDATE_PRIMARY_KEY).tableName(tableName);
    }

    public static Migration.Builder changeDefaultValueMigration(String tableName) {
        return migration(Migration.Type.CHANGE_DEFAULT_VALUE).tableName(tableName);
    }

    public static Migration.Builder makeColumnUniqueMigration(String tableName) {
        return migration(Migration.Type.MAKE_COLUMN_UNIQUE).tableName(tableName);
    }

    public static Migration.Builder addUniqueIndexMigration(String tableName) {
        return migration(Migration.Type.ADD_UNIQUE_INDEX).tableName(tableName);
    }
}
