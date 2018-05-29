package org.cafejojo.schaapi.pipeline.testrunner.junit.test;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SimpleTest {
    @Test
    public void passingTest1() {
        assertTrue(true);
    }

    @Test
    @Ignore
    public void ignoredTest() {
        assertTrue(true);
    }

    @Test
    public void passingTest2() {
        assertFalse(false);
    }

    @Test
    public void failingTest() {
        fail();
    }
}
