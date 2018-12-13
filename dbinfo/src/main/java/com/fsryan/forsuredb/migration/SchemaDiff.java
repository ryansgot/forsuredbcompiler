package com.fsryan.forsuredb.migration;

import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>The idea of a {@link SchemaDiff} is to provide all of the information the
 * code integrating with the DBMS needs about a migration to apply a migration.
 * That is not to say that all diffs can be taken independent from a full
 * picture of the target schema, but that the most important bits about the
 * diff can be captured here.
 * <p>The code integrating with the DBMS is intended to work out the DBMS-
 * specific logic for resolving the difference between the existing schema and
 * the target schema.
 * <p>Each {@link SchemaDiff} should be associated with the fully qualified
 * class name of the FSGetApi extension so that the diff can get properly
 * associated with the table to which the difference applies.
 */
@AutoValue
public abstract class SchemaDiff {

    @AutoValue.Builder
    public static abstract class Builder {

        private final Map<String, String> attributes = new HashMap<>(4);
        private long subType = 0;

        /**
         * @return this {@link Builder}
         * @see SchemaDiff#type()
         */
        public abstract Builder type(int type);                                 // type

        /**
         * <p>Enriches the subtype by logical OR
         * @param subType the additive subType
         * @return this {@link Builder}
         */
        public Builder enrichSubType(long subType) {
            this.subType |= subType;
            return this;
        }

        /**
         * <p>Replaces the subType
         * @param subType the subType to overwrite
         * @return this {@link Builder}
         */
        public Builder replaceSubType(long subType) {
            this.subType = subType;
            return this;
        }

        /**
         * @return this {@link Builder}
         */
        public abstract Builder tableName(@Nonnull String tableName);           // table_name

        /**
         * @param name the name of the attribute
         * @param value the name of the attribute
         * @return this {@link Builder}
         */
        public Builder addAttribute(@Nonnull String name, @Nullable String value) {
            attributes.put(name, value);
            return this;
        }

        /**
         * @param attributes the attributes to add
         * @return this {@link Builder}
         */
        public Builder addAllAttributes(Map<String, String> attributes) {
            if (attributes != null) {
                this.attributes.putAll(attributes);
            }
            return this;
        }

        public SchemaDiff build() {
            return attributes(attributes).subType(subType).autoBuild();
        }

        /**
         * @return this {@link Builder}
         * @see SchemaDiff#subType()
         */
        abstract Builder subType(long subType);                          // sub_type

        /**
         * @return this {@link Builder}
         * @see SchemaDiff#attributes()
         */
        abstract Builder attributes(@Nonnull Map<String, String> attributes);   // attributes

        /**
         * @return The {@link SchemaDiff} built up by this builder
         */
        abstract SchemaDiff autoBuild();
    }

    /**
     * <p>An object (a table, column, index, etc) should be created.
     */
    public static final int TYPE_CREATED = 0;

    /**
     * <p>The table should should be altered. The type of alteration will be
     * specified by the {@link #subType()}, and the attributes of the change
     * will be available in the {@link #attributes()}.
     */
    public static final int TYPE_CHANGED = TYPE_CREATED + 1;

    /**
     * <p>An existing object (a table, column, index, etc) should be dropped.
     */
    public static final int TYPE_DROPPED = TYPE_CHANGED + 1;

    // subtypes
    /**
     * <p>The table should be renamed. Both {@link #ATTR_CURR_NAME} and
     * {@link #ATTR_PREV_NAME} will be populated.
     */
    public static final int TYPE_NAME = 0b1;                                        // 0b1

    // TODO: detect java type diffs
    /**
     * <p>The Java type of a column changed. This may have repercussions for
     * the type stored in the DBMS, so type changes get detected as diffs.
     * Both {@link #ATTR_PREV_TYPES} and {@link #ATTR_CURR_TYPES} will be
     * populated.
     */
    public static final int TYPE_COLUMN_TYPES = TYPE_NAME << 1;                     // 0b10

    /**
     * <p>Columns were created on the table. {@link #ATTR_CREATE_COLUMNS} will
     * be populated.
     */
    public static final int TYPE_ADD_COLUMNS = TYPE_COLUMN_TYPES << 1;              // 0b100

    /**
     * <p>Columns were dropped from the table. {@link #ATTR_DROP_COLUMNS} will
     * be populated.
     */
    public static final int TYPE_DROP_COLUMNS = TYPE_ADD_COLUMNS << 1;              // 0b1000

    /**
     * <p>One or more columns of the table were renamed.
     * {@link #ATTR_RENAME_COLUMNS} will be populated.
     */
    public static final int TYPE_RENAME_COLUMNS = TYPE_DROP_COLUMNS << 1;           // 0b10000

    /**
     * <p>The columns of a primary key changed. {@link #ATTR_PREV_PK_COL_NAMES}
     * and {@link #ATTR_CURR_PK_COL_NAMES} will be populated.
     */
    public static final int TYPE_PK_COLUMNS = TYPE_DROP_COLUMNS << 1;               // 0b100000

    /**
     * <p>The primary key on conflict behavior changed.
     * {@link #ATTR_PREV_PK_ON_CONFLICT} and {@link #ATTR_CURR_PK_ON_CONFLICT}
     * will be populated.
     */
    public static final int TYPE_PK_ON_CONFLICT = TYPE_PK_COLUMNS << 1;             // 0b1000000

    // TODO: detect sort changes on indices.
    /**
     * <p>The sort of one or more indices of an index changed.
     * {@link #ATTR_IDX_SORTS} will be populated.
     */
    public static final int TYPE_IDX_SORT = TYPE_PK_ON_CONFLICT << 1;               // 0b10000000

    /**
     * <p>The constraint of a column in a table changed.
     * {@link #ATTR_COLUMN_CONSTRAINTS} will be populated.
     */
    public static final int TYPE_COL_CONSTRAINT = TYPE_IDX_SORT << 1;               // 0b100000000

    /**
     * <p>One or more default values of the table's columns should change.
     * {@link #ATTR_DEFAULTS} will be populated.
     */
    public static final int TYPE_DEFAULT = TYPE_COL_CONSTRAINT << 1;                // 0b1000000000

    /**
     * <p>One or more foreign keys was added to a table.
     * {@link #ATTR_CREATED_FKS} will be populated.
     */
    public static final int TYPE_CREATE_FK = TYPE_DEFAULT << 1;                     // 0b10000000000

    /**
     * <p>One or more foreign keys was dropped from a table.
     * is {@link #ATTR_DROPPED_FKS} will be populated
     */
    public static final int TYPE_DROP_FK = TYPE_CREATE_FK << 1;                     // 0b100000000000

    /**
     * <p>The current name of the table.
     */
    public static final String ATTR_CURR_NAME = "c_name";

    /**
     * <p>The previous name of the table. This will be populated only when the
     * {@link #subType()} includes the {@link #TYPE_NAME} value.
     */
    public static final String ATTR_PREV_NAME = "p_name";

    // TODO: detect column type changes
    /**
     * <p>Comma-separated values of the form column_name=java_type_name that
     * represent the current types of the columns that have changed types. This
     * will be populated only when the {@link #subType()} contains the
     * {@link #TYPE_COLUMN_TYPES} value.
     */
    public static final String ATTR_CURR_TYPES = "c_type";

    // TODO: detect column type changes
    /**
     * <p>Comma-separated values of the form column_name=java_type_name that
     * represent the previous types of the columns that have changed types.
     * This will be populated only when the {@link #subType()} contains the
     * {@link #TYPE_COLUMN_TYPES} value.
     */
    public static final String ATTR_PREV_TYPES = "p_type";

    /**
     * <p>The column names of the columns added to a table. This will be
     * populated only when the {@link #subType()} contains
     * {@link #TYPE_ADD_COLUMNS}.
     */
    public static final String ATTR_CREATE_COLUMNS = "c_cols";

    /**
     * <p>The column names of the columns dropped from a table. This will be
     * populated only when the {@link #subType()} contains
     * {@link #TYPE_DROP_COLUMNS}.
     */
    public static final String ATTR_DROP_COLUMNS = "d_cols";

    /**
     * <p>A comma-separated list of prev=curr column names. This will be
     * populated only when the {@link #subType()} contains the
     * {@link #TYPE_RENAME_COLUMNS} value.
     */
    public static final String ATTR_RENAME_COLUMNS = "r_cols";

    /**
     * <p>A comma-separated list of the current primary key column names for
     * this table. This will be populated only when the {@link #subType()}
     * contains {@link #TYPE_PK_COLUMNS}.
     * @see #ATTR_PREV_PK_COL_NAMES
     */
    public static final String ATTR_CURR_PK_COL_NAMES = "c_col_names";

    /**
     * <p>A comma-separated list of the previous primary key column names for
     * this table. This will be populated only when the {@link #subType()}
     * contains {@link #TYPE_PK_COLUMNS}.
     * @see #ATTR_CURR_PK_COL_NAMES
     */
    public static final String ATTR_PREV_PK_COL_NAMES = "p_pk_col_names";

    /**
     * <p>The current primary key on-conflict behavior for this table. This
     * will be populated only when the {@link #subType()} contains
     * {@link #TYPE_PK_ON_CONFLICT}.
     * @see #ATTR_PREV_PK_ON_CONFLICT
     */
    public static final String ATTR_CURR_PK_ON_CONFLICT = "c_pk_on_conflict";

    /**
     * <p>The previous primary key on-conflict behavior for this table. This
     * will be populated only when the {@link #subType()} contains
     * {@link #TYPE_PK_ON_CONFLICT}.
     * @see #ATTR_CURR_PK_ON_CONFLICT
     */
    public static final String ATTR_PREV_PK_ON_CONFLICT = "p_pk_on_conflict";

    /**
     * <p>One or more created foreign keys of the format:
     * <pre>
     * {@code foreign_table:local_col1=foreign_col1,local_col2=foreign_col2:update_action:delete_action}
     * </pre>
     * <p>This will populated when {@link #subType()} contains
     * {@link #TYPE_CREATE_FK}
     */
    public static final String ATTR_CREATED_FKS = "c_fk";

    /**
     * <p>One or more dropped foreign keys of the format:
     * <pre>
     * {@code foreign_table:local_col1=foreign_col1,local_col2=foreign_col2:update_action:delete_action}
     * </pre>
     * <p>This will populated when {@link #subType()} contains
     * {@link #TYPE_DROP_FK}
     */
    public static final String ATTR_DROPPED_FKS = "d_fk";

    // TODO: actually detect differences in index sorts.
    /**
     * <p>One or more indices of the table changed sorts. This will be
     * populated if {@link #subType()} contains {@link #TYPE_IDX_SORT}.
     */
    public static final String ATTR_IDX_SORTS = "c_sort";

    /**
     * <p>One or more columns of a table has changed its constraints. This will
     * be populated if {@link #subType()} contains {@link #TYPE_COL_CONSTRAINT}
     */
    public static final String ATTR_COLUMN_CONSTRAINTS = "col_constraints";

    /**
     * <p>A comma-separated list of col=default values. This will be populated
     * if {@link #subType()} contains {@link #TYPE_DEFAULT}.
     */
    public static final String ATTR_DEFAULTS = "defaults";

    /**
     * <p>The name of the uniqueness constraint.
     */
    public static final String CONSTRAINT_UNIQUE = "UNIQUE";

    /**
     * <p>The name of the not null constraint.
     */
    public static final String CONSTRAINT_NOT_NULL = "NOT NULL";

    public static Builder builder() {
        return new AutoValue_SchemaDiff.Builder();
    }

    public static SchemaDiff forTableCreated(@Nonnull String tableName) {
        return builder()
                .type(TYPE_CREATED)
                .addAttribute(ATTR_CURR_NAME, tableName)
                .tableName(tableName)
                .build();
    }

    public static SchemaDiff forTableDropped(@Nonnull String tableName) {
        return builder()
                .type(TYPE_DROPPED)
                .tableName(tableName)
                .build();
    }

    /**
     * <p>There are three types:
     * <ol>
     *   <li>{@link #TYPE_CREATED} - previously did not exist</li>
     *   <li>{@link #TYPE_CHANGED} - previously existed, but changed</li>
     *   <li>{@link #TYPE_DROPPED} - previously existed, but no longer</li>
     * </ol>
     * @return the type of this diff
     */
    public abstract int type();                                             // type

    /**
     * <p>If the {@link #type()} is {@link #TYPE_CHANGED}, then
     * {@link #subType()} will describe the kind of information available in
     * {@link #attributes()}. This has no meaning when {@link #type()} is
     * something other than {@link #TYPE_CHANGED}.
     * @return the subtype of this diff
     */
    public abstract long subType();                                         // sub_type

    /**
     * @return The name of the table to which this diff applies.
     */
    @Nonnull public abstract String tableName();                            // table_name

    /**
     * <p>Further information is usually necessary when determining how to
     * apply a diff. The name of the table will always be supplied at the key
     * {@link #ATTR_CURR_NAME}
     * @return the attributes associated with this diff
     * @see #subType()
     */
    @Nonnull public abstract Map<String, String> attributes();              // attributes

    /**
     * <p>A {@link SchemaDiff} may actually represent no diff or no recognized
     * diff.
     * @return if there is actually no diff.
     */
    public boolean isEmpty() {
        return type() != TYPE_CREATED && type() != TYPE_DROPPED && subType() == 0;
    }
}