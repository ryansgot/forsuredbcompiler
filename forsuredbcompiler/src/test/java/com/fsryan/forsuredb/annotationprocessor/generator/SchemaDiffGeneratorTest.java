package com.fsryan.forsuredb.annotationprocessor.generator;

import com.fsryan.forsuredb.annotationprocessor.TableContext;
import com.fsryan.forsuredb.migration.SchemaDiff;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.fsryan.forsuredb.info.ColumnInfoUtil.colNameByType;
import static com.fsryan.forsuredb.info.DBInfoFixtures.cascadeForeignKeyTo;
import static com.fsryan.forsuredb.info.DBInfoFixtures.longCol;
import static com.fsryan.forsuredb.info.DBInfoFixtures.tableBuilder;
import static com.fsryan.forsuredb.info.TableInfoUtil.tableFQClassName;
import static com.fsryan.forsuredb.info.TableInfoUtil.tableMapOf;
import static com.fsryan.forsuredb.test.assertions.AssertCollection.assertMapEquals;
import static com.fsryan.forsuredb.test.assertions.AssertCollection.assertSetEquals;
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
                                setOf(SchemaDiff.forTableCreated("t1"))
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
                                setOf(SchemaDiff.forTableCreated("t1")),
                                tableFQClassName("t2"),
                                setOf(SchemaDiff.forTableCreated("t2"))
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
                                setOf(SchemaDiff.forTableCreated("t1"))
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
                                setOf(SchemaDiff.forTableCreated("t1"))
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
                                setOf(SchemaDiff.forTableCreated("t1"))
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
                                setOf(SchemaDiff.forTableCreated("t1")),
                                tableFQClassName("t2"),
                                setOf(SchemaDiff.forTableCreated("t2"))
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
                                setOf(SchemaDiff.forTableDropped("t1"))
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
                                setOf(SchemaDiff.forTableDropped("t1")),
                                tableFQClassName("t2"),
                                setOf(SchemaDiff.forTableDropped("t2"))
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
                                setOf(SchemaDiff.forTableDropped("t1")),
                                tableFQClassName("t2"),
                                setOf(SchemaDiff.forTableCreated("t2"))
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
                                setOf(SchemaDiff.forTableRenamed("t1", "t1_renamed"))
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
                                setOf(SchemaDiff.forTableRenamed("t1", "t1_renamed")),
                                tableFQClassName("t2"),
                                setOf(SchemaDiff.forTableRenamed("t2", "t2_renamed"))
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
                                setOf(SchemaDiff.forTableRenamed("t1", "t1_renamed")),
                                tableFQClassName("t2"),
                                setOf(SchemaDiff.forTableCreated("t2"))
                        )
                )
        );
    }

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("tableCreateFromZeroInput")
    @DisplayName("Table creation from zero should be represented as the correct set of create table diffs")
    public void tableCreateFromZero(String desc, TableContext target, Map<String, Set<SchemaDiff>> expected) {
        runTest(desc, TableContext.empty(), target, expected);
    }

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("dropTableInput")
    @DisplayName("Dropping a table should be detected")
    public void dropTable(String desc, TableContext base, TableContext target, Map<String, Set<SchemaDiff>> expected) {
        runTest(desc, base, target, expected);
    }

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("tableRenameInput")
    @DisplayName("Renaming a table should be detected")
    public void renameTable(String desc, TableContext base, TableContext target, Map<String, Set<SchemaDiff>> expected) {
        runTest(desc, base, target, expected);
    }

    static void runTest(String desc, TableContext base, TableContext target, Map<String, Set<SchemaDiff>> expected) {
        Map<String, Set<SchemaDiff>> actual = new SchemaDiffGenerator(base).generate(target);
        expected.forEach((tableClassName, expectedDiffSet) -> {
            Set<SchemaDiff> actualDiffSet = actual.get(tableClassName);
            assertSetEquals(desc + "; mismatched diff set at key " + tableClassName, expectedDiffSet, actualDiffSet);
        });
        assertMapEquals(desc, expected, actual);
    }
}
