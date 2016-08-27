package com.fsryan.forsuredb.annotationprocessor.generator;

import com.fsryan.forsuredb.annotationprocessor.TableContext;
import com.fsryan.forsuredb.api.migration.Migration;
import com.fsryan.forsuredb.api.migration.MigrationSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

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
        if (expectedMigrationSet.getOrderedMigrations().size() != actualMigrationSet.getOrderedMigrations().size()) {
            fail(formatErrorMessage("expected count: " + expectedMigrationSet.getOrderedMigrations().size()
                    + "; actual count: " + actualMigrationSet.getOrderedMigrations().size()));  // <-- formatting the error result is a little time-consuming, so only do it on failures
        }
    }

    @Test
    public void shouldMatchMigrationsInOrderAndContent() {
        final List<Migration> expected = expectedMigrationSet.getOrderedMigrations();
        final List<Migration> actual = actualMigrationSet.getOrderedMigrations();
        for (int i = 0; i < expected.size(); i++) {
            assertEquals("migration index " + i, expected.get(i), actual.get(i));
        }
    }

    @Test
    public void shouldContainTargetContext() {
        assertEquals(processingContext.tableMap(), actualMigrationSet.getTargetSchema());
    }

    private String formatErrorMessage(String firstLine) {
        return firstLine
                + "\nexpected:\n" + gson.toJson(expectedMigrationSet.getOrderedMigrations())
                + "\nactual:\n" + gson.toJson(actualMigrationSet.getOrderedMigrations());
    }
}
