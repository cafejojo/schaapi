package org.cafejojo.schaapi.pipeline.patterndetector.ccspan

import org.cafejojo.schaapi.models.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.PathUtil
import org.cafejojo.schaapi.pipeline.Pattern

/**
 * Finds closed sequential patterns using the CCSpan algorithm by Zhang et. al.
 *
 * @property sequences the collection of all sequences
 * @property minimumSupport the minimum amount of times a [Node] must appear in [sequences] for it to be considered
 * significant
 * @property nodeComparator the comparator used to determine whether two [Node]s are equal
 */
internal class CCSpan<N : Node>(
    private val sequences: Collection<List<N>>,
    private val minimumSupport: Int,
    private val nodeComparator: GeneralizedNodeComparator<N>
) {
    private val pathUtil = PathUtil<N>()

    private val sequencesOfPreviousLength = mutableSetOf<SequenceTriple<N>>()
    private val sequencesOfCurrentLength = mutableSetOf<SequenceTriple<N>>()

    private fun List<N>.pre() = this.subList(0, this.size - 1)
    private fun List<N>.post() = this.subList(1, this.size)

    /**
     * Generates a list of frequent patterns which are all closed contiguous frequent sequences.
     *
     * @return list of frequent sequential patterns
     */
    internal fun findFrequentPatterns(): List<Pattern<N>> {
        findFrequentSingletonSequences()
        val frequentClosedContiguousSequences = mutableListOf<Pattern<N>>()

        var subSequenceLength = 2
        while (sequencesOfPreviousLength.isNotEmpty()) {
            val checkedSequences = mutableListOf<List<N>>()

            sequences
                .filter { it.size >= subSequenceLength }
                .forEach { findAllContiguousSequencesOfLength(it, subSequenceLength, checkedSequences) }

            identifyNonClosedSequences()
            frequentClosedContiguousSequences.addAll(
                sequencesOfPreviousLength.filter { it.isClosedSequence }.map { it.sequence }
            )

            shiftCurrent()
            subSequenceLength++
        }

        return frequentClosedContiguousSequences
    }

    private fun findFrequentSingletonSequences() {
        val checkedElements = mutableListOf<N>()

        sequences.flatten().forEach { element ->
            if (!checkedElements.contains(element)) {
                val support = calculateSupport(listOf(element))
                if (support >= minimumSupport) sequencesOfPreviousLength += SequenceTriple(listOf(element), support)

                checkedElements += element
            }
        }
    }

    private fun findAllContiguousSequencesOfLength(
        sequence: List<N>,
        subSequenceLength: Int,
        checkedSequences: MutableList<List<N>>
    ) {
        sequence.dropLast(subSequenceLength - 1).indices.forEach {
            val subSequence = sequence.subList(it, it + subSequenceLength)
            checkSupportOfSubSequence(sequence.subList(it, it + subSequenceLength), checkedSequences)

            checkedSequences += subSequence
        }
    }

    private fun checkSupportOfSubSequence(subSequence: List<N>, checkedSequences: MutableList<List<N>>) {
        if (checkedSequences.contains(subSequence)) return

        if (sequencesOfPreviousLength.any { it.sequence == subSequence.pre() }
            && sequencesOfPreviousLength.any { it.sequence == subSequence.post() }) {
            val support = calculateSupport(subSequence)
            if (support >= minimumSupport) sequencesOfCurrentLength += SequenceTriple(subSequence, support)
        }
    }

    private fun identifyNonClosedSequences() {
        sequencesOfCurrentLength.forEach { (sequence, sequenceSupport, _) ->
            sequencesOfPreviousLength
                .filter {
                    it.isClosedSequence
                        && (it.sequence == sequence.pre() || it.sequence == sequence.post())
                        && sequenceSupport >= it.support
                }
                .forEach { it.isClosedSequence = false }
        }
    }

    private fun calculateSupport(sequence: List<N>) =
        sequences.count { pathUtil.pathContainsSequence(it, sequence, nodeComparator) }

    private fun shiftCurrent() {
        sequencesOfPreviousLength.clear()
        sequencesOfPreviousLength.addAll(sequencesOfCurrentLength)
        sequencesOfCurrentLength.clear()
    }
}

private data class SequenceTriple<N>(val sequence: List<N>, val support: Int, var isClosedSequence: Boolean = true)
