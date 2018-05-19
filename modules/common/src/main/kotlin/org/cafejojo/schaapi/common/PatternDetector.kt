package org.cafejojo.schaapi.common

/**
 * Represents a pattern detector.
 */
interface PatternDetector {
    /**
     * Detects common patterns within the given list of graphs.
     *
     * @param graphs a list of graphs
     * @return a list of detected patterns
     */
    fun findPatterns(graphs: List<Node>): List<Pattern>
}
