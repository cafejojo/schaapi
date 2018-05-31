package org.cafejojo.schaapi.pipeline.patterndetector.spam

import org.cafejojo.schaapi.models.CustomEqualsHashSet
import org.cafejojo.schaapi.models.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.PathUtil
import org.cafejojo.schaapi.pipeline.Pattern

/**
 * Finds frequent sequences of [Node]s in the given collection of paths, using the SPAM algorithm by Ayres et al.
 *
 * @property sequences all paths in which patterns should be detected. Each path is a list of [Node]s.
 * @property minimumSupport the minimum amount of times a node must appear in [sequences] for it to be considered a
 * frequent node.
 * @property nodeComparator the nodeComparator used to determine whether two [Node]s are equal
 */
internal class SPAM<N : Node>(
    private val sequences: Collection<List<N>>,
    private val minimumSupport: Int,
    private val nodeComparator: GeneralizedNodeComparator<N>
) {
    private val pathUtil = PathUtil<N>()
    private val frequentSequences = mutableListOf<List<N>>()
    private val frequentItems = CustomEqualsHashSet<N>(Node.Companion::equiv, Node::equivHashCode)

    /**
     * Finds frequent sequences in [sequences], using the SPAM algorithm by Ayres et al. (2002).
     *
     * @return frequent sequences in [sequences]
     */
    internal fun findFrequentSequences(): List<Pattern<N>> {
        frequentItems.addAll(pathUtil.findFrequentNodesInPaths(sequences, minimumSupport))
        frequentItems.forEach { runAlgorithm(listOf(it), frequentItems) }

        return frequentSequences
    }

    /**
     * Creates a mapping from the found frequent [Pattern]s to [sequences] which contain said sequence.
     *
     * If [findFrequentSequences] has not been run before, the resulting map will not contain any keys.
     *
     * @return a mapping from the frequent patterns to sequences which contain said sequence
     */
    internal fun mapFrequentPatternsToSequences(): Map<Pattern<N>, List<List<N>>> =
        frequentSequences.map { sequence ->
            Pair(sequence, sequences.filter { pathUtil.pathContainsSequence(it, sequence, nodeComparator) })
        }.toMap()

    @Suppress("UnsafeCast") // pattern: List<N> + extension: N is always a List<N>
    private fun runAlgorithm(pattern: List<N>, extensions: Set<N>) {
        frequentSequences.add(pattern)

        val frequentExtensions = extensions.mapNotNull { extension ->
            val extendedPattern: List<N> = (pattern + extension) as List<N>
            val support = sequences.count { path ->
                pathUtil.pathContainsSequence(path, extendedPattern, nodeComparator)
            }

            if (support >= minimumSupport) extension
            else null
        }.toSet()

        frequentExtensions.forEach { runAlgorithm((pattern + it) as List<N>, frequentExtensions) }
    }
}
