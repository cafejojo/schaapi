package org.cafejojo.schaapi.miningpipeline

import org.cafejojo.schaapi.models.Node

/**
 * Detects [Pattern]s in the graphs that are implicitly represented by [Node]s.
 */
interface PatternDetector<N : Node> {
    /**
     * Detects patterns that are common in the given set of [sequences].
     *
     * @param sequences a list of sequences, each a list of [Node]s
     * @return the list of detected patterns
     */
    fun findPatterns(sequences: List<List<N>>): List<Pattern<N>>
}
