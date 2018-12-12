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
 * associated with
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
         * @return
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
     * <p>An object (a table, column, index, etc) should be created. See the
     * {@link #category()} for the category of object that was created.
     */
    public static final int TYPE_CREATED = 0;

    /**
     * <p>An existing object (a table, column, index, etc) was modified. The
     * type of modification will be specified by the {@link #subType()}, and
     * the specifics of the change will be specified by the
     * {@link #attributes()}. See the specific type for the relevant
     * attributes.
     */
    public static final int TYPE_CHANGED = TYPE_CREATED + 1;

    /**
     * <p>An existing object (a table, column, index, etc) was dropped.
     */
    public static final int TYPE_DROPPED = TYPE_CHANGED + 1;

    // subtypes
    /**
     * <p>An object (a table, column, index, etc) may have its name changed.
     * When an object is renamed, this flag will be set on the subtype.
     * <p>Relevant attributes:
     * <ul>
     *   <li>{@link #ATTR_CURR_NAME}</li>
     *   <li>{@link #ATTR_PREV_NAME}</li>
     * </ul>
     */
    public static final int TYPE_NAME = 0b1;                                        // 0b1

    /**
     * <p>The Java type of a column changed. This may have repercussions for
     * the type stored in the DBMS, so type changes get detected as diffs.
     * <p>Relevant attributes:
     * <ul>
     *   <li>{@link #ATTR_PREV_TYPE}</li>
     *   <li>{@link #ATTR_CURR_TYPE}</li>
     * </ul>
     */
    public static final int TYPE_TYPE = TYPE_NAME << 1;                             // 0b10

    /**
     * <p>Columns were created on a table
     * <ul>
     *   <li>{@link #ATTR_CREATE_COLUMNS}</li>
     * </ul>
     */
    public static final int TYPE_ADD_COLUMNS = TYPE_TYPE << 1;                      // 0b100

    /**
     * <p>Columns were dropped from a table
     * <ul>
     *   <li>{@link #ATTR_DROP_COLUMNS}</li>
     * </ul>
     */
    public static final int TYPE_DROP_COLUMNS = TYPE_ADD_COLUMNS << 1;              // 0b1000

    /**
     * <p>Columns were either created or dropped from a table
     * <ul>
     *   <li>{@link #ATTR_RENAME_COLUMNS}</li>
     * </ul>
     */
    public static final int TYPE_RENAME_COLUMNS = TYPE_DROP_COLUMNS << 1;           // 0b10000

    /**
     * <p>The columns of a primary key changed.
     * <p>Relevant attributes:
     * <ul>
     *   <li>{@link #ATTR_PREV_PK_COL_NAMES}</li>
     *   <li>{@link #ATTR_CURR_PK_COL_NAMES}</li>
     * </ul>
     */
    public static final int TYPE_PK_COLUMNS = TYPE_DROP_COLUMNS << 1;               // 0b100000

    /**
     * <p>The primary key on conflict behavior changed.
     * <p>Relevant attributes:
     * <ul>
     *   <li>{@link #ATTR_PREV_PK_ON_CONFLICT}</li>
     *   <li>{@link #ATTR_CURR_PK_ON_CONFLICT}</li>
     * </ul>
     */
    public static final int TYPE_PK_ON_CONFLICT = TYPE_PK_COLUMNS << 1;             // 0b1000000

    /**
     * <p>The sort of an index changed. This can coincide with the
     * {@link #TYPE_PK_COLUMNS} subtype.
     * <p>Relevant attributes:
     * <ul>
     *   <li>{@link #ATTR_PREV_SORT}</li>
     *   <li>{@link #ATTR_CURR_SORT}</li>
     * </ul>
     */
    public static final int TYPE_SORT = TYPE_PK_ON_CONFLICT << 1;                   // 0b10000000

    /**
     * <p>The constraint of a column in a table changed.
     * <p>Relevant attributes:
     * <ul>
     *   <li>{@link #ATTR_CONSTRAINTS}</li>
     *   <li>{@link #ATTR_PREV_CONSTRAINT_VAL}</li>
     *   <li>{@link #ATTR_CURR_CONSTRAINT_VAL}</li>
     * </ul>
     */
    public static final int TYPE_CONSTRAINT = TYPE_SORT << 1;                       // 0b100000000

    /**
     * <p>The default value of a column in a table changed. The relevant
     * attribute is {@link #ATTR_DEFAULTS}
     */
    public static final int TYPE_DEFAULT = TYPE_CONSTRAINT << 1;                    // 0b1000000000

    /**
     * <p>One or more foreign keys was added to a table. Relevant attribute is
     * {@link #ATTR_CREATED_FKS}
     */
    public static final int TYPE_CREATE_FK = TYPE_DEFAULT << 1;                     // 0b10000000000

    /**
     * <p>One or more foreign keys was dropped from a table. Relevant attribute
     * is {@link #ATTR_DROPPED_FKS}
     */
    public static final int TYPE_DROP_FK = TYPE_CREATE_FK << 1;                     // 0b100000000000

    /**
     * <p>The current name of the object in question. This will always be non
     * null.
     * @see #ATTR_PREV_NAME
     */
    public static final String ATTR_CURR_NAME = "c_name";

    /**
     * <p>The name of the object to which the diff pertains. This will be non
     * null when the {@link #type()} is {@link #TYPE_CHANGED} and the
     * {@link #subType()} is {@link #TYPE_NAME} regardless of
     * {@link #category()}.
     * @see #ATTR_CURR_NAME
     */
    public static final String ATTR_PREV_NAME = "p_name";

    /**
     * <p>The type of the column to which the diff pertains. This will be non
     * null when the {@link #type()} is {@link #TYPE_CHANGED} and when the
     * {@link #subType()} is {@link #TYPE_TYPE}.
     * @see #ATTR_PREV_TYPE
     */
    public static final String ATTR_CURR_TYPE = "c_type";

    /**
     * <p>The type of the column to which the diff pertains. This will be non
     * null when the {@link #type()} is {@link #TYPE_CHANGED} and when the
     * {@link #subType()} is {@link #TYPE_TYPE}.
     * @see #ATTR_CURR_TYPE
     */
    public static final String ATTR_PREV_TYPE = "p_type";

    /**
     * <p>The column names of the columns added to a table.
     * {@link #TYPE_ADD_COLUMNS}.
     */
    public static final String ATTR_CREATE_COLUMNS = "c_cols";

    /**
     * <p>The column names of the columns dropped from a table.
     * {@link #TYPE_DROP_COLUMNS}.
     */
    public static final String ATTR_DROP_COLUMNS = "d_cols";

    /**
     * <p>A comma-separated list of prev=curr column names.
     */
    public static final String ATTR_RENAME_COLUMNS = "r_cols";

    /**
     * <p>The current primary key column names. This will be non null when:
     * TODO
     * @see #ATTR_PREV_PK_COL_NAMES
     */
    public static final String ATTR_CURR_PK_COL_NAMES = "c_col_names";

    /**
     * <p>The current primary key column names. This will be non null when:
     * TODO
     * @see #ATTR_CURR_PK_COL_NAMES
     */
    public static final String ATTR_PREV_PK_COL_NAMES = "p_pk_col_names";

    /**
     * <p>The current primary key column names. This will be non null when:
     * TODO
     * @see #ATTR_PREV_PK_COL_NAMES
     */
    public static final String ATTR_CURR_PK_ON_CONFLICT = "c_pk_on_conflict";

    /**
     * <p>The current primary key column names. This will be non null when:
     * TODO
     * @see #ATTR_CURR_PK_COL_NAMES
     */
    public static final String ATTR_PREV_PK_ON_CONFLICT = "p_pk_on_conflict";

    public static final String ATTR_CREATED_FKS = "c_fk";

    public static final String ATTR_DROPPED_FKS = "d_fk";

    /**
     * <p>This does not have a current/previous version. The foreign key table
     * cannot be changed for an existing foreign key, so you must drop the
     * foreign key first, then create a new one.
     * <p>This will be non null if the {@link #type()} is one of
     * <ul>
     *   <li>{@link #TYPE_CREATED}</li>
     *   <li>{@link #TYPE_CHANGED}</li>
     *   <li>{@link #TYPE_DROPPED}</li>
     * </ul>
     * and the {@link #category()} is {@link #CAT_FOREIGN_KEY}
     */
    public static final String ATTR_FK_TABLE = "fk_table";

    /**
     * <p>The current sort of an index. This will be non null under of the
     * following sets of conditions
     * <ul>
     *   <li>
     *     {@link #type()} is {@link #TYPE_CREATED} and the {@link #category()}
     *     is {@link #CAT_INDEX}.
     *   </li>
     *   <li>
     *     {@link #type()} is {@link #TYPE_CHANGED} and the
     *     {@link #category()} is {@link #CAT_INDEX}. If the {@link #subType()}
     *     is {@link #TYPE_SORT}, then only the sorts of the existing columns
     *     has changed.
     *   </li>
     * </ul>
     */
    public static final String ATTR_CURR_SORT = "c_sort";

    /**
     * <p>The previous sort of an index. This will be non null under of the
     * following sets of conditions
     * <ul>
     *   <li>
     *     {@link #type()} is {@link #TYPE_CHANGED} and the {@link #category()}
     *     is {@link #CAT_INDEX}. If the {@link #subType()} is
     *     {@link #TYPE_SORT}, then only the sorts of the existing columns has
     *     changed.
     *   </li>
     *   <li>
     *     {@link #type()} is {@link #TYPE_DROPPED} and the {@link #category()}
     *     is {@link #CAT_INDEX}
     *   </li>
     * </ul>
     */
    public static final String ATTR_PREV_SORT = "p_sort";

    /**
     * TODO: figure this one out
     */
    public static final String ATTR_CONSTRAINTS = "constraint";
    /**
     * TODO: figure this one out
     */
    public static final String ATTR_PREV_CONSTRAINT_VAL = "p_constraint_val";
    /**
     * TODO: figure this one out
     */
    public static final String ATTR_CURR_CONSTRAINT_VAL = "c_constraint_val";

    /**
     * <p>A comma-separated list of col=default values
     */
    public static final String ATTR_DEFAULTS = "defaults";

    /**
     * <p>The name of the uniqueness constraint.
     */
    public static final String CONSTRAINT_UNIQUE = "UNIQUE";

    /**
     * <p>The name of the uniqueness constraint.
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
     * apply a diff. The name of the object will always be supplied at the key
     * {@link #ATTR_CURR_NAME}
     * <p>The attributes that may present in any {@link SchemaDiff} are defined
     * by the {@link #category()} and {@link #subType()} of the
     * {@link SchemaDiff}.
     * @return the attributes associated with this diff
     * @see #subType()
     * @see #category()
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