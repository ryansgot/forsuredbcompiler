package com.fsryan.forsuredb.annotationprocessor.generator;

import com.fsryan.forsuredb.annotationprocessor.TableContext;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.migration.SchemaDiff;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static com.fsryan.forsuredb.info.DBInfoFixtures.tableBuilder;
import static com.fsryan.forsuredb.info.TableInfoUtil.tableFQClassName;
import static com.fsryan.forsuredb.info.TableInfoUtil.tableMapOf;
import static com.fsryan.forsuredb.test.assertions.AssertCollection.assertMapEquals;
import static com.fsryan.forsuredb.test.assertions.AssertCollection.assertSetEquals;
import static com.fsryan.forsuredb.test.tools.CollectionUtil.mapOf;
import static com.fsryan.forsuredb.test.tools.CollectionUtil.setOf;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class SchemaDiffGeneratorTest {

    public static Iterable<Arguments> tableCreateFromZeroInput() {
        return Arrays.asList(
                arguments(
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder("t1")
                                        .build()
                        )),
                        mapOf(
                                tableFQClassName("t1"),
                                setOf(SchemaDiff.forTableCreated("t1"))
                        )

                )
        );
    }

    @ParameterizedTest
    @MethodSource("tableCreateFromZeroInput")
    @DisplayName("Table creation from zero should be represented as the correct set of diffs")
    public void tableCreateFromZero(TableContext target, Map<String, Set<SchemaDiff>> expected) {
        SchemaDiffGenerator generator = new SchemaDiffGenerator(TableContext.empty());
        Map<String, Set<SchemaDiff>> actual = generator.generate(target);

        expected.forEach((tableClassName, expectedDiffSet) -> {
            Set<SchemaDiff> actualDiffSet = actual.get(tableClassName);
            assertSetEquals("mismatched diff set at key " + actualDiffSet, expectedDiffSet, actualDiffSet);
        });
        assertMapEquals(expected, actual);
    }
}
