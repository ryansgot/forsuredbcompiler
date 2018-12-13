package com.fsryan.forsuredb.annotationprocessor.generator;

import com.fsryan.forsuredb.annotationprocessor.TableContext;
import com.fsryan.forsuredb.migration.SchemaDiff;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nonnull;
import java.util.*;

import static com.fsryan.forsuredb.info.ColumnInfoUtil.colNameByType;
import static com.fsryan.forsuredb.info.DBInfoFixtures.*;
import static com.fsryan.forsuredb.info.TableInfoUtil.tableFQClassName;
import static com.fsryan.forsuredb.info.TableInfoUtil.tableMapOf;
import static com.fsryan.forsuredb.test.assertions.AssertCollection.assertMapEquals;
import static com.fsryan.forsuredb.test.tools.CollectionUtil.mapOf;
import static com.fsryan.forsuredb.test.tools.CollectionUtil.setOf;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class SchemaDiffGeneratorTest {

    static Iterable<Arguments> tableCreateFromZeroInput() {
        return Arrays.asList(
                arguments(
                        "Single table",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                SchemaDiff.forTableCreated("t1")
                        )
                ),
                arguments(
                        "Multiple tables",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .build(),
                                tableBuilder("t2")
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                SchemaDiff.forTableCreated("t1"),
                                tableFQClassName("t2"),
                                SchemaDiff.forTableCreated("t2")
                        )
                ),
                arguments(
                        "Table with extra columns should not generate column diffs",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                SchemaDiff.forTableCreated("t1")
                        )
                ),
                arguments(
                        "Table with non-default primary key should not generate primary key column diff",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .resetPrimaryKey(setOf(colNameByType(long.class)))
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                SchemaDiff.forTableCreated("t1")
                        )
                ),
                // TODO: update this test when composite indices are possible
                arguments(
                        "Table with index should not generate index diff",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().unique(true).index(true).build())
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                SchemaDiff.forTableCreated("t1")
                        )
                ),
                arguments(
                        "Table with foreign key should not generate foreign key diff",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().unique(true).index(true).build())
                                        .build(),
                                tableBuilder("t2")
                                        .addColumn(longCol().build())
                                        .addForeignKey(cascadeForeignKeyTo("t1")
                                                .mapLocalToForeignColumn(colNameByType(long.class), colNameByType(long.class))
                                                .build()
                                        ).build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                SchemaDiff.forTableCreated("t1"),
                                tableFQClassName("t2"),
                                SchemaDiff.forTableCreated("t2")
                        )
                )
        );
    }

    public static Iterable<Arguments> dropTableInput() {
        return Arrays.asList(
                arguments(
                        "Single table",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .build()
                        )),
                        TableContext.fromSchema(Collections.emptyMap()),
                        mapOf(
                                tableFQClassName("t1"),
                                SchemaDiff.forTableDropped("t1")
                        )
                ),
                arguments(
                        "Multiple tables",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .build(),
                                tableBuilder("t2")
                                        .build()
                        )),
                        TableContext.fromSchema(Collections.emptyMap()),
                        mapOf(
                                tableFQClassName("t1"),
                                SchemaDiff.forTableDropped("t1"),
                                tableFQClassName("t2"),
                                SchemaDiff.forTableDropped("t2")
                        )
                ),
                arguments(
                        "One table added, one table removed",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t2")
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                SchemaDiff.forTableDropped("t1"),
                                tableFQClassName("t2"),
                                SchemaDiff.forTableCreated("t2")
                        )
                )
        );
    }

    public static Iterable<Arguments> tableRenameInput() {
        return Arrays.asList(
                arguments(
                        "Single table; should not be counted as dropped",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1_renamed")
                                        .qualifiedClassName(tableFQClassName("t1")) // <-- ensures same key
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                tableRenameDiff("t1", "t1_renamed")
                        )
                ),
                arguments(
                        "Multiple tables; neither should be counted as dropped",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .build(),
                                tableBuilder("t2")
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1_renamed")
                                        .qualifiedClassName(tableFQClassName("t1")) // <-- ensures same key
                                        .build(),
                                tableBuilder("t2_renamed")
                                        .qualifiedClassName(tableFQClassName("t2")) // <-- ensures same key
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                tableRenameDiff("t1", "t1_renamed"),
                                tableFQClassName("t2"),
                                tableRenameDiff("t2", "t2_renamed")
                        )
                ),
                arguments(
                        "One table added; one table renamed",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1_renamed")
                                        .qualifiedClassName(tableFQClassName("t1")) // <-- ensures same key
                                        .build(),
                                tableBuilder("t2")
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                tableRenameDiff("t1", "t1_renamed"),
                                tableFQClassName("t2"),
                                SchemaDiff.forTableCreated("t2")
                        )
                )
        );
    }

    public static Iterable<Arguments> changePrimaryKeyInput() {
        return Arrays.asList(
                arguments(
                        "Single table: change primary key column from default to different column",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .resetPrimaryKey(setOf(colNameByType(long.class)))
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                diffForPrimaryKeyChange(
                                        "t1",
                                        "",
                                        "",
                                        "_id",
                                        colNameByType(long.class)
                                )
                        )
                ),
                arguments(
                        "Single table: change primary key on conflict from none to REPLACE",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .resetPrimaryKey(setOf(colNameByType(long.class)))
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .resetPrimaryKey(setOf(colNameByType(long.class)))
                                        .primaryKeyOnConflict("REPLACE")
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                diffForPrimaryKeyChange(
                                        "t1",
                                        "",
                                        "REPLACE",
                                        colNameByType(long.class),
                                        colNameByType(long.class)
                                )
                        )
                ),
                arguments(
                        "Single table: change primary key on conflict from REPLACE to ABORT",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .resetPrimaryKey(setOf(colNameByType(long.class)))
                                        .primaryKeyOnConflict("REPLACE")
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .resetPrimaryKey(setOf(colNameByType(long.class)))
                                        .primaryKeyOnConflict("ABORT")
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                diffForPrimaryKeyChange(
                                        "t1",
                                        "REPLACE",
                                        "ABORT",
                                        colNameByType(long.class),
                                        colNameByType(long.class)
                                )
                        )
                ),
                arguments(
                        "Single table: change both the columns and the on conflict behavior at once",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .resetPrimaryKey(setOf(colNameByType(long.class)))
                                        .primaryKeyOnConflict("FAIL")
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                diffForPrimaryKeyChange(
                                        "t1",
                                        "",
                                        "FAIL",
                                        "_id",
                                        colNameByType(long.class)
                                )
                        )
                ),
                arguments(
                        "Single table: change composite primary key columns only including existing columns",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .addColumn(intCol().build())
                                        .addColumn(stringCol().build())
                                        .resetPrimaryKey(setOf(colNameByType(long.class), colNameByType(int.class)))
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .addColumn(intCol().build())
                                        .addColumn(stringCol().build())
                                        .resetPrimaryKey(setOf(
                                                colNameByType(long.class),
                                                colNameByType(int.class),
                                                colNameByType(String.class)
                                        )).build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                diffForPrimaryKeyChange(
                                        "t1",
                                        "",
                                        "",
                                        String.format("%s,%s", colNameByType(int.class), colNameByType(long.class)),
                                        String.format("%s,%s,%s", colNameByType(int.class), colNameByType(long.class), colNameByType(String.class))
                                )
                        )
                )
        );
    }

    public static Iterable<Arguments> addColumnsInput() {
        return Arrays.asList(
                arguments(
                        "Single table; add one column",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                addColumnsDiff("t1", colNameByType(long.class))
                        )
                ),
                arguments(
                        "Single table; add two columns",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .addColumn(intCol().build())
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                addColumnsDiff(
                                        "t1",
                                        String.format("%s,%s", colNameByType(int.class), colNameByType(long.class))
                                )
                        )
                ),
                arguments(
                        "Multiple tables; add two columns each",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .build(),
                                tableBuilder("t2")
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .addColumn(intCol().build())
                                        .build(),
                                tableBuilder("t2")
                                        .addColumn(longCol().build())
                                        .addColumn(intCol().build())
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                addColumnsDiff(
                                        "t1",
                                        String.format("%s,%s", colNameByType(int.class), colNameByType(long.class))
                                ),
                                tableFQClassName("t2"),
                                addColumnsDiff(
                                        "t2",
                                        String.format("%s,%s", colNameByType(int.class), colNameByType(long.class))
                                )
                        )
                )
        );
    }

    public static Iterable<Arguments> dropColumnsInput() {
        return Arrays.asList(
                arguments(
                        "Single table; drop one column",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                dropColumnsDiff("t1", colNameByType(long.class))
                        )
                ),
                arguments(
                        "Single table; drop two columns",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .addColumn(intCol().build())
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                dropColumnsDiff(
                                        "t1",
                                        String.format("%s,%s", colNameByType(int.class), colNameByType(long.class))
                                )
                        )
                ),
                arguments(
                        "Multiple tables; drop two columns each",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .addColumn(intCol().build())
                                        .build(),
                                tableBuilder("t2")
                                        .addColumn(longCol().build())
                                        .addColumn(intCol().build())
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .build(),
                                tableBuilder("t2")
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                dropColumnsDiff(
                                        "t1",
                                        String.format("%s,%s", colNameByType(int.class), colNameByType(long.class))
                                ),
                                tableFQClassName("t2"),
                                dropColumnsDiff(
                                        "t2",
                                        String.format("%s,%s", colNameByType(int.class), colNameByType(long.class))
                                )
                        )
                )
        );
    }

    public static Iterable<Arguments> changeColumnNamesInput() {
        return Arrays.asList(
                arguments(
                        "Single table; change one column name",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().columnName("long_col_renamed").build())
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                columnNameChangeDiff(
                                        "t1",
                                        String.format("%s=%s", colNameByType(long.class), "long_col_renamed")
                                )
                        )
                ),
                arguments(
                        "Single table; change multiple column names",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .addColumn(stringCol().build())
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().columnName("long_col_renamed").build())
                                        .addColumn(stringCol().columnName("string_col_renamed").build())
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                columnNameChangeDiff(
                                        "t1",
                                        String.format(
                                                "%s=%s,%s=%s",
                                                colNameByType(long.class),
                                                "long_col_renamed",
                                                colNameByType(String.class),
                                                "string_col_renamed"
                                        )
                                )
                        )
                ),
                arguments(
                        "Multiple tables; change multiple column names",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .addColumn(stringCol().build())
                                        .build(),
                                tableBuilder("t2")
                                        .addColumn(intCol().build())
                                        .addColumn(booleanCol().build())
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t2")
                                        .addColumn(intCol().columnName("int_col_renamed").build())
                                        .addColumn(booleanCol().columnName("boolean_col_renamed").build())
                                        .build(),
                                tableBuilder("t1")
                                        .addColumn(longCol().columnName("long_col_renamed").build())
                                        .addColumn(stringCol().columnName("string_col_renamed").build())
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                columnNameChangeDiff(
                                        "t1",
                                        String.format(
                                                "%s=%s,%s=%s",
                                                colNameByType(long.class),
                                                "long_col_renamed",
                                                colNameByType(String.class),
                                                "string_col_renamed"
                                        )
                                ),
                                tableFQClassName("t2"),
                                columnNameChangeDiff(
                                        "t2",
                                        String.format(
                                                "%s=%s,%s=%s",
                                                colNameByType(boolean.class),
                                                "boolean_col_renamed",
                                                colNameByType(int.class),
                                                "int_col_renamed"
                                        )
                                )
                        )
                )
        );
    }

    public static Iterable<Arguments> changeColumnDefaultInput() {
        return Arrays.asList(
                arguments(
                        "Single table; change one column default",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(dateCol().defaultValue("1970-01-01 00:00:00.000").build())
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(dateCol().defaultValue("CURRENT_TIMESTAMP").build())
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                columnDefaultDiff(
                                        "t1",
                                        String.format("%s=%s", colNameByType(Date.class), "CURRENT_TIMESTAMP")
                                )
                        )
                ),
                arguments(
                        "Single table; int_col from no default to default; date_col none to none",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(dateCol().build())
                                        .addColumn(intCol().build())
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(dateCol().build())
                                        .addColumn(intCol().defaultValue("5").build())
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                columnDefaultDiff(
                                        "t1",
                                        String.format("%s=%s", colNameByType(int.class), "5")
                                )
                        )
                ),
                arguments(
                        "Single table; multiple column default change",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(dateCol().build())
                                        .addColumn(intCol().build())
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(dateCol().defaultValue("CURRENT_TIMESTAMP").build())
                                        .addColumn(intCol().defaultValue("5").build())
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                columnDefaultDiff(
                                        "t1",
                                        String.format(
                                                "%s=%s,%s=%s",
                                                colNameByType(Date.class),
                                                "CURRENT_TIMESTAMP",
                                                colNameByType(int.class),
                                                "5"
                                        )
                                )
                        )
                ),
                arguments(
                        "Multiple table; multiple column default change",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(dateCol().build())
                                        .addColumn(intCol().build())
                                        .build(),
                                tableBuilder("t2")
                                        .addColumn(booleanCol().defaultValue("false").build())
                                        .addColumn(floatCol().defaultValue("0.01").build())
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(dateCol().defaultValue("CURRENT_TIMESTAMP").build())
                                        .addColumn(intCol().defaultValue("5").build())
                                        .build(),
                                tableBuilder("t2")
                                        .addColumn(booleanCol().defaultValue("true").build())
                                        .addColumn(floatCol().defaultValue("1.01").build())
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                columnDefaultDiff(
                                        "t1",
                                        String.format(
                                                "%s=%s,%s=%s",
                                                colNameByType(Date.class),
                                                "CURRENT_TIMESTAMP",
                                                colNameByType(int.class),
                                                "5"
                                        )
                                ),
                                tableFQClassName("t2"),
                                columnDefaultDiff(
                                        "t2",
                                        String.format(
                                                "%s=%s,%s=%s",
                                                colNameByType(boolean.class),
                                                "true",
                                                colNameByType(float.class),
                                                "1.01"
                                        )
                                )
                        )
                )
        );
    }

    public static Iterable<Arguments> changeColumnConstraintInput() {
        return Arrays.asList(
                arguments(
                        "Single table; make existing column unique",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(stringCol().unique(false).build())
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(stringCol().unique(true).build())
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                columnConstraintDiff(
                                        "t1",
                                        String.format("%s:%s=%s", colNameByType(String.class), SchemaDiff.CONSTRAINT_UNIQUE, "true")
                                )
                        )
                ),
                arguments(
                        "Single table; make existing unique column non unique",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(stringCol().unique(true).build())
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(stringCol().unique(false).build())
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                columnConstraintDiff(
                                        "t1",
                                        String.format("%s:%s=%s", colNameByType(String.class), SchemaDiff.CONSTRAINT_UNIQUE, "false")
                                )
                        )
                ),
                arguments(
                        "Single table; make changes to uniqueness constraint of multiple columns",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(stringCol().unique(true).build())
                                        .addColumn(intCol().unique(false).build())
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(stringCol().unique(false).build())
                                        .addColumn(intCol().unique(true).build())
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                columnConstraintDiff(
                                        "t1",
                                        String.format(
                                                "%s:%s=%s,%s:%s=%s",
                                                colNameByType(int.class), SchemaDiff.CONSTRAINT_UNIQUE, "true",
                                                colNameByType(String.class), SchemaDiff.CONSTRAINT_UNIQUE, "false"
                                        )
                                )
                        )
                ),
                arguments(
                        "Multiple table; make changes to uniqueness constraint of multiple columns",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(stringCol().unique(true).build())
                                        .addColumn(intCol().unique(false).build())
                                        .build(),
                                tableBuilder("t2")
                                        .addColumn(stringCol().unique(false).build())
                                        .addColumn(intCol().unique(true).build())
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(stringCol().unique(false).build())
                                        .addColumn(intCol().unique(true).build())
                                        .build(),
                                tableBuilder("t2")
                                        .addColumn(stringCol().unique(true).build())
                                        .addColumn(intCol().unique(false).build())
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                columnConstraintDiff(
                                        "t1",
                                        String.format(
                                                "%s:%s=%s,%s:%s=%s",
                                                colNameByType(int.class), SchemaDiff.CONSTRAINT_UNIQUE, "true",
                                                colNameByType(String.class), SchemaDiff.CONSTRAINT_UNIQUE, "false"
                                        )
                                ),
                                tableFQClassName("t2"),
                                columnConstraintDiff(
                                        "t2",
                                        String.format(
                                                "%s:%s=%s,%s:%s=%s",
                                                colNameByType(int.class), SchemaDiff.CONSTRAINT_UNIQUE, "false",
                                                colNameByType(String.class), SchemaDiff.CONSTRAINT_UNIQUE, "true"
                                        )
                                )
                        )
                )
        );
    }

    static Iterable<Arguments> changeForeignKeyInput() {
        return Arrays.asList(
                arguments(
                        "Single table; add a non-composite foreign key referencing an existing table using a new column",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .build(),
                                tableBuilder("t2")
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .addForeignKey(
                                                cascadeForeignKeyTo("t2")
                                                        .mapLocalToForeignColumn(colNameByType(long.class), "_id")
                                                        .build()
                                        ).build(),
                                tableBuilder("t2")
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                SchemaDiff.builder()
                                        .type(SchemaDiff.TYPE_CHANGED)
                                        .enrichSubType(SchemaDiff.TYPE_ADD_COLUMNS)
                                        .enrichSubType(SchemaDiff.TYPE_CREATE_FK)
                                        .tableName("t1")
                                        .addAttribute(SchemaDiff.ATTR_CURR_NAME, "t1")
                                        .addAttribute(SchemaDiff.ATTR_CREATE_COLUMNS, colNameByType(long.class))
                                        .addAttribute(
                                                SchemaDiff.ATTR_CREATED_FKS,
                                                String.format("%s:%s=%s:%s:%s", "t2", colNameByType(long.class), "_id", "CASCADE", "CASCADE")
                                        ).build()
                        )
                ),
                arguments(
                        "Single table; add a non-composite foreign key referencing an existing table using an existing column",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .build(),
                                tableBuilder("t2")
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .addForeignKey(
                                                cascadeForeignKeyTo("t2")
                                                        .mapLocalToForeignColumn(colNameByType(long.class), "_id")
                                                        .build()
                                        ).build(),
                                tableBuilder("t2")
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                SchemaDiff.builder()
                                        .type(SchemaDiff.TYPE_CHANGED)
                                        .enrichSubType(SchemaDiff.TYPE_CREATE_FK)
                                        .tableName("t1")
                                        .addAttribute(SchemaDiff.ATTR_CURR_NAME, "t1")
                                        .addAttribute(
                                                SchemaDiff.ATTR_CREATED_FKS,
                                                String.format("%s:%s=%s:%s:%s", "t2", colNameByType(long.class), "_id", "CASCADE", "CASCADE")
                                        ).build()
                        )
                ),
                arguments(
                        "Single table; add a non-composite foreign key referencing an new table using an existing column",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .addForeignKey(
                                                cascadeForeignKeyTo("t2")
                                                        .mapLocalToForeignColumn(colNameByType(long.class), "_id")
                                                        .build()
                                        ).build(),
                                tableBuilder("t2")
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                SchemaDiff.builder()
                                        .type(SchemaDiff.TYPE_CHANGED)
                                        .enrichSubType(SchemaDiff.TYPE_CREATE_FK)
                                        .tableName("t1")
                                        .addAttribute(SchemaDiff.ATTR_CURR_NAME, "t1")
                                        .addAttribute(
                                                SchemaDiff.ATTR_CREATED_FKS,
                                                String.format("%s:%s=%s:%s:%s", "t2", colNameByType(long.class), "_id", "CASCADE", "CASCADE")
                                        ).build(),
                                tableFQClassName("t2"),
                                SchemaDiff.forTableCreated("t2")
                        )
                ),
                arguments(
                        "Single table; add composite foreign key referencing an existing table using an existing column and a new column; update and delete actions get put in correct spot",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .build(),
                                tableBuilder("t2")
                                        .resetPrimaryKey(setOf(colNameByType(long.class), colNameByType(String.class)))
                                        .addColumn(longCol().build())
                                        .addColumn(stringCol().build())
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .addColumn(stringCol().build())
                                        .addForeignKey(
                                                foreignKeyTo("t2")
                                                        .updateChangeAction("NO ACTION")
                                                        .deleteChangeAction("CASCADE")
                                                        .mapLocalToForeignColumn(colNameByType(long.class), colNameByType(long.class))
                                                        .mapLocalToForeignColumn(colNameByType(String.class), colNameByType(String.class))
                                                        .build()
                                        ).build(),
                                tableBuilder("t2")
                                        .resetPrimaryKey(setOf(colNameByType(long.class), colNameByType(String.class)))
                                        .addColumn(longCol().build())
                                        .addColumn(stringCol().build())
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                SchemaDiff.builder()
                                        .type(SchemaDiff.TYPE_CHANGED)
                                        .enrichSubType(SchemaDiff.TYPE_ADD_COLUMNS)
                                        .enrichSubType(SchemaDiff.TYPE_CREATE_FK)
                                        .tableName("t1")
                                        .addAttribute(SchemaDiff.ATTR_CURR_NAME, "t1")
                                        .addAttribute(SchemaDiff.ATTR_CREATE_COLUMNS, colNameByType(String.class))
                                        .addAttribute(
                                                SchemaDiff.ATTR_CREATED_FKS,
                                                String.format("%s:%s=%s,%s=%s:%s:%s",
                                                        "t2",
                                                        colNameByType(long.class), colNameByType(long.class),
                                                        colNameByType(String.class), colNameByType(String.class),
                                                        "NO ACTION",
                                                        "CASCADE"
                                                )
                                        ).build()
                        )
                ),
                arguments(
                        "Single table; drop a non-composite foreign key referencing an existing table, dropping the column",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .addForeignKey(
                                                cascadeForeignKeyTo("t2")
                                                        .mapLocalToForeignColumn(colNameByType(long.class), "_id")
                                                        .build()
                                        ).build(),
                                tableBuilder("t2")
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .build(),
                                tableBuilder("t2")
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                SchemaDiff.builder()
                                        .type(SchemaDiff.TYPE_CHANGED)
                                        .enrichSubType(SchemaDiff.TYPE_DROP_COLUMNS)
                                        .enrichSubType(SchemaDiff.TYPE_DROP_FK)
                                        .tableName("t1")
                                        .addAttribute(SchemaDiff.ATTR_CURR_NAME, "t1")
                                        .addAttribute(SchemaDiff.ATTR_DROP_COLUMNS, colNameByType(long.class))
                                        .addAttribute(
                                                SchemaDiff.ATTR_DROPPED_FKS,
                                                String.format("%s:%s=%s:%s:%s", "t2", colNameByType(long.class), "_id", "CASCADE", "CASCADE")
                                        ).build()
                        )
                ),
                arguments(
                        "Single table; drop a non-composite foreign key referencing an existing table without dropping the column",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .addForeignKey(
                                                cascadeForeignKeyTo("t2")
                                                        .mapLocalToForeignColumn(colNameByType(long.class), "_id")
                                                        .build()
                                        ).build(),
                                tableBuilder("t2")
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .build(),
                                tableBuilder("t2")
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                SchemaDiff.builder()
                                        .type(SchemaDiff.TYPE_CHANGED)
                                        .enrichSubType(SchemaDiff.TYPE_DROP_FK)
                                        .tableName("t1")
                                        .addAttribute(SchemaDiff.ATTR_CURR_NAME, "t1")
                                        .addAttribute(
                                                SchemaDiff.ATTR_DROPPED_FKS,
                                                String.format("%s:%s=%s:%s:%s", "t2", colNameByType(long.class), "_id", "CASCADE", "CASCADE")
                                        ).build()
                        )
                ),
                arguments(
                        "Single table; drop composite foreign key referencing an existing table, dropping one of the columns",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .addColumn(stringCol().build())
                                        .addForeignKey(
                                                foreignKeyTo("t2")
                                                        .updateChangeAction("NO ACTION")
                                                        .deleteChangeAction("CASCADE")
                                                        .mapLocalToForeignColumn(colNameByType(long.class), colNameByType(long.class))
                                                        .mapLocalToForeignColumn(colNameByType(String.class), colNameByType(String.class))
                                                        .build()
                                        ).build(),
                                tableBuilder("t2")
                                        .resetPrimaryKey(setOf(colNameByType(long.class), colNameByType(String.class)))
                                        .addColumn(longCol().build())
                                        .addColumn(stringCol().build())
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .build(),
                                tableBuilder("t2")
                                        .resetPrimaryKey(setOf(colNameByType(long.class), colNameByType(String.class)))
                                        .addColumn(longCol().build())
                                        .addColumn(stringCol().build())
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                SchemaDiff.builder()
                                        .type(SchemaDiff.TYPE_CHANGED)
                                        .enrichSubType(SchemaDiff.TYPE_DROP_COLUMNS)
                                        .enrichSubType(SchemaDiff.TYPE_DROP_FK)
                                        .tableName("t1")
                                        .addAttribute(SchemaDiff.ATTR_CURR_NAME, "t1")
                                        .addAttribute(SchemaDiff.ATTR_DROP_COLUMNS, colNameByType(String.class))
                                        .addAttribute(
                                                SchemaDiff.ATTR_DROPPED_FKS,
                                                String.format("%s:%s=%s,%s=%s:%s:%s",
                                                        "t2",
                                                        colNameByType(long.class), colNameByType(long.class),
                                                        colNameByType(String.class), colNameByType(String.class),
                                                        "NO ACTION",
                                                        "CASCADE"
                                                )
                                        ).build()
                        )
                ),
                arguments(
                        "Single table; drop a foreign key and add a foreign key",
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .addColumn(stringCol().build())
                                        .addForeignKey(
                                                cascadeForeignKeyTo("t2")
                                                        .mapLocalToForeignColumn(colNameByType(long.class), "_id")
                                                        .build()
                                        ).build(),
                                tableBuilder("t2")
                                        .addColumn(stringCol().unique(true).build())
                                        .build()
                        )),
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .addColumn(stringCol().build())
                                        .addForeignKey(
                                                cascadeForeignKeyTo("t2")
                                                        .mapLocalToForeignColumn(colNameByType(String.class), colNameByType(String.class))
                                                        .build()
                                        ).build(),
                                tableBuilder("t2")
                                        .addColumn(stringCol().unique(true).build())
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                SchemaDiff.builder()
                                        .type(SchemaDiff.TYPE_CHANGED)
                                        .enrichSubType(SchemaDiff.TYPE_DROP_FK)
                                        .enrichSubType(SchemaDiff.TYPE_CREATE_FK)
                                        .tableName("t1")
                                        .addAttribute(SchemaDiff.ATTR_CURR_NAME, "t1")
                                        .addAttribute(
                                                SchemaDiff.ATTR_DROPPED_FKS,
                                                String.format("%s:%s=%s:%s:%s",
                                                        "t2",
                                                        colNameByType(long.class), "_id",
                                                        "CASCADE",
                                                        "CASCADE"
                                                )
                                        ).addAttribute(
                                                SchemaDiff.ATTR_CREATED_FKS,
                                                String.format("%s:%s=%s:%s:%s",
                                                        "t2",
                                                        colNameByType(String.class), colNameByType(String.class),
                                                        "CASCADE",
                                                        "CASCADE"
                                                )
                                        ).build()
                        )
                )
        );
    }

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("tableCreateFromZeroInput")
    @DisplayName("Table creation from zero should be represented as the correct set of create table diffs")
    public void tableCreateFromZero(String desc, TableContext target, Map<String, SchemaDiff> expected) {
        Map<String, SchemaDiff> actual = new SchemaDiffGenerator(TableContext.empty()).generate(target);
        assertMapEquals(desc, expected, actual);
    }

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("dropTableInput")
    @DisplayName("Dropping a table should be detected")
    public void dropTable(String desc, TableContext base, TableContext target, Map<String, SchemaDiff> expected) {
        Map<String, SchemaDiff> actual = new SchemaDiffGenerator(base).generate(target);
        assertMapEquals(desc, expected, actual);
    }

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("tableRenameInput")
    @DisplayName("Renaming a table should be detected")
    public void renameTable(String desc, TableContext base, TableContext target, Map<String, SchemaDiff> expected) {
        Map<String, SchemaDiff> actual = new SchemaDiffGenerator(base).generate(target);
        assertMapEquals(desc, expected, actual);
    }

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("changePrimaryKeyInput")
    @DisplayName("Changing primary key on conflict behavior and primary key columns should be detected")
    public void changePrimaryKey(String desc, TableContext base, TableContext target, Map<String, SchemaDiff> expected) {
        Map<String, SchemaDiff> actual = new SchemaDiffGenerator(base).generate(target);
        assertMapEquals(desc, expected, actual);
    }

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("addColumnsInput")
    @DisplayName("New columns of a table should be detected")
    public void addColumns(String desc, TableContext base, TableContext target, Map<String, SchemaDiff> expected) {
        Map<String, SchemaDiff> actual = new SchemaDiffGenerator(base).generate(target);
        assertMapEquals(desc, expected, actual);
    }

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("dropColumnsInput")
    @DisplayName("Dropped columns of a table should be detected")
    public void dropColumns(String desc, TableContext base, TableContext target, Map<String, SchemaDiff> expected) {
        Map<String, SchemaDiff> actual = new SchemaDiffGenerator(base).generate(target);
        assertMapEquals(desc, expected, actual);
    }

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("changeColumnNamesInput")
    @DisplayName("Column name changes should be detected")
    public void changeColumnNames(String desc, TableContext base, TableContext target, Map<String, SchemaDiff> expected) {
        Map<String, SchemaDiff> actual = new SchemaDiffGenerator(base).generate(target);
        assertMapEquals(desc, expected, actual);
    }

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("changeColumnDefaultInput")
    @DisplayName("Column default value changes should be detected")
    public void changeColumnDefault(String desc, TableContext base, TableContext target, Map<String, SchemaDiff> expected) {
        Map<String, SchemaDiff> actual = new SchemaDiffGenerator(base).generate(target);
        assertMapEquals(desc, expected, actual);
    }

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("changeColumnConstraintInput")
    @DisplayName("Column constraint changes should be detected")
    public void changeColumnConstraint(String desc, TableContext base, TableContext target, Map<String, SchemaDiff> expected) {
        Map<String, SchemaDiff> actual = new SchemaDiffGenerator(base).generate(target);
        assertMapEquals(desc, expected, actual);
    }

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("changeForeignKeyInput")
    @DisplayName("Foreign key changes should be detected")
    public void changeForeignKey(String desc, TableContext base, TableContext target, Map<String, SchemaDiff> expected) {
        Map<String, SchemaDiff> actual = new SchemaDiffGenerator(base).generate(target);
        assertMapEquals(desc, expected, actual);
    }

    public static SchemaDiff tableRenameDiff(@Nonnull String previousName, @Nonnull String currentName) {
        return SchemaDiff.builder()
                .type(SchemaDiff.TYPE_CHANGED)
                .replaceSubType(SchemaDiff.TYPE_NAME)
                .tableName(currentName)
                .addAttribute(SchemaDiff.ATTR_PREV_NAME, previousName)
                .addAttribute(SchemaDiff.ATTR_CURR_NAME, currentName)
                .build();
    }

    /**
     * <p>Note: the previousPKCols and currentPKCols must be sorted
     * @param tableName
     * @param previousPKOnConflict
     * @param currentPKOnConflict
     * @param previousPKCols
     * @param currentPKCols
     * @return
     */
    private static SchemaDiff diffForPrimaryKeyChange(@Nonnull String tableName, @Nonnull String previousPKOnConflict,
                                                     @Nonnull String currentPKOnConflict,
                                                     @Nonnull String previousPKCols,
                                                     @Nonnull String currentPKCols) {
        SchemaDiff.Builder builder = SchemaDiff.builder()
                .tableName(tableName)
                .type(SchemaDiff.TYPE_CHANGED)
                .addAttribute(SchemaDiff.ATTR_CURR_NAME, tableName);
        if (!previousPKOnConflict.equals(currentPKOnConflict)) {
            builder.addAttribute(SchemaDiff.ATTR_PREV_PK_ON_CONFLICT, previousPKOnConflict)
                    .addAttribute(SchemaDiff.ATTR_CURR_PK_ON_CONFLICT, currentPKOnConflict)
                    .enrichSubType(SchemaDiff.TYPE_PK_ON_CONFLICT);
        }
        if (!previousPKCols.equals(currentPKCols)) {
            builder.addAttribute(SchemaDiff.ATTR_PREV_PK_COL_NAMES, previousPKCols)
                    .addAttribute(SchemaDiff.ATTR_CURR_PK_COL_NAMES, currentPKCols)
                    .enrichSubType(SchemaDiff.TYPE_PK_COLUMNS);
        }
        return builder.build();
    }

    private static SchemaDiff addColumnsDiff(@Nonnull String tableName, @Nonnull String columnsCSV) {
        return SchemaDiff.builder()
                .type(SchemaDiff.TYPE_CHANGED)
                .tableName(tableName)
                .addAttribute(SchemaDiff.ATTR_CURR_NAME, tableName)
                .enrichSubType(SchemaDiff.TYPE_ADD_COLUMNS)
                .addAttribute(SchemaDiff.ATTR_CREATE_COLUMNS, columnsCSV)
                .build();
    }

    private static SchemaDiff dropColumnsDiff(@Nonnull String tableName, @Nonnull String columnsCSV) {
        return SchemaDiff.builder()
                .type(SchemaDiff.TYPE_CHANGED)
                .tableName(tableName)
                .addAttribute(SchemaDiff.ATTR_CURR_NAME, tableName)
                .enrichSubType(SchemaDiff.TYPE_DROP_COLUMNS)
                .addAttribute(SchemaDiff.ATTR_DROP_COLUMNS, columnsCSV)
                .build();
    }

    private static SchemaDiff columnNameChangeDiff(String tableName, String expectedNameChangeDesc) {
        return SchemaDiff.builder()
                .type(SchemaDiff.TYPE_CHANGED)
                .tableName(tableName)
                .enrichSubType(SchemaDiff.TYPE_RENAME_COLUMNS)
                .addAttribute(SchemaDiff.ATTR_CURR_NAME, tableName)
                .addAttribute(SchemaDiff.ATTR_RENAME_COLUMNS, expectedNameChangeDesc)
                .build();
    }

    private static SchemaDiff columnDefaultDiff(String tableName, String expectedDefaultsDesc) {
        return SchemaDiff.builder()
                .type(SchemaDiff.TYPE_CHANGED)
                .tableName(tableName)
                .enrichSubType(SchemaDiff.TYPE_DEFAULT)
                .addAttribute(SchemaDiff.ATTR_CURR_NAME, tableName)
                .addAttribute(SchemaDiff.ATTR_DEFAULTS, expectedDefaultsDesc)
                .build();
    }

    private static SchemaDiff columnConstraintDiff(String tableName, String expectedConstraintsDesc) {
        return SchemaDiff.builder()
                .type(SchemaDiff.TYPE_CHANGED)
                .tableName(tableName)
                .enrichSubType(SchemaDiff.TYPE_COL_CONSTRAINT)
                .addAttribute(SchemaDiff.ATTR_CURR_NAME, tableName)
                .addAttribute(SchemaDiff.ATTR_COLUMN_CONSTRAINTS, expectedConstraintsDesc)
                .build();
    }
}
