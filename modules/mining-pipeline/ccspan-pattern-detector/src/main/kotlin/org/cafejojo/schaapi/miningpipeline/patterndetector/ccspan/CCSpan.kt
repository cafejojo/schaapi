// ktlint-disable op-spacing
package org.cafejojo.schaapi.miningpipeline.patterndetector.ccspan

import org.cafejojo.schaapi.miningpipeline.Pattern
import org.cafejojo.schaapi.models.CustomEqualsHashMap
import org.cafejojo.schaapi.models.CustomEqualsList
import org.cafejojo.schaapi.models.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.NodeSequenceUtil

/**
 * Finds closed sequential patterns using the CCSpan algorithm by Zhang et. al.
 *
 * @property equalsSequences the collection of all sequences
 * @property minimumSupport the minimum amount of times a [Node] must appear in [equalsSequences] for it to be
 * considered significant
 * @property nodeComparator the comparator used to determine whether two [Node]s are equal
 */
internal class CCSpan<N : Node>(
    sequences: Collection<List<N>>,
    private val minimumSupport: Int,
    private val nodeComparator: GeneralizedNodeComparator<N>
) {
    private val equalsSequences = sequences.map { CustomEqualsList(it, Node.Companion::equiv, Node::equivHashCode) }
    private val nodeSequenceUtil = NodeSequenceUtil<N>()

    private val sequencesOfPreviousLength = mutableSetOf<SequenceTriple<N>>()
    private val sequencesOfCurrentLength = mutableSetOf<SequenceTriple<N>>()

    private val sequenceSupportMap = CustomEqualsHashMap<List<N>, Int>(List<N>::equals, List<N>::hashCode)

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
            equalsSequences.filter { it.size >= subSequenceLength }
                .forEach { findAllContiguousSequencesOfLength(it, subSequenceLength) }

            identifyNonClosedSequences()
            frequentClosedContiguousSequences.addAll(
                sequencesOfPreviousLength.filter { it.isClosedSequence }.map { it.sequence }
            )

            shiftCurrent()
            subSequenceLength++
        }

        return frequentClosedContiguousSequences.map { it.toList() }
    }

    private fun findFrequentSingletonSequences() {
        sequencesOfPreviousLength += nodeSequenceUtil.findFrequentNodesInSequences(equalsSequences, minimumSupport)
            .map { (node, support) ->
                SequenceTriple(
                    CustomEqualsList(listOf(node), Node.Companion::equiv, Node::equivHashCode),
                    support
                )
            }
    }

    private fun findAllContiguousSequencesOfLength(sequence: List<N>, subSequenceLength: Int) {
        (0..sequence.size - subSequenceLength)
            .forEach { checkSupportOfSubSequence(sequence.subList(it, it + subSequenceLength)) }
    }

    private fun checkSupportOfSubSequence(subSequence: List<N>) {
        if (sequencesOfPreviousLength.any { it.sequence == subSequence.pre() } &&
            sequencesOfPreviousLength.any { it.sequence == subSequence.post() }) {
            val support = calculateSupport(subSequence)
            if (support >= minimumSupport) sequencesOfCurrentLength += SequenceTriple(subSequence, support)
        }
    }

    private fun identifyNonClosedSequences() {
        sequencesOfCurrentLength.forEach { (sequence, sequenceSupport, _) ->
            sequencesOfPreviousLength.parallelStream()
                .filter {
                    it.isClosedSequence &&
                        (it.sequence == sequence.pre() || it.sequence == sequence.post()) &&
                        sequenceSupport >= it.support
                }
                .forEach { it.isClosedSequence = false }
        }
    }

    private fun calculateSupport(sequence: List<N>) =
        sequenceSupportMap[sequence] ?: equalsSequences
            .filter { it.size >= sequence.size }
            .count { nodeSequenceUtil.sequenceContainsSubSequence(it, sequence, nodeComparator) }
            .also { sequenceSupportMap[sequence] = it }

    private fun shiftCurrent() {
        sequencesOfPreviousLength.clear()
        sequencesOfPreviousLength.addAll(sequencesOfCurrentLength)
        sequencesOfCurrentLength.clear()
    }
}

private data class SequenceTriple<N>(val sequence: List<N>, val support: Int, var isClosedSequence: Boolean = true)
