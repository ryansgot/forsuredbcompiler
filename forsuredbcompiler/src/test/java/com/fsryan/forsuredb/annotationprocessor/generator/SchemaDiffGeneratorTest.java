package com.fsryan.forsuredb.annotationprocessor.generator;

import com.fsryan.forsuredb.annotationprocessor.TableContext;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.migration.SchemaDiff;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;

import static com.fsryan.forsuredb.info.Fixtures.*;
import static com.fsryan.forsuredb.test.assertions.AssertCollection.assertMapEquals;
import static com.fsryan.forsuredb.test.tools.CollectionUtil.mapOf;
import static com.fsryan.forsuredb.test.tools.CollectionUtil.setOf;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class SchemaDiffGeneratorTest {

    public static Iterable<Arguments> initialDiffSource() {
        List<Arguments> ret = new ArrayList<>();

        TableContext.Builder builder = new TableContext.Builder();
        ret.add(arguments(
                "No tables should be able to added when there are no tables",
                builder.build(),
                Collections.<String, Set<SchemaDiff>>emptyMap())
        );

        TableInfo.BuilderCompat t1Builder = tableBuilder("t1");
        builder.addTable("t1", tableFQClassName("t1"), t1Builder);
        ret.add(arguments(
                "A single tableBuilder should be able to be added",
                builder.build(),
                mapOf("t1", setOf(SchemaDiff.forTableCreated("t1")))
        ));

        TableInfo.BuilderCompat t2Builder = tableBuilder("t2");
        builder.addTable("t2", tableFQClassName("t2"), t2Builder);
        ret.add(arguments(
                "Multiple tables should be able to be created at the same time",
                builder.build(),
                mapOf("t1", setOf(SchemaDiff.forTableCreated("t1")), "t2", setOf(SchemaDiff.forTableCreated("t2")))
        ));
        return ret;
    }

    public static Iterable<Arguments> subsequentDiffSource() {
        List<Arguments> ret = new ArrayList<>();

        TableContext.Builder baseBuilder = new TableContext.Builder();
        TableContext.Builder targetBuilder = new TableContext.Builder();
        TableInfo.BuilderCompat t1BaseBuilder = tableBuilder("t1");
        TableInfo.BuilderCompat t1TargetBuilder = tableBuilder("t1");

        // TODO: fix this test. Unfortunately, shallow copying is causing
        // further tests to mutate the underlying collections, so it's not
        // working out in a way that would be convenient. Probably you should
        // just handle this by serializing the inputs into json files and
        // deserializing at test time--as deserializing twice would force a
        // deep copy.
        // To be clear, shallow copying in production code is fine--just does
        // not work out to test conveniently in the way below.
        baseBuilder.addTable("t1", tableFQClassName("t1"), t1BaseBuilder);
        targetBuilder.addTable("t1", tableFQClassName("t1"), t1TargetBuilder);
        ret.add(arguments(
                "Diff Generator with equivalent tables should generate no diff",
                baseBuilder.build(),
                targetBuilder.build(),
                Collections.<String, Set<SchemaDiff>>emptyMap()
        ));

        targetBuilder.addColumn("t1", colNameByType(long.class), colBuilder(long.class));
        ret.add(arguments(
                "Diff Generator with column added to table should generate column creation diff",
                baseBuilder.build(),
                targetBuilder.build(),
                mapOf("t1", setOf(SchemaDiff.forColumnCreated(colNameByType(long.class))))
        ));
        baseBuilder.addColumn("t1", colNameByType(long.class), colBuilder(long.class));

        // TODO: rename t1
        // The above is going to force you to key tables by their fq names
        // instead of by their SQL names within the TableContext.Builder

        // TODO: rename a column
        // The above is going to force you to key columns by their fq method
        // names instead of by their SQL names within the TableContext.Builder.

        return ret;
    }

    @ParameterizedTest
    @MethodSource("initialDiffSource")
    @DisplayName("Empty base should contain tableBuilder of diffs only")
    public void diffWithEmptyBase(String desc, TableContext input, Map<String, Set<SchemaDiff>> expected) {
        Map<String, Set<SchemaDiff>> actual = new SchemaDiffGenerator().calculateDiff(input);
        assertMapEquals(desc, expected, actual);
    }

    @ParameterizedTest
    @MethodSource("subsequentDiffSource")
    @DisplayName("Nonempty base should contain only differences")
    public void diffWithNonemptyBase(String desc, TableContext base, TableContext target, Map<String, Set<SchemaDiff>> expected) {
        Map<String, Set<SchemaDiff>> actual = new SchemaDiffGenerator(base).calculateDiff(target);
        assertMapEquals(desc, expected, actual);
    }

}
