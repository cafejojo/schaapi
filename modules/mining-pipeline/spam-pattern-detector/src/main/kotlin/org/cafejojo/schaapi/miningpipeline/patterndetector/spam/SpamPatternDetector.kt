package org.cafejojo.schaapi.miningpipeline.patterndetector.spam

import org.cafejojo.schaapi.models.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.PathEnumerator
import org.cafejojo.schaapi.miningpipeline.Pattern
import org.cafejojo.schaapi.miningpipeline.PatternDetector

/**
 * A pattern detector implementing the SPAM algorithm.
 */
class SpamPatternDetector<N : Node>(
    private val minimumCount: Int,
    private val enumerator: (N) -> PathEnumerator<N>,
    private val comparator: GeneralizedNodeComparator<N>
) : PatternDetector<N> {
    override fun findPatterns(graphs: List<N>): List<Pattern<N>> {
        val sequences = graphs.flatMap { enumerator(it).enumerate() }
        return Spam(sequences, minimumCount, comparator).findFrequentPatterns()
    }
}
