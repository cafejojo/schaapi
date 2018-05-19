package org.cafejojo.schaapi.patterndetector.prefixspan

import org.cafejojo.schaapi.common.Node
import org.cafejojo.schaapi.common.Pattern
import org.cafejojo.schaapi.common.PatternDetector
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.compare.GeneralizedSootComparator

/**
 * Represents a pattern detector.
 */
class PatternDetector(private val minimumCount: Int) : PatternDetector {
    override fun findPatterns(graphs: List<Node>): List<Pattern> {
        val userPaths = graphs.flatMap { it.flatMap { PathEnumerator(it).enumerate() } }

        return FrequentSequenceFinder(userPaths, minimumCount, GeneralizedSootComparator()).findFrequentSequences()
    }
}
