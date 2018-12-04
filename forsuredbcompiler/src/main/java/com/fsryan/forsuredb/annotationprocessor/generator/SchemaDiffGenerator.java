package com.fsryan.forsuredb.annotationprocessor.generator;

import com.fsryan.forsuredb.annotationprocessor.TableContext;
import com.fsryan.forsuredb.annotationprocessor.util.Pair;
import com.fsryan.forsuredb.annotationprocessor.util.StreamUtil;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.migration.SchemaDiff;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static com.fsryan.forsuredb.annotationprocessor.util.StreamUtil.mapCollector;

public class SchemaDiffGenerator {

    private static final BinaryOperator<Map<String, Set<SchemaDiff>>> diffMapCombiner = (m1, m2) -> {
        Map<String, Set<SchemaDiff>> combined = new HashMap<>(m1);
        m2.forEach((k, v) -> {
            Set<SchemaDiff> current = combined.get(k);
            if (current == null) {
                combined.put(k, v);
            } else {
                current.addAll(v);
            }
        });
        return combined;
    };

    private final TableContext base;

    public SchemaDiffGenerator(@Nonnull TableContext base) {
        this.base = base;
    }

    @Nonnull
    public Map<String, Set<SchemaDiff>> generate(@Nonnull TableContext target) {
        // create table diffs
        final Map<String, TableInfo> renamedTables = target.allTables()
                .stream()
                .filter(t -> containsConflictingTableName(base, t))
                .collect(mapCollector((acc, table) -> acc.put(table.qualifiedClassName(), table)));
        final Map<String, TableInfo> newTables = missingTables(base, target);
        final Map<String, TableInfo> droppedTables = missingTables(target, base);


        return StreamUtil.concatAll(
                singletonTableDiffStream(renamedTables, this::createTableRenameDiff),
                singletonTableDiffStream(newTables, t -> SchemaDiff.forTableCreated(t.tableName())),
                singletonTableDiffStream(droppedTables, t -> SchemaDiff.forTableDropped(t.tableName()))
        ).collect(Collector.of(
                HashMap::new,
                (Map<String, Set<SchemaDiff>> acc, Pair<String, Set<SchemaDiff>> pair) -> {
                    final String tableClassName = pair.first();
                    final Set<SchemaDiff> diffs = pair.second();
                    final Set<SchemaDiff> existingDiffs = acc.get(tableClassName);
                    if (existingDiffs == null) {
                        acc.put(tableClassName, diffs);
                    } else {
                        existingDiffs.addAll(diffs);
                    }
                },
                diffMapCombiner
        ));
    }

    private static boolean containsConflictingTableName(@Nonnull TableContext context, @Nonnull TableInfo table) {
        TableInfo contextTable = context.tableMap().get(table.qualifiedClassName());
        return contextTable != null && !contextTable.tableName().equals(table.tableName());
    }

    private static Stream<Pair<String, Set<SchemaDiff>>> singletonTableDiffStream(Map<String, TableInfo> tables, Function<TableInfo, SchemaDiff> mapper) {
        return tables.entrySet()
                .stream()
                .map(e -> {
                    HashSet<SchemaDiff> val = new HashSet<>(1);
                    val.add(mapper.apply(e.getValue()));
                    return Pair.create(e.getKey(), val);
                });
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

    private SchemaDiff createTableRenameDiff(TableInfo t) {
        final String previousName = base.tableNameByClass(t.qualifiedClassName());
        if (previousName == null) {
            throw new IllegalStateException("Detected table rename diff, but cannot find previous name: " + t);
        }
        return SchemaDiff.forTableRenamed(previousName, t.tableName());
    }

}
