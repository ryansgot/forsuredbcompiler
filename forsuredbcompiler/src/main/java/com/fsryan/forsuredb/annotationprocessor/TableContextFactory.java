package com.fsryan.forsuredb.annotationprocessor;

import com.fsryan.forsuredb.annotationprocessor.util.Pair;
import com.fsryan.forsuredb.api.migration.MigrationRetriever;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.migration.Migration;
import com.fsryan.forsuredb.migration.MigrationSet;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class TableContextFactory {

    /*
            // TODO: use TableContext.Builder
        Map<String, TableInfo.Builder> tableBuilderMap = new HashMap<>();
        Map<String, ColumnInfo.Builder> columnBuilderMap = new HashMap<>();
        for (MigrationSet migrationSet : mr.getMigrationSets()) {
            for (Migration m : migrationSet.orderedMigrations()) {
                final TableInfo table = migrationSet.targetSchema().get(m.tableName());
                update(table, m, tableBuilderMap, columnBuilderMap);
            }
        }

        for (Map.Entry<String, Map<String, ColumnInfo>> entry : createTableBuilderKeyToColumnMapMap(columnBuilderMap).entrySet()) {
            TableInfo.Builder tb = tableBuilderMap.get(entry.getKey());
            TableInfo temp = tb.build();
            Map<String, ColumnInfo> previousColumnMap = temp.columnMap();    // <-- cannot overwrite previous
            previousColumnMap.putAll(entry.getValue());
            tb.columnMap(previousColumnMap);
        }

        Map<String, TableInfo> retMap = new HashMap<>();
        for (Map.Entry<String, TableInfo.Builder> entry : tableBuilderMap.entrySet()) {
            retMap.put(entry.getKey(), entry.getValue().build());
        }

        return retMap;
     */


    @Nonnull
    public static TableContext createFromMigrationRetriever(MigrationRetriever mr) {
        return createfromMigrations(mr == null ? null : mr.getMigrationSets());
    }

    @Nonnull
    public static TableContext createfromMigrations(List<MigrationSet> migrationSets) {
        TableContext.Builder builder = new TableContext.Builder();
        if (migrationSets == null || migrationSets.size() < 1) {
            return builder.build();
        }
        migrationSets.stream()
                .flatMap(streamMigrations())
                .forEach(applyMigration(builder));
        return builder.build();
    }

    private static Consumer<Pair<Migration, Map<String, TableInfo>>> applyMigration(TableContext.Builder builder) {
        return migrationTargetSchemaPair -> {
            final Migration m = migrationTargetSchemaPair.first;
            final Map<String, TableInfo> targetSchema = migrationTargetSchemaPair.second;
            final TableInfo table = targetSchema.get(m.tableName());
            switch (m.type()) {
                case CREATE_TABLE:
                    builder.addTable(table.tableName(), table.qualifiedClassName(), table.toBuilderCompat());
                    break;
//                case UPDATE_PRIMARY_KEY:
//                    handleUpdatePrimaryKey(table, m, tableBuilderMap);
//                    for (String primaryKeyColumnName : table.getPrimaryKey()) {
//                        final String columnKey = columnKey(table.tableName(), primaryKeyColumnName);
//                        final ColumnInfo column = table.getColumn(primaryKeyColumnName);
//                        columnBuilderMap.put(columnKey, column.toBuilder());
//                    }
//                    break;
//                case ADD_FOREIGN_KEY_REFERENCE:
//                    // intentionaly falling through
//                case UPDATE_FOREIGN_KEYS:
//                    handleUpdateForeignKeys(table, m, tableBuilderMap);
//                    for (TableForeignKeyInfo foreignKey : table.foreignKeys()) {
//                        for (String columnName : foreignKey.localToForeignColumnMap().keySet()) {
//                            columnBuilderMap.put(columnKey(table.tableName(), columnName), table.getColumn(columnName).toBuilder());
//                        }
//                    }
//                    break;
//                case CHANGE_DEFAULT_VALUE:
//                    // intentionally falling through
//                case ALTER_TABLE_ADD_UNIQUE:
//                    // intentionally falling through
//                case MAKE_COLUMN_UNIQUE:
//                    // intentionally falling through
//                case ALTER_TABLE_ADD_COLUMN:
//                    columnBuilderMap.put(columnKey(m), table.getColumn(m.columnName()).toBuilder());
//                    break;
//                case ADD_INDEX:
//                    columnBuilderMap.put(columnKey(m), table.getColumn(m.columnName()).toBuilder().index(true));
//                    break;
//                default:
//                    APLog.w(LOG_TAG, "Not handling update of type " + m.type() + "; this could cause the migration context to misrepresent the existing schema.");
            }
        };
    }

    private static Function<MigrationSet, Stream<Pair<Migration, Map<String, TableInfo>>>> streamMigrations() {
        return migrationSet -> migrationSet.orderedMigrations()
                .stream()
                .map(m -> new Pair<>(m, migrationSet.targetSchema()));
    }
}
