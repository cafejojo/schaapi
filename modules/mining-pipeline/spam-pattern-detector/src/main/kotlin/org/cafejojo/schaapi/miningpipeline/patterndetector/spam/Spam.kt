package org.cafejojo.schaapi.miningpipeline.patterndetector.spam

import org.cafejojo.schaapi.miningpipeline.Pattern
import org.cafejojo.schaapi.models.CustomEqualsHashSet
import org.cafejojo.schaapi.models.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.NodeSequenceUtil

/**
 * Finds frequent sequences of [Node]s in the given collection of sequences, using the SPAM algorithm by Ayres et al.
 *
 * @property sequences all sequences in which patterns should be detected. Each sequence is a list of [Node]s.
 * @property minimumSupport the minimum amount of times a node must appear in [sequences] for it to be considered a
 * frequent node.
 * @property nodeComparator the nodeComparator used to determine whether two [Node]s are equal
 */
internal class Spam<N : Node>(
    private val sequences: Collection<List<N>>,
    private val minimumSupport: Int,
    private val nodeComparator: GeneralizedNodeComparator<N>
) {
    private val nodeSequenceUtil = NodeSequenceUtil<N>()
    private val frequentPatterns = mutableListOf<Pattern<N>>()
    private val frequentItems = CustomEqualsHashSet<N>(Node.Companion::equiv, Node::equivHashCode)

    /**
     * Finds frequent sequences in [sequences], using the SPAM algorithm by Ayres et al. (2002).
     *
     * @return frequent sequences in [sequences]
     */
    internal fun findFrequentPatterns(): List<Pattern<N>> {
        frequentItems.addAll(nodeSequenceUtil.findFrequentNodesInSequences(sequences, minimumSupport).keys)
        frequentItems.forEach { runAlgorithm(listOf(it), frequentItems) }

        return frequentPatterns
    }

    /**
     * Creates a mapping from the found frequent [Pattern]s to [sequences] which contain said sequence.
     *
     * If [findFrequentPatterns] has not been run before, the resulting map will not contain any keys.
     *
     * @return a mapping from the frequent patterns to sequences which contain said sequence
     */
    internal fun mapFrequentPatternsToSequences(): Map<Pattern<N>, List<List<N>>> =
        frequentPatterns.map { sequence ->
            sequence to sequences.filter { nodeSequenceUtil.sequenceContainsSubSequence(it, sequence, nodeComparator) }
        }.toMap()

    private fun runAlgorithm(pattern: List<N>, extensions: Set<N>) {
        frequentPatterns.add(pattern)

        val frequentExtensions = extensions.mapNotNull { extension ->
            val extendedPattern = pattern.toMutableList().apply { add(extension) }
            val support = sequences.count { sequence ->
                nodeSequenceUtil.sequenceContainsSubSequence(sequence, extendedPattern, nodeComparator)
            }

            if (support >= minimumSupport) extension
            else null
        }.toSet()

        frequentExtensions.forEach { runAlgorithm(pattern.toMutableList().apply { add(it) }, frequentExtensions) }
    }
}
