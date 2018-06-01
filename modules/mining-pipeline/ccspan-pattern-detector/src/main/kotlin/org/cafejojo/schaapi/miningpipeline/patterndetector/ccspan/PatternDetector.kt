package org.cafejojo.schaapi.miningpipeline.patterndetector.ccspan

import org.cafejojo.schaapi.miningpipeline.Pattern
import org.cafejojo.schaapi.miningpipeline.PatternDetector
import org.cafejojo.schaapi.models.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.PathEnumerator

/**
 * Finds closed sequential patterns using the CCSpan algorithm by Zhang et. al.
 */
class PatternDetector<N : Node>(
    private val minimumCount: Int,
    private val comparator: GeneralizedNodeComparator<N>
) : PatternDetector<N> {
    override fun findPatterns(graphs: List<N>): List<Pattern<N>> {
        val sequences = graphs.flatMap { PathEnumerator(it).enumerate() }
        return CCSpan(sequences, minimumCount, comparator).findFrequentPatterns()
    }
}
