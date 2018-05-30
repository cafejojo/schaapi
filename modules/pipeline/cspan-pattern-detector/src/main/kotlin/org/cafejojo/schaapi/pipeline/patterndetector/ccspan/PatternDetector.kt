package org.cafejojo.schaapi.pipeline.patterndetector.ccspan

import org.cafejojo.schaapi.models.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.PathEnumerator
import org.cafejojo.schaapi.pipeline.Pattern
import org.cafejojo.schaapi.pipeline.PatternDetector

/**
 * Represents a pattern detector.
 */
class PatternDetector<N : Node>(
    private val minimumCount: Int,
    private val comparator: GeneralizedNodeComparator<N>
) : PatternDetector<N> {
    override fun findPatterns(graphs: List<N>): List<Pattern<N>> {
        val userPaths = graphs.flatMap { PathEnumerator(it).enumerate() }

//        return FrequentSequenceFinder(userPaths, minimumCount, comparator).findFrequentSequences()
        return emptyList()
    }
}
