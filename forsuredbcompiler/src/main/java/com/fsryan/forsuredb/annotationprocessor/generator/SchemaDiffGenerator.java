package com.fsryan.forsuredb.annotationprocessor.generator;

import com.fsryan.forsuredb.annotationprocessor.TableContext;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.migration.SchemaDiff;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collector;

public class SchemaDiffGenerator {

    private final TableContext base;

    public SchemaDiffGenerator(@Nonnull TableContext base) {
        this.base = base;
    }

    @Nonnull
    public Map<String, Set<SchemaDiff>> generate(@Nonnull TableContext target) {
        // create table diffs
        Map<String, Set<SchemaDiff>> ret = findNewTables(target);

        return ret;
    }

    private Map<String, Set<SchemaDiff>> findNewTables(@Nonnull TableContext target) {
        return target.allTables()
                .stream()
                .filter(t -> !baseContainsTable(t))
                .collect(Collector.of(
                        HashMap::new,
                        (Map<String, Set<SchemaDiff>> acc, TableInfo created) -> {
                            Set<SchemaDiff> destination = acc.computeIfAbsent(created.qualifiedClassName(), k -> new HashSet<>());
                            destination.add(SchemaDiff.forTableCreated(created.tableName()));
                        },
                        (m1, m2) -> {
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
                        }
                ));
    }

    private boolean baseContainsTable(TableInfo table) {
        return table != null && base.tableMap().containsKey(table.qualifiedClassName());
    }
}
