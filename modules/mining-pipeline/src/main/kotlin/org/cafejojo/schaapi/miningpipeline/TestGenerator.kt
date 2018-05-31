package org.cafejojo.schaapi.miningpipeline

import org.cafejojo.schaapi.models.Node
import java.io.File

/**
 * Generates test files based on [Pattern]s.
 */
interface TestGenerator<in N : Node> {
    /**
     * Generates a test file based on the given [patterns].
     *
     * @param patterns a list of [Pattern]s
     * @return a test file containing all generated tests
     */
    fun generate(patterns: List<Pattern<N>>): File
}
