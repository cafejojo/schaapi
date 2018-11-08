package org.cafejojo.schaapi.miningpipeline

import org.cafejojo.schaapi.models.Node

/**
 * Generates test files based on [Pattern]s.
 */
interface TestGenerator<in N : Node> {
    /**
     * Generates a test file based on the given [patterns].
     *
     * @param patterns a list of [Pattern]s
     */
    fun generate(patterns: List<Pattern<N>>)
}
