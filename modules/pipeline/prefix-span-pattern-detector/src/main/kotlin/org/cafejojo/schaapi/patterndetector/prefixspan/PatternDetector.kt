package org.cafejojo.schaapi.patterndetector.prefixspan

import org.cafejojo.schaapi.common.GeneralizedNodeComparator
import org.cafejojo.schaapi.common.Node
import org.cafejojo.schaapi.common.Pattern
import org.cafejojo.schaapi.common.PatternDetector


/**
 * Represents a pattern detector.
 */
class PatternDetector(
    private val minimumCount: Int,
    private val comparator: GeneralizedNodeComparator
) : PatternDetector {
    override fun findPatterns(graphs: List<Node>): List<Pattern> {
        val userPaths = graphs.flatMap { it.flatMap { PathEnumerator(it).enumerate() } }

        return FrequentSequenceFinder(userPaths, minimumCount, comparator).findFrequentSequences()
    }
}
