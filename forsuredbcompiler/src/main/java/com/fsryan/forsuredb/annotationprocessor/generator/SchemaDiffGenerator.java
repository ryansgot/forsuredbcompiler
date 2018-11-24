package com.fsryan.forsuredb.annotationprocessor.generator;

import com.fsryan.forsuredb.annotationprocessor.TableContext;
import com.fsryan.forsuredb.annotationprocessor.util.Pair;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.migration.SchemaDiff;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class SchemaDiffGenerator {

    private final TableContext base;

    public SchemaDiffGenerator() {
        this(null);
    }

    public SchemaDiffGenerator(@Nullable TableContext base) {
        this.base = base == null ? TableContext.empty() : base;
    }

    @Nonnull
    public Map<String, Set<SchemaDiff>> calculateDiff(TableContext toDiff) {
        return toDiff.tableMap().entrySet()
                .stream()
                .flatMap(entry -> base.hasTable(entry.getKey()) ? findAllDiffs(entry) : createTableStream(entry))
                .collect(Collector.of(HashMap::new, SchemaDiffGenerator::accumulateDiffs, SchemaDiffGenerator::combineDiffs));
    }

    private static Map<String, Set<SchemaDiff>> combineDiffs(Map<String, Set<SchemaDiff>> result1, Map<String, Set<SchemaDiff>> result2) {
        Map<String, Set<SchemaDiff>> ret = new HashMap<>(result1.size() + result2.size());
        ret.putAll(result1);
        ret.putAll(result2);
        return ret;
    }

    private Stream<Pair<String, Set<SchemaDiff>>> createTableStream(Map.Entry<String, TableInfo> entry) {
        String tableName = entry.getValue().tableName();
        SchemaDiff diff = SchemaDiff.forTableCreated(tableName);
        return Stream.of(Pair.of(tableName, Collections.singleton(diff)));
    }

    private Stream<Pair<String, Set<SchemaDiff>>> findAllDiffs(Map.Entry<String, TableInfo> entry) {
        // TODO: further matched table diffs
        return Stream.of(
                createdColumnDiffs(entry.getKey(), entry.getValue())
        );
    }

    private static void accumulateDiffs(Map<String, Set<SchemaDiff>> result, Pair<String, Set<SchemaDiff>> pair) {
        Set<SchemaDiff> newDiffs = pair.second();
        if (newDiffs == null || newDiffs.isEmpty()) {
            return;
        }
        result.computeIfAbsent(pair.first(), k -> new HashSet<>()).addAll(newDiffs);
    }

    private Pair<String, Set<SchemaDiff>> createdColumnDiffs(String tableKey, TableInfo table) {
        final TableInfo baseTable = base.getTable(tableKey);
        return table.getColumns().stream()
                .filter(c -> !baseTable.hasColumn(c.columnName()))
                .collect(Collector.of(
                        () -> Pair.of(tableKey, new HashSet<>()),
                        (result, c) -> result.second().add(SchemaDiff.forColumnCreated(c.columnName())),
                        (result1, result2) -> {
                            Set<SchemaDiff> combined = new HashSet<>(result1.second().size() + result2.second().size());
                            combined.addAll(result1.second());
                            combined.addAll(result2.second());
                            return Pair.of(result1.first(), combined);
                        }
                ));
    }
}
