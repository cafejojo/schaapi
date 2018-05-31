package org.cafejojo.schaapi.miningpipeline

import org.cafejojo.schaapi.models.Node

/**
 * Detects [Pattern]s in the graphs that are implicitly represented by [Node]s.
 */
interface PatternDetector<N : Node> {
    /**
     * Detects patterns that are common to the given [graphs].
     *
     * @param graphs a list of graphs, where each graph is represented by a [Node]
     * @return the list of detected patterns
     */
    fun findPatterns(graphs: List<N>): List<Pattern<N>>
}
