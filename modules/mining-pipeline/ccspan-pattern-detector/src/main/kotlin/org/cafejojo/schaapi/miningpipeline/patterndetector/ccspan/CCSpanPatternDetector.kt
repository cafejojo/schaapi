package org.cafejojo.schaapi.miningpipeline.patterndetector.ccspan

import mu.KLogging
import org.cafejojo.schaapi.miningpipeline.Pattern
import org.cafejojo.schaapi.miningpipeline.PatternDetector
import org.cafejojo.schaapi.models.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.Node

/**
 * Finds closed sequential patterns using the CCSpan algorithm by Zhang et. al.
 */
class CCSpanPatternDetector<N : Node>(
    private val minimumCount: Int,
    private val comparator: GeneralizedNodeComparator<N>
) : PatternDetector<N> {
    companion object : KLogging()

    override fun findPatterns(sequences: List<List<N>>): List<Pattern<N>> =
        CCSpan(sequences, minimumCount, comparator).findFrequentPatterns()
}
