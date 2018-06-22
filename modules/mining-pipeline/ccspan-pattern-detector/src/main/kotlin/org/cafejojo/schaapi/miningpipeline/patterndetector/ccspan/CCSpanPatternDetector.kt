package org.cafejojo.schaapi.miningpipeline.patterndetector.ccspan

import mu.KLogging
import org.cafejojo.schaapi.miningpipeline.CSVWriter
import org.cafejojo.schaapi.miningpipeline.Pattern
import org.cafejojo.schaapi.miningpipeline.PatternDetector
import org.cafejojo.schaapi.models.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.PathEnumerator

/**
 * Finds closed sequential patterns using the CCSpan algorithm by Zhang et. al.
 */
class CCSpanPatternDetector<N : Node>(
    private val csvWriter: CSVWriter<N>,
    private val minimumCount: Int,
    private val enumerator: (N) -> PathEnumerator<N>,
    private val comparator: GeneralizedNodeComparator<N>
) : PatternDetector<N> {
    companion object : KLogging()

    override fun findPatterns(graphs: List<N>): List<Pattern<N>> {
        val sequences = graphs.flatMap { enumerator(it).enumerate() }
        logger.info { "${sequences.size} sequences extracted from ${graphs.size} graphs." }
        csvWriter.writeSequenceLengths(sequences)

        return CCSpan(sequences, minimumCount, comparator).findFrequentPatterns()
            .also { csvWriter.writePatternLengths(it) }
    }
}
