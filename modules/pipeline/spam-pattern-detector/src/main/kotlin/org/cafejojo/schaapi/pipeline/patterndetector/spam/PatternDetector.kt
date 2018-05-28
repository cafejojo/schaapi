package org.cafejojo.schaapi.pipeline.patterndetector.spam

import org.cafejojo.schaapi.models.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.PathEnumerator
import org.cafejojo.schaapi.pipeline.Pattern
import org.cafejojo.schaapi.pipeline.PatternDetector
import org.cafejojo.schaapi.pipeline.patterndetector.spam.FrequentSequenceFinder

/**
 * Represents a pattern detector.
 */
class PatternDetector(
    private val minimumCount: Int,
    private val comparator: GeneralizedNodeComparator
) : PatternDetector {
    override fun findPatterns(graphs: List<Node>): List<Pattern> {
        val userPaths = graphs.flatMap { PathEnumerator(it).enumerate() }

        return FrequentSequenceFinder(userPaths, minimumCount, comparator).findFrequentSequences()
    }
}
