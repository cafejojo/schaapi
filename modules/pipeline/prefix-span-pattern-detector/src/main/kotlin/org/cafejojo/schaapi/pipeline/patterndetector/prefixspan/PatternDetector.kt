package org.cafejojo.schaapi.pipeline.patterndetector.prefixspan

import org.cafejojo.schaapi.models.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.pipeline.Pattern
import org.cafejojo.schaapi.pipeline.PatternDetector

/**
 * Represents a pattern detector.
 */
class PatternDetector(
    private val minimumCount: Int,
    private val comparator: GeneralizedNodeComparator
) : PatternDetector {
    override fun findPatterns(graphs: List<Node>): List<Pattern> {
        val userPaths = graphs.flatMap { it.flatMap { PathEnumerator(it).enumerate() } }

        return FrequentSequenceFinder(userPaths,
            minimumCount,
            comparator).findFrequentSequences()
    }
}
