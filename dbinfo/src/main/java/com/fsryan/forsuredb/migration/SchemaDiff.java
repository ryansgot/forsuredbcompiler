package com.fsryan.forsuredb.migration;

import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>The idea of a {@link SchemaDiff} is to provide the code integrating with
 * the DBMS all of the information it needs about a migration to apply it.
 * That is not to say that all diffs can be taken independent from a full
 * picture of the target schema, but that the most important bits about the
 * diff can be captured here.
 * <p>The code integrating with the DBMS is intended to work out the DBMS-
 * specific logic for resolving the difference between the existing schema and
 * the target schema.
 * <p>Each {@link SchemaDiff} should be associated with a table name to have
 * any meaning. The association is not provided within this class.
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
        public abstract Builder category(int category);

        /**
         * @return this {@link Builder}
         * @see SchemaDiff#type()
         */
        public abstract Builder type(int type);

        /**
         * @return this {@link Builder}
         * @see SchemaDiff#subType()
         */
        public abstract Builder subType(int subType);


        /**
         * @param name the name of the attribute
         * @param value the name of the attribute
         * @return this {@link Builder}
         */
        public Builder addAttribute(@Nonnull String name, @Nullable String value) {
            attributes.put(name, value);
            return this;
        }

        public SchemaDiff create() {
            return attributes(attributes).build();
        }

        /**
         * @return this {@link Builder}
         * @see SchemaDiff#attributes()
         */
        abstract Builder attributes(@Nonnull Map<String, String> attributes);

        /**
         * @return The {@link SchemaDiff} built up by this builder
         */
        abstract SchemaDiff build();
    }
    
    public static final int TYPE_CREATED = 0;
    public static final int TYPE_CHANGED = TYPE_CREATED + 1;
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
     * <p>The foreign key of a table changed.
     * <p>Relevant attributes:
     * <ul>
     *   <li>{@link #ATTR_PREV_SORT}</li>
     *   <li>{@link #ATTR_CURR_SORT}</li>
     * </ul>
     */
    public static final int TYPE_FOREIGN_KEY = TYPE_SORT << 1;                      // 0b100000000

    /**
     * <p>The constraint of a column in a table changed.
     * <p>Relevant attributes:
     * <ul>
     *   <li>{@link #ATTR_CONSTRAINT}</li>
     *   <li>{@link #ATTR_PREV_CONSTRAINT_VAL}</li>
     *   <li>{@link #ATTR_CURR_CONSTRAINT_VAL}</li>
     * </ul>
     */
    public static final int TYPE_CONSTRAINT = TYPE_FOREIGN_KEY << 1;                // 0b1000000000

    /**
     * <p>The default value of a column in a table changed.
     * <p>Relevant attributes:
     * <ul>
     *   <li>{@link #ATTR_PREV_DEFAULT}</li>
     *   <li>{@link #ATTR_CURR_DEFAULT}</li>
     * </ul>
     */
    public static final int TYPE_DEFAULT = TYPE_FOREIGN_KEY << 1;                   // 0b10000000000

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
     * <p>The diff pertains to a column of a table only. The table could be
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
     *   <li>column added {@link #TYPE_FOREIGN_KEY}</li>
     *   <li>column removed {@link #TYPE_FOREIGN_KEY}</li>
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

    public static final String ATTR_CURR_NAME = "c_name";
    public static final String ATTR_PREV_NAME = "p_name";
    public static final String ATTR_PREV_TYPE = "p_type";
    public static final String ATTR_CURR_TYPE = "c_type";
    public static final String ATTR_PREV_COL_NAMES = "p_col_names";
    public static final String ATTR_CURR_COL_NAMES = "c_col_names";
    public static final String ATTR_PREV_SORT = "p_sort";
    public static final String ATTR_CURR_SORT = "c_sort";
    public static final String ATTR_CONSTRAINT = "constraint";
    public static final String ATTR_PREV_CONSTRAINT_VAL = "p_constraint_val";
    public static final String ATTR_CURR_CONSTRAINT_VAL = "c_constraint_val";
    public static final String ATTR_PREV_DEFAULT = "p_default";
    public static final String ATTR_CURR_DEFAULT = "c_default";

    /**
     * <p>The previous foreign key
     * <p>The format of the string of this attribute is:
     * {@code "foreign_table,local_column1,foreign_column1,local_column2,foreign_column2}
     */
    public static final String ATTR_PREV_FK = "p_fk";
    /**
     * <p>The current foreign key
     * @see #ATTR_PREV_FK for string format
     */
    public static final String ATTR_CURR_FK = "c_fk";

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
                .create();
    }

    public static SchemaDiff forColumnCreated(String colName) {
        return builder()
                .type(TYPE_CREATED)
                .subType(TYPE_CREATED)
                .category(CAT_COLUMN)
                .addAttribute(ATTR_CURR_NAME, colName)
                .create();
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
    public abstract int category();

    /**
     * <p>There are three types:
     * <ol>
     *   <li>{@link #TYPE_CREATED} - previously did not exist</li>
     *   <li>{@link #TYPE_CHANGED} - previously existed, but changed</li>
     *   <li>{@link #TYPE_DROPPED} - previously existed, but no longer</li>
     * </ol>
     * @return the type of this diff
     */
    public abstract int type();

    /**
     * <p>If the {@link #type()} is {@link #TYPE_CHANGED}, then
     * {@link #subType()} will describe the kind of information available in
     * {@link #attributes()}. If the {@link #type()} is {@link #TYPE_CREATED}
     * or {@link #TYPE_DROPPED}, then {@link #subType()} and {@link #type()}
     * will return the same value.
     * @return the subtype of this diff
     */
    public abstract int subType();

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
    @Nonnull public abstract Map<String, String> attributes();
}
