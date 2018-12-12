package com.fsryan.forsuredb.annotationprocessor.generator;

import com.fsryan.forsuredb.annotationprocessor.TableContext;
import com.fsryan.forsuredb.annotationprocessor.util.Pair;
import com.fsryan.forsuredb.annotationprocessor.util.StreamUtil;
import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.migration.SchemaDiff;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fsryan.forsuredb.annotationprocessor.util.StreamUtil.mapCollector;
import static com.fsryan.forsuredb.annotationprocessor.util.StreamUtil.mapCombiner;

public class SchemaDiffGenerator {

    private static final BinaryOperator<Map<String, SchemaDiff>> diffMapCombiner = (m1, m2) -> {
        Map<String, SchemaDiff> combined = new HashMap<>(m1);
        m2.forEach((k, v) -> {
            SchemaDiff current = combined.get(k);
            if (current != null) {
                throw new IllegalStateException("Cannot combine diffs with same key: '" + k + "': current = " + current + "; to combine = " + v);
            }
            combined.put(k, v);
        });
        return combined;
    };

    private final TableContext base;

    public SchemaDiffGenerator(@Nonnull TableContext base) {
        this.base = base;
    }

    @Nonnull
    public Map<String, SchemaDiff> generate(@Nonnull TableContext target) {
        // create table diffs
//        final Map<String, TableInfo> renamedTables = target.allTables()
//                .stream()
//                .filter(t -> containsConflictingTableName(base, t))
//                .collect(mapCollector((acc, table) -> acc.put(table.qualifiedClassName(), table)));
        final Map<String, TableInfo> newTables = missingTables(base, target);
        final Map<String, TableInfo> droppedTables = missingTables(target, base);
        Stream<Pair<String, SchemaDiff>> existingTableDiffStream = target.tableMap().values()
                .stream()
                .filter(t -> !newTables.containsKey(t.qualifiedClassName()))
                .flatMap(this::generateAllDiffs);


        return StreamUtil.concatAll(
                existingTableDiffStream,
                tableDiffStream(newTables, t -> SchemaDiff.forTableCreated(t.tableName())),
                tableDiffStream(droppedTables, t -> SchemaDiff.forTableDropped(t.tableName()))
        ).collect(Collector.of(
                HashMap::new,
                (Map<String, SchemaDiff> acc, Pair<String, SchemaDiff> pair) -> {
                    final String tableClassName = pair.first();
                    final SchemaDiff diff = pair.second();
                    SchemaDiff current = acc.get(tableClassName);
                    if (current != null) {
                        throw new IllegalStateException("Cannot combine diffs with same key: '" + tableClassName + "': current = " + current + "; to combine = " + diff);
                    }
                    acc.put(tableClassName, diff);
                },
                diffMapCombiner
        ));
    }

    private Stream<Pair<String, SchemaDiff>> generateAllDiffs(TableInfo targetTable) {
        TableInfo baseTable = base.tableMap().get(targetTable.qualifiedClassName());
        if (baseTable == null) {
            throw new IllegalStateException("Expecting base context to contain table at key '" + targetTable.qualifiedClassName() + "'; base: " + base);
        }

        SchemaDiff.Builder builder = SchemaDiff.builder()
                .tableName(targetTable.tableName())
                .addAttribute(SchemaDiff.ATTR_CURR_NAME, targetTable.tableName())
                .type(SchemaDiff.TYPE_CHANGED);

        enrichWithNameChangeDiff(builder, baseTable, targetTable);
        enrichWithPrimaryKeyDiff(builder, baseTable, targetTable);
        enrichWithColumnChangesDiff(builder, baseTable, targetTable);
        SchemaDiff tableDiff = builder.build();
        return tableDiff.isEmpty()
                ? Stream.empty()
                : Stream.of(Pair.create(targetTable.qualifiedClassName(), tableDiff));
    }

    private static void enrichWithColumnChangesDiff(SchemaDiff.Builder builder, TableInfo baseTable, TableInfo targetTable) {
        Map<String, ColumnInfo> newColumns = missingColumns(baseTable.columnMap(), targetTable.columnMap());
        Map<String, ColumnInfo> droppedColumns = missingColumns(targetTable.columnMap(), baseTable.columnMap());

        StringBuilder nameChangeBuf = targetTable.getColumns()
                .stream()
                .filter(c -> !newColumns.containsKey(c.methodName()))
                .map(c -> Pair.create(baseTable.columnMap().get(c.methodName()).getColumnName(), c.getColumnName()))
                .filter(pair -> !pair.first().equals(pair.second()))
                .sorted(Comparator.comparing(Pair::first))
                .collect(Collector.of(
                        StringBuilder::new,
                        (acc, pair) -> acc.append(pair.first()).append('=').append(pair.second()).append(','),
                        (buf1, buf2) -> {
                            if (buf1.length() == 0) {
                                return buf2;
                            }
                            if (buf2.length() == 0) {
                                return buf1;
                            }
                            return buf1.append(buf2);
                        }
                ));

        if (nameChangeBuf.length() != 0) {
            String renameColumns = nameChangeBuf.delete(nameChangeBuf.length() - 1, nameChangeBuf.length()).toString();
            builder.enrichSubType(SchemaDiff.TYPE_RENAME_COLUMNS)
                    .addAttribute(SchemaDiff.ATTR_RENAME_COLUMNS, renameColumns);
        }

        if (!newColumns.isEmpty()) {
            Set<String> newColumnNames = newColumns.values()
                    .stream()
                    .map(ColumnInfo::getColumnName)
                    .collect(Collectors.toSet());
            builder.enrichSubType(SchemaDiff.TYPE_ADD_COLUMNS)
                    .addAttribute(SchemaDiff.ATTR_CREATE_COLUMNS, toCsv(newColumnNames, true));
        }

        if (!droppedColumns.isEmpty()) {
            Set<String> droppedColumnNames = droppedColumns.values()
                    .stream()
                    .map(ColumnInfo::getColumnName)
                    .collect(Collectors.toSet());
            builder.enrichSubType(SchemaDiff.TYPE_DROP_COLUMNS)
                    .addAttribute(SchemaDiff.ATTR_DROP_COLUMNS, toCsv(droppedColumnNames, true));
        }
    }

    private static Map<String, ColumnInfo> missingColumns(Map<String, ColumnInfo> baseColumns, Map<String, ColumnInfo> targetColumns) {
        return targetColumns.keySet()
                .stream()
                .filter(columnMethodName -> !baseColumns.containsKey(columnMethodName))
                .map(columnMethodName -> targetColumns.get(columnMethodName))
                .collect(mapCollector((acc, column) -> acc.put(column.methodName(), column)));
    }

    private static void enrichWithNameChangeDiff(SchemaDiff.Builder builder, TableInfo baseTable, TableInfo targetTable) {
        if (!baseTable.tableName().equals(targetTable.tableName())) {
            builder.enrichSubType(SchemaDiff.TYPE_NAME)
                    .addAttribute(SchemaDiff.ATTR_PREV_NAME, baseTable.tableName());
        }
    }

    private static void enrichWithPrimaryKeyDiff(SchemaDiff.Builder builder, TableInfo baseTable, TableInfo targetTable) {
        // Primary Key on conflict behavior
        String basePkOnConflict = baseTable.primaryKeyOnConflict();
        basePkOnConflict = basePkOnConflict == null ? "" : basePkOnConflict;
        String targetPkOnConflict = targetTable.primaryKeyOnConflict();
        targetPkOnConflict = targetPkOnConflict == null ? "" : targetPkOnConflict;
        if (!basePkOnConflict.equals(targetPkOnConflict)) {
            builder.enrichSubType(SchemaDiff.TYPE_PK_ON_CONFLICT)
                    .addAttribute(SchemaDiff.ATTR_PREV_PK_ON_CONFLICT, basePkOnConflict)
                    .addAttribute(SchemaDiff.ATTR_CURR_PK_ON_CONFLICT, targetPkOnConflict);
        }

        // Primary Key columns
        if (!baseTable.getPrimaryKey().equals(targetTable.getPrimaryKey())) {
            builder.enrichSubType(SchemaDiff.TYPE_PK_COLUMNS)
                    .addAttribute(SchemaDiff.ATTR_PREV_PK_COL_NAMES, toCsv(baseTable.getPrimaryKey(), true))
                    .addAttribute(SchemaDiff.ATTR_CURR_PK_COL_NAMES, toCsv(targetTable.getPrimaryKey(), true));
        }
    }

    private static Stream<Pair<String, SchemaDiff>> tableDiffStream(Map<String, TableInfo> tables, Function<TableInfo, SchemaDiff> mapper) {
        return tables.entrySet()
                .stream()
                .map(e -> Pair.create(e.getKey(), mapper.apply(e.getValue())));
    }

    private static Map<String, TableInfo> missingTables(@Nonnull TableContext base, @Nonnull TableContext target) {
        return target.allTables()
                .stream()
                .filter(t -> !contextContainsTable(base, t))
                .collect(mapCollector((acc, table) -> acc.put(table.qualifiedClassName(), table)));
    }

    private static boolean contextContainsTable(TableContext context, TableInfo table) {
        return context.tableMap().containsKey(table.qualifiedClassName());
    }

    /**
     * @param col a possibly null or empty collection of Strings
     * @return a csv of all the values
     */
    @Nonnull
    private static String toCsv(@Nullable Collection<String> col, boolean sort) {
        if (col == null || col.size() == 0) {
            return "";
        }
        if (col.size() == 1) {
            return col.toArray(new String[1])[0];
        }


        List<String> list = new ArrayList<>(col);
        if (sort) {
            Collections.sort(list);
        }

        StringBuilder buf = new StringBuilder();
        for (String s : list) {
            buf.append(s).append(',');
        }
        return buf.delete(buf.length() - 1, buf.length()).toString();
    }


}
