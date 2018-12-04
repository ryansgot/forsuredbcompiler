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

        /**
         * @return this {@link Builder}
         * @see SchemaDiff#category()
         */
        public abstract Builder category(int category);                         // category

        /**
         * @return this {@link Builder}
         * @see SchemaDiff#type()
         */
        public abstract Builder type(int type);                                 // type

        /**
         * @return this {@link Builder}
         * @see SchemaDiff#subType()
         */
        public abstract Builder subType(long subType);                          // sub_type

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
            return attributes(attributes).autoBuild();
        }

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
    public static final int TYPE_NAME = 0b10000;                                    // 0b10000

    /**
     * <p>The Java type of a column changed. This may have repercussions for
     * the type stored in the DBMS, so type changes get detected as diffs.
     * <p>Relevant attributes:
     * <ul>
     *   <li>{@link #ATTR_PREV_TYPE}</li>
     *   <li>{@link #ATTR_CURR_TYPE}</li>
     * </ul>
     */
    public static final int TYPE_TYPE = TYPE_NAME << 1;                             // 0b100000

    /**
     * <p>The columns of a primary key or an index changed. This diff will
     * include order differences as well as length differences.
     * <p>Relevant attributes:
     * <ul>
     *   <li>{@link #ATTR_PREV_COL_NAMES}</li>
     *   <li>{@link #ATTR_CURR_COL_NAMES}</li>
     * </ul>
     */
    public static final int TYPE_COLUMNS = TYPE_TYPE << 1;                          // 0b1000000

    /**
     * <p>The sort of an index changed. This can coincide with the
     * {@link #TYPE_COLUMNS} subtype.
     * <p>Relevant attributes:
     * <ul>
     *   <li>{@link #ATTR_PREV_SORT}</li>
     *   <li>{@link #ATTR_CURR_SORT}</li>
     * </ul>
     */
    public static final int TYPE_SORT = TYPE_COLUMNS << 1;                          // 0b10000000

    /**
     * <p>The constraint of a column in a table changed.
     * <p>Relevant attributes:
     * <ul>
     *   <li>{@link #ATTR_CONSTRAINT}</li>
     *   <li>{@link #ATTR_PREV_CONSTRAINT_VAL}</li>
     *   <li>{@link #ATTR_CURR_CONSTRAINT_VAL}</li>
     * </ul>
     */
    public static final int TYPE_CONSTRAINT = TYPE_SORT << 1;                    // 0b100000000

    /**
     * <p>The default value of a column in a table changed.
     * <p>Relevant attributes:
     * <ul>
     *   <li>{@link #ATTR_PREV_DEFAULT}</li>
     *   <li>{@link #ATTR_CURR_DEFAULT}</li>
     * </ul>
     */
    public static final int TYPE_DEFAULT = TYPE_CONSTRAINT << 1;                   // 0b1000000000

    /**
     * <p>The diff pertains to the table only. The table could be . . .
     * <ul>
     *   <li>created {@link #TYPE_CREATED}</li>
     *   <li>dropped {@link #TYPE_DROPPED}</li>
     *   <li>name changed {@link #TYPE_NAME}</li>
     * </ul>
     */
    public static final int CAT_TABLE = 1;

    /**
     * <p>The diff pertains to one or more columns of a table. The type could
     * be
     * <ul>
     *   <li>created {@link #TYPE_CREATED}</li>
     *   <li>dropped {@link #TYPE_DROPPED}</li>
     *   <li>name changed {@link #TYPE_NAME}</li>
     *   <li>type changed {@link #TYPE_TYPE}</li>
     *   <li>constraint changed {@link #TYPE_CONSTRAINT}</li>
     *   <li>default changed {@link #TYPE_CONSTRAINT}</li>
     * </ul>
     */
    public static final int CAT_COLUMN = CAT_TABLE + 1;

    /**
     * <p>The diff pertains to the primary key of a table. Typically, this
     * will happen when the order changes or a column is added/removed from the
     * primary key.
     * <ul>
     *   <li>primary key order changed {@link #TYPE_COLUMNS}</li>
     *   <li>primary key column added {@link #TYPE_COLUMNS}</li>
     *   <li>primary key column removed {@link #TYPE_COLUMNS}</li>
     * </ul>
     */
    public static final int CAT_PRIMARY_KEY = CAT_COLUMN + 1;

    /**
     * <p>The diff pertains to a foreign key of a table. The foreign key could
     * be . . .
     * <ul>
     *   <li>created {@link #TYPE_CREATED}</li>
     *   <li>dropped {@link #TYPE_DROPPED}</li>
     * </ul>
     */
    public static final int CAT_FOREIGN_KEY = CAT_PRIMARY_KEY + 1;

    /**
     * <p>The diff pertains to an index of a table. The index could be . . .
     * <ul>
     *   <li>created {@link #TYPE_CREATED}</li>
     *   <li>dropped {@link #TYPE_DROPPED}</li>
     *   <li>column added {@link #TYPE_COLUMNS}</li>
     *   <li>column removed {@link #TYPE_COLUMNS}</li>
     *   <li>sort changed {@link #TYPE_SORT}</li>
     *   <li>column order changed {@link #TYPE_COLUMNS}</li>
     * </ul>
     */
    public static final int CAT_INDEX = CAT_FOREIGN_KEY + 1;

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
     * <p>The current column names. This will be non null under one of the
     * following sets of conditions:
     * <ul>
     *   <li>
     *     {@link #type()} is {@link #TYPE_CREATED} and {@link #category()} is
     *     one of
     *     <ul>
     *       <li>
     *         {@link #CAT_INDEX}. In this case, {@link #ATTR_CURR_SORT} will
     *         also be available
     *       </li>
     *       <li>
     *         {@link #CAT_FOREIGN_KEY}. In this case,
     *         {@link #ATTR_CURR_FK_COL_NAMES} will also be available.
     *       </li>
     *     </ul>
     *   </li>
     *   <li>
     *     {@link #type()} is {@link #TYPE_CHANGED} and {@link #category()} is
     *     one of
     *     <ul>
     *       <li>
     *         {@link #CAT_INDEX}. In this case {@link #ATTR_CURR_SORT} will
     *         also be available.
     *       </li>
     *       <li>
     *         {@link #CAT_FOREIGN_KEY}. In this case,
     *         {@link #ATTR_CURR_FK_COL_NAMES} will also be available
     *       </li>
     *       <li>
     *         {@link #CAT_PRIMARY_KEY}
     *       </li>
     *     </ul>
     *   </li>
     *   <li>
     *     {@link #type()} is {@link #TYPE_DROPPED} and {@link #category()} is
     *     one of
     *     <ul>
     *       <li>
     *         {@link #CAT_COLUMN}. In this case, the names of the columns will
     *         be all of the remaining columns.
     *       </li>
     *       <li>
     *         {@link #CAT_FOREIGN_KEY}. In this case, the names of the columns
     *         will be the names of the columns that comprise the foreign key.
     *       </li>
     *     </ul>
     *   </li>
     * </ul>
     * <p>These column names are always columns of the {@link #tableName()}
     * table.
     * @see #ATTR_PREV_COL_NAMES
     */
    public static final String ATTR_CURR_COL_NAMES = "c_col_names";

    /**
     * <p>The current column names. This will be non null under one of the
     * following sets of conditions:
     * <ul>
     *   <li>
     *     {@link #type()} is {@link #TYPE_CHANGED} and {@link #category()} is
     *     one of
     *     <ul>
     *       <li>
     *         {@link #CAT_INDEX}. IF {@link #subType()} is not
     *         {@link #TYPE_NAME}, then {@link #ATTR_PREV_SORT} will also be
     *         available.
     *       </li>
     *       <li>
     *         {@link #CAT_FOREIGN_KEY}. In this case,
     *         {@link #ATTR_PREV_FK_COL_NAMES} will also be available.
     *       </li>
     *       <li>
     *         {@link #CAT_PRIMARY_KEY}
     *       </li>
     *     </ul>
     *   </li>
     *   <li>
     *     {@link #type()} is {@link #TYPE_DROPPED} and {@link #category()} is
     *     one of
     *     <ul>
     *       <li>
     *         {@link #CAT_COLUMN}. All of the table's previous columns will be
     *         included.
     *       </li>
     *       <li>
     *         {@link #CAT_FOREIGN_KEY}. In this case,
     *         {@link #ATTR_PREV_FK_COL_NAMES} will also be available.
     *       </li>
     *     </ul>
     *   </li>
     * </ul>
     * <p>These column names are always columns of the {@link #tableName()}
     * table.
     * @see #ATTR_CURR_COL_NAMES
     */
    public static final String ATTR_PREV_COL_NAMES = "p_col_names";

    /**
     * <p>The current foreign key column names. This will be non null if the
     * {@link #type()} is one of
     * <ul>
     *   <li>{@link #TYPE_CREATED}</li>
     *   <li>{@link #TYPE_CHANGED}</li>
     * </ul>
     * and the {@link #category()} is {@link #CAT_FOREIGN_KEY}
     * <p>These column names are always columns of the foreign table
     * @see #ATTR_CURR_FK_COL_NAMES
     */
    public static final String ATTR_CURR_FK_COL_NAMES = "c_fk";

    /**
     * <p>The previous foreign key column names. This will be non null if the
     * {@link #type()} is {@link #TYPE_CHANGED} or {@link #TYPE_DROPPED} and
     * {@link #category()} is {@link #CAT_FOREIGN_KEY}
     * <p>These column names are always columns of the foreign table
     * @see #ATTR_CURR_FK_COL_NAMES
     */
    public static final String ATTR_PREV_FK_COL_NAMES = "p_fk";

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
    public static final String ATTR_CONSTRAINT = "constraint";
    /**
     * TODO: figure this one out
     */
    public static final String ATTR_PREV_CONSTRAINT_VAL = "p_constraint_val";
    /**
     * TODO: figure this one out
     */
    public static final String ATTR_CURR_CONSTRAINT_VAL = "c_constraint_val";

    /**
     * <p>The default value of the column has changed. This will be non null if
     * {@link #type()} is {@link #TYPE_CHANGED} and the {@link #subType()} is
     * {@link #TYPE_DEFAULT} and the {@link #category()} is {@link #CAT_COLUMN}
     */
    public static final String ATTR_PREV_DEFAULT = "p_default";
    public static final String ATTR_CURR_DEFAULT = "c_default";

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
                .subType(TYPE_CREATED)
                .category(CAT_TABLE)
                .addAttribute(ATTR_CURR_NAME, tableName)
                .tableName(tableName)
                .build();
    }

    public static SchemaDiff forTableDropped(@Nonnull String tableName) {
        return builder()
                .type(TYPE_DROPPED)
                .subType(TYPE_DROPPED)
                .category(CAT_TABLE)
                .tableName(tableName)
                .build();
    }

    public static SchemaDiff forTableRenamed(@Nonnull String previousName, @Nonnull String currentName) {
        return builder()
                .type(TYPE_CHANGED)
                .subType(TYPE_NAME)
                .category(CAT_TABLE)
                .tableName(currentName)
                .addAttribute(ATTR_PREV_NAME, previousName)
                .addAttribute(ATTR_CURR_NAME, currentName)
                .build();

    }

    public static SchemaDiff forColumnCreated(@Nonnull String tableName, @Nonnull String colName) {
        return builder()
                .type(TYPE_CREATED)
                .subType(TYPE_CREATED)
                .category(CAT_COLUMN)
                .addAttribute(ATTR_CURR_NAME, colName)
                .tableName(tableName)
                .build();
    }

    /**
     * <p>There are five categories:
     * <ol>
     *   <li>{@link #CAT_TABLE} - pertains to a table</li>
     *   <li>{@link #CAT_COLUMN} - pertains to a column of this table</li>
     *   <li>{@link #CAT_PRIMARY_KEY} - pertains to this table's PK</li>
     *   <li>{@link #CAT_FOREIGN_KEY} - pertains to a foreign key ref</li>
     *   <li>{@link #CAT_INDEX} - pertains to an index of this table</li>
     * </ol>
     * @return the category of this diff
     */
    public abstract int category();                                         // category

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
     * {@link #attributes()}. If the {@link #type()} is {@link #TYPE_CREATED}
     * or {@link #TYPE_DROPPED}, then {@link #subType()} and {@link #type()}
     * will return the same value.
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
}