package org.cafejojo.schaapi.validationpipeline.testrunner.junit.test;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AnotherSimpleTest {
    @Test
    public void passingTest() {
        assertTrue(true);
    }

    @Test
    public void failingTest() {
        fail();
    }
}
