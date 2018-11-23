package com.fsryan.forsuredb.annotationprocessor.generator;

import com.fsryan.forsuredb.annotationprocessor.TableContext;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.migration.SchemaDiff;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        Map<String, Set<SchemaDiff>> ret = new HashMap<>();

        // find the table keys that were matched--none of these will be created
        Set<String> matchedTableKeys = toDiff.tableMap().keySet()
                .stream()
                .filter(qName -> base.tableMap().containsKey(qName))
                .collect(Collectors.toSet());

        // Add create diffs for all tables whose keys were not matched
        toDiff.tableMap().entrySet()
                .stream()
                .filter(toDiffEntry -> !matchedTableKeys.contains(toDiffEntry.getKey()))
                .forEach(toDiffEntry -> {
                    final String key = toDiffEntry.getKey();
                    final TableInfo table = toDiffEntry.getValue();
                    Set<SchemaDiff> diffs = ret.computeIfAbsent(key, k -> new HashSet<>());
                    diffs.add(SchemaDiff.forTableCreated(table.tableName()));
                });

        return ret;
    }
}
