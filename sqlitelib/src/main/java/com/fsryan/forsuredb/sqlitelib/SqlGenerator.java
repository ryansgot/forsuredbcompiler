/*
   forsuredbsqlitelib, sqlite library for the forsuredb project

   Copyright 2015 Ryan Scott

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.fsryan.forsuredb.sqlitelib;

import com.fsryan.forsuredb.api.*;
import com.fsryan.forsuredb.api.sqlgeneration.DBMSIntegrator;
import com.fsryan.forsuredb.api.sqlgeneration.SqlForPreparedStatement;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.migration.Migration;
import com.fsryan.forsuredb.migration.MigrationSet;
import com.fsryan.forsuredb.serialization.FSDbInfoSerializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.fsryan.forsuredb.sqlitelib.QueryBuilder.*;

public class SqlGenerator implements DBMSIntegrator {

    public static final String CURRENT_UTC_TIME = "STRFTIME('%Y-%m-%d %H:%M:%f', 'NOW')";
    public static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        }
    };

    public static final String CHANGE_ACTION_NO_ACTION = "NO ACTION";
    public static final String CHANGE_ACTION_RESTRICT = "RESTRICT";
    public static final String CHANGE_ACTION_SET_NULL = "SET NULL";
    public static final String CHANGE_ACTION_SET_DEFAULT = "SET DEFAULT";
    public static final String CHANGE_ACTION_CASCADE = "CASCADE";

    /*package*/ static final Set<Migration.Type> TYPES_REQUIRING_TABLE_RECREATION = new HashSet<>(4);
    static {
        TYPES_REQUIRING_TABLE_RECREATION.add(Migration.Type.CREATE_TABLE);
        TYPES_REQUIRING_TABLE_RECREATION.add(Migration.Type.CHANGE_DEFAULT_VALUE);
        TYPES_REQUIRING_TABLE_RECREATION.add(Migration.Type.UPDATE_FOREIGN_KEYS);
        TYPES_REQUIRING_TABLE_RECREATION.add(Migration.Type.UPDATE_PRIMARY_KEY);
    }

    // visible for testing
    private static final Set<String> columnExclusionFilter = new HashSet<>(Arrays.asList("_id", "created", "modified"));

    private final ProjectionHelper projectionHelper;

    public SqlGenerator() {
        projectionHelper = new ProjectionHelper(this);
    }

    @Override
    public List<String> generateMigrationSql(MigrationSet migrationSet, FSDbInfoSerializer serializer) {
        if (migrationSet == null || !migrationSet.containsMigrations() || migrationSet.targetSchema() == null) {
            return new ArrayList<>();
        }

        QueryGeneratorFactory qgf = new QueryGeneratorFactory(migrationSet);
        List<Migration> migrations = migrationSet.orderedMigrations();
        Collections.sort(migrations, new MigrationComparator(migrationSet.targetSchema()));
        List<String> sqlList = new ArrayList<>();
        Set<String> recreatedTables = new HashSet<>();
        for (Migration m : migrations) {
            if (recreatedTables.contains(m.tableName()) && isMigrationHandledOnCreate(m, migrationSet.targetSchema())) {
                continue;
            }
            if (TYPES_REQUIRING_TABLE_RECREATION.contains(m.type())) {
                recreatedTables.add(m.tableName());
            }
            sqlList.addAll(qgf.getFor(m, migrationSet.targetSchema(), serializer).generate());
        }

        return sqlList;
    }

    @Override
    public String newSingleRowInsertionSql(String tableName, List<String> columns) {
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("Cannot insert into null or empty table: '" + tableName + "'");
        }
        return columns == null || columns.isEmpty()
                ? singleRecordInsertion(tableName, Collections.singletonList("deleted"))
                : singleRecordInsertion(tableName, columns);
    }

    @Override
    public String unambiguousColumn(String tableName, String columnName) {
        return tableName + '.' + columnName;
    }

    @Override
    public String unambiguousRetrievalColumn(String tableName, String columnName) {
        return tableName + '_' + columnName;
    }

    @Override
    public String expressOrdering(List<FSOrdering> orderings) {
        if (orderings == null || orderings.isEmpty()) {
            return "";
        }

        StringBuilder buf = new StringBuilder();
        for (FSOrdering ordering : orderings) {
            buf.append(unambiguousColumn(ordering.table(), ordering.column()))
                    .append(" ")
                    .append(ordering.direction() < OrderBy.ORDER_ASC ? "DESC" : "ASC")    // <-- 0 or positive treated as ASC
                    .append(", ");
        }

        return buf.delete(buf.length() - 2, buf.length()).toString();
    }

    @Override
    public Date parseDate(String dateStr) {
        try {
            return DATE_FORMAT.get().parse(dateStr);
        } catch (ParseException pe) {
            pe.printStackTrace();
        }
        return null;
    }

    @Nonnull
    @Override
    public DateFormat getDateFormat() {
        return DATE_FORMAT.get();
    }

    @Override
    public boolean alwaysUnambiguouslyAliasColumns() {
        return true;
    }

    @Override
    public SqlForPreparedStatement createQuerySql(@Nonnull String table,
                                                  @Nullable FSProjection projection,
                                                  @Nullable FSSelection selection,
                                                  @Nullable List<FSOrdering> orderings) {
        final String[] p = projection == FSProjection.COUNT || projection == FSProjection.COUNT_DISTINCT
                ? projection.columns()
                : projectionHelper.formatProjection(projection);
        final String orderBy = expressOrdering(orderings);
        final QueryCorrector qc = new QueryCorrector(table, null, selection, orderBy);
        final String where = qc.getSelection(true);
        final boolean distinct = projection != null && projection.isDistinct();
        return new SqlForPreparedStatement(
                buildQuery(qc.hasCompoundSelect(), distinct, table, p, where, null, null, orderBy, qc.getLimit(), qc.getOffset()),
                qc.getSelectionArgs()
        );
    }

    @Override
    public SqlForPreparedStatement createQuerySql(@Nonnull String table,
                                                  @Nullable List<FSJoin> joins,
                                                  @Nullable List<FSProjection> projections,
                                                  @Nullable FSSelection selection,
                                                  @Nullable List<FSOrdering> orderings) {
        final QueryCorrector qc = new QueryCorrector(table, joins, selection, expressOrdering(orderings));
        final String[] p = projectionHelper.formatProjection(projections);
        final String joinStr = qc.getJoinString();
        final String where = qc.getSelection(true);
        final String orderBy = qc.getOrderBy();
        return new SqlForPreparedStatement(
                buildJoinQuery(qc.hasCompoundSelect(), projectionHelper.isDistinct(projections), table, p, joinStr, where, orderBy, qc.getLimit(), qc.getOffset()),
                qc.getSelectionArgs()
        );
    }

    @Override
    public SqlForPreparedStatement createUpdateSql(@Nonnull String table,
                                                   @Nonnull List<String> updateColumns,
                                                   @Nullable FSSelection selection,
                                                   @Nullable List<FSOrdering> orderings) {
        final QueryCorrector qc = new QueryCorrector(table, null, selection, expressOrdering(orderings));
        return new SqlForPreparedStatement(
                buildUpdate(table, updateColumns, qc.getSelection(false)),
                qc.getSelectionArgs()
        );
    }

    @Override
    public SqlForPreparedStatement createDeleteSql(@Nonnull String table,
                                                   @Nullable FSSelection selection,
                                                   @Nullable List<FSOrdering> orderings) {
        final QueryCorrector qc = new QueryCorrector(table, null, selection, expressOrdering(orderings));
        return new SqlForPreparedStatement(
                buildDelete(table, qc.getSelection(false)), // <-- todo delete
                qc.getSelectionArgs()
        );
    }

    @Override
    public String createWhere(@Nonnull String tableName, @Nonnull List<Finder.WhereElement> whereElements) {
        if (whereElements.isEmpty()) {
            return null;
        }

        StringBuilder buf = new StringBuilder();
        for (Finder.WhereElement element : whereElements) {
            throwIfInvalid(element);
            switch (element.type()) {
                case Finder.WhereElement.TYPE_GROUP_START:
                    buf.append('(');
                    break;
                case Finder.WhereElement.TYPE_GROUP_END:
                    buf.append(')');
                    break;
                case Finder.WhereElement.TYPE_OR_CONJUNCTION:
                    buf.append(" OR ");
                    break;
                case Finder.WhereElement.TYPE_AND_CONJUNCTION:
                    buf.append(" AND ");
                    break;
                case Finder.WhereElement.TYPE_CONDITION:
                    buf.append(unambiguousColumn(tableName, element.column())).append(' ');
                    if (element.value() == null) {
                        buf.append(element.op() == Finder.OP_EQ ? "IS NULL" : "IS NOT NULL");
                    } else {
                        buf.append(operationStr(element.op())).append(" ?");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown WhereElement type: " + element.type());
            }
        }
        return buf.toString();
    }

    @Override
    public Object objectForReplacement(int op, Object obj) {
        if (obj == null) {
            return null;
        }

        if (op == Finder.OP_LIKE) {
            return '%' + String.valueOf(obj) + '%';
        }

        Class<?> cls = obj.getClass();
        return cls == Date.class ? DATE_FORMAT.get().format((Date) obj)
                : cls == BigInteger.class || cls == BigDecimal.class ? String.valueOf(obj)
                : obj;
    }

    private static boolean isMigrationHandledOnCreate(Migration m, Map<String, TableInfo> targetSchema) {
        switch (m.type()) {
            case ADD_UNIQUE_INDEX:
                // intentionally falling through
            case ADD_FOREIGN_KEY_REFERENCE:
            case ALTER_TABLE_ADD_UNIQUE:
                return true;
            case ALTER_TABLE_ADD_COLUMN:
                TableInfo table = targetSchema.get(m.tableName());
                return table.isForeignKeyColumn(m.columnName())
                        || table.getPrimaryKey().contains(m.columnName())
                        || table.getColumn(m.columnName()).unique();
        }
        return false;
    }

    private static void throwIfInvalid(Finder.WhereElement element) {
        if (element.type() != Finder.WhereElement.TYPE_CONDITION) {
            return;
        }

        if (element.op() == Finder.OP_NONE) {
           throw new IllegalArgumentException(("Invalid condition: OP_NONE"));
        }
        if (element.column() == null || element.column().isEmpty()) {
            throw new IllegalArgumentException("element condition may not include null or empty column");
        }
        if (element.op() != Finder.OP_EQ && element.op() != Finder.OP_NE && element.value() == null) {
            throw new IllegalArgumentException("null not allowed for operator: " + operationStr(element.op()));
        }
    }

    private static String operationStr(int op) {
        switch (op) {
            case Finder.OP_EQ: return "=";
            case Finder.OP_GE: return ">=";
            case Finder.OP_GT: return ">";
            case Finder.OP_LE: return "<=";
            case Finder.OP_LIKE: return "LIKE";
            case Finder.OP_LT: return "<";
            case Finder.OP_NE: return "!=";
            case Finder.OP_NONE: throw new IllegalArgumentException("OP_NONE is unresolvable to an operator");
            default: throw new IllegalArgumentException("Unrecognized operation: " + op);
        }
    }
}
