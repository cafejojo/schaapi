package org.cafejojo.schaapi.common

import java.io.OutputStream

/**
 * Represents the test generator that generates tests based on patterns.
 */
interface TestGenerator {
    /**
     * Generates tests based on patterns.
     *
     * @param patterns a list of patterns
     * @return a test file containing all generated tests
     */
    fun generate(patterns: List<Pattern>): OutputStream
}
