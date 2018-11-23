package com.fsryan.forsuredb.annotationprocessor.generator;

import com.fsryan.forsuredb.annotationprocessor.TableContext;
import com.fsryan.forsuredb.migration.Migration;
import com.fsryan.forsuredb.migration.MigrationSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.fsryan.forsuredb.test.assertions.AssertCollection.assertListEquals;
import static com.fsryan.forsuredb.test.assertions.AssertCollection.assertMapEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public abstract class BaseDiffGeneratorTest {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    protected MigrationSet actualMigrationSet;

    protected final int sourceDbVersion;
    protected final TableContext migrationContext;
    protected final TableContext processingContext;
    protected final MigrationSet expectedMigrationSet;


    public BaseDiffGeneratorTest(int sourceDbVersion, TableContext migrationContext, TableContext processingContext, MigrationSet expectedMigrationSet) {
        this.sourceDbVersion = sourceDbVersion;
        this.migrationContext = migrationContext;
        this.processingContext = processingContext;
        this.expectedMigrationSet = expectedMigrationSet;
    }

    @Before
    public void setUp() {
        actualMigrationSet = new DiffGenerator(migrationContext, sourceDbVersion).analyzeDiff(processingContext);
    }

    @Test
    public void shouldHaveCorrectNumberOfMigrations() {
        if (expectedMigrationSet.orderedMigrations().size() != actualMigrationSet.orderedMigrations().size()) {
            fail(formatErrorMessage("expected count: " + expectedMigrationSet.orderedMigrations().size()
                    + "; actual count: " + actualMigrationSet.orderedMigrations().size()));  // <-- formatting the error result is a little time-consuming, so only do it on failures
        }
    }

    @Test
    public void shouldMatchMigrationsInOrderAndContent() {
        assertListEquals(expectedMigrationSet.orderedMigrations(), actualMigrationSet.orderedMigrations());
    }

    @Test
    public void shouldContainTargetContext() {
        assertMapEquals(processingContext.tableMap(), actualMigrationSet.targetSchema());
    }

    private String formatErrorMessage(String firstLine) {
        return firstLine
                + "\nexpected:\n" + gson.toJson(expectedMigrationSet.orderedMigrations())
                + "\nactual:\n" + gson.toJson(actualMigrationSet.orderedMigrations());
    }
}
