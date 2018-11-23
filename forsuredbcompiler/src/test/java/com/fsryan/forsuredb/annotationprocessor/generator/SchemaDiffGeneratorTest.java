package com.fsryan.forsuredb.annotationprocessor.generator;

import com.fsryan.forsuredb.annotationprocessor.TableContext;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.migration.SchemaDiff;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;

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

        TableInfo.BuilderCompat t1Builder = TableInfo.builder().tableName("t1").qualifiedClassName("t1.qclass.name");
        builder.addTable("t1", "t1.qclass.name", t1Builder);
        ret.add(arguments(
                "A single table should be able to be added",
                builder.build(),
                mapOf("t1", setOf(SchemaDiff.forTableCreated("t1")))
        ));

        TableInfo.BuilderCompat t2Builder = TableInfo.builder().tableName("t2").qualifiedClassName("t2.qclass.name");
        builder.addTable("t2", "t2.qclass.name", t2Builder);
        ret.add(arguments(
                "Multiple tables should be able to be created at the same time",
                builder.build(),
                mapOf("t1", setOf(SchemaDiff.forTableCreated("t1")), "t2", setOf(SchemaDiff.forTableCreated("t2")))
        ));
        return ret;
    }

    @ParameterizedTest
    @MethodSource("initialDiffSource")
    @DisplayName("Empty base should contain all expected diffs")
    public void diffWithEmptyBase(String desc, TableContext input, Map<String, Set<SchemaDiff>> expected) {
        Map<String, Set<SchemaDiff>> actual = new SchemaDiffGenerator().calculateDiff(input);
        assertMapEquals(desc, expected, actual);
    }

}
