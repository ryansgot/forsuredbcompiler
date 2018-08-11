package com.fsryan.forsuredb.api;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class FinderGroupTest extends FinderTest.ForFinder {

    @Test
    public void shouldAddStartGroupWhenStartingGroupAnd() {
        finderUnderTest.startGroup();
        assertEquals(Collections.singletonList(Finder.WhereElement.START_GROUP), accessWhereElements());
    }

    // this may change because it allows you to end a group before starting a group.
    // Such a behavior would be useful if adding a condition to a Finder that was
    // created as a Finder for a joined Resolver
    @Test
    public void shouldAddEndGroupWhenAddedCondition() {
        Finder.WhereElement added = Finder.WhereElement.createCondition("column", Finder.OP_EQ, new Object());
        addWhereElement(added);
        finderUnderTest.endGroup();
        assertEquals(Arrays.asList(added, Finder.WhereElement.END_GROUP), accessWhereElements());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowWhenStartingGroupAndLastElementIsCondition() {
        addWhereElement(Finder.WhereElement.createCondition("column", Finder.OP_EQ, new Object()));
        finderUnderTest.startGroup();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowWhenStartingGroupAndLastElementIsGroupEnd() {
        addWhereElement(Finder.WhereElement.END_GROUP);
        finderUnderTest.startGroup();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowWhenEndingGroupAndNoWhereElements() {
        finderUnderTest.endGroup();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowWhenEndingGroupAndLastElementIsGroupStart() {
        addWhereElement(Finder.WhereElement.START_GROUP);
        finderUnderTest.endGroup();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowWhenEndingGroupAndLastElementIsAndConjunction() {
        addWhereElement(Finder.WhereElement.AND);
        finderUnderTest.endGroup();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowWhenEndingGroupAndLastElementIsOrConjunction() {
        addWhereElement(Finder.WhereElement.OR);
        finderUnderTest.endGroup();
    }
}
