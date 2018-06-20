package org.cafejojo.schaapi.miningpipeline.patterndetector.prefixspan

import mu.KLogging
import org.cafejojo.schaapi.miningpipeline.Pattern
import org.cafejojo.schaapi.miningpipeline.PatternDetector
import org.cafejojo.schaapi.models.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.PathEnumerator

/**
 * A pattern detector implementing the PrefixSpan algorithm.
 */
class PrefixSpanPatternDetector<N : Node>(
    private val minimumCount: Int,
    private val maximumSequenceLength: Int,
    private val comparator: GeneralizedNodeComparator<N>
) : PatternDetector<N> {
    private companion object : KLogging()

    override fun findPatterns(graphs: List<N>): List<Pattern<N>> {
        logger.info { "Detecting patterns in ${graphs.size} graphs." }

        val sequences = graphs.flatMap { PathEnumerator(it, maximumSequenceLength).enumerate() }
        logger.info { "Found ${sequences.size} sequences in ${graphs.size} graphs." }

        return PrefixSpan(sequences, minimumCount, comparator).findFrequentPatterns()
    }
}
