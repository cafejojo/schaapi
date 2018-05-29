package org.cafejojo.schaapi.pipeline.patterndetector.spam

import org.cafejojo.schaapi.models.CustomEqualsHashSet
import org.cafejojo.schaapi.models.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.PathUtil
import org.cafejojo.schaapi.pipeline.Pattern

/**
 * Finds frequent sequences of [Node]s in the given collection of paths, using the SPAM algorithm.
 *
 * @property allPaths all paths in which patterns should be detected. Each path is a list of [Node]s.
 * @property minimumCount the minimum amount of times a node must appear in [allPaths] for it to be considered a
 * frequent node.
 * @property comparator the comparator used to determine whether two [Node]s are equal
 */
class FrequentSequenceFinder<N : Node>(
    private val allPaths: Collection<List<N>>,
    private val minimumCount: Int,
    private val comparator: GeneralizedNodeComparator<N>
) {
    private val pathUtil = PathUtil<N>()
    private val frequentSequences = mutableListOf<List<N>>()
    private val frequentItems = CustomEqualsHashSet<N>(Node.Companion::equiv, Node::equivHashCode)

    /**
     * Finds frequent sequences in [allPaths], using the SPAM algorithm by Ayres et al. (2002).
     *
     * @return frequent sequences in [allPaths]
     */
    fun findFrequentSequences(): List<Pattern<N>> {
        frequentItems.addAll(pathUtil.findFrequentNodesInPaths(allPaths, minimumCount))
        frequentItems.forEach { runAlgorithm(listOf(it), frequentItems) }

        return frequentSequences
    }

    /**
     * Creates a mapping from the found frequent patterns to [allPaths] which contain said sequence.
     *
     * If [findFrequentSequences] has not been run before, the resulting map will not contain any keys.
     *
     * @return a mapping from the frequent patterns to sequences which contain said sequence
     */
    fun mapFrequentSequencesToPaths(): Map<List<N>, List<List<N>>> =
        frequentSequences.map { sequence ->
            Pair(sequence, allPaths.filter { pathUtil.pathContainsSequence(it, sequence, comparator) })
        }.toMap()

    private fun runAlgorithm(pattern: List<N>, extensions: Set<N>) {
        frequentSequences.add(pattern)

        val frequentExtensions = extensions.mapNotNull { extension ->
            val extendedPattern = pattern.toMutableList().apply { add(extension) }
            val support = allPaths.count { path -> pathUtil.pathContainsSequence(path, extendedPattern, comparator) }

            if (support >= minimumCount) extension
            else null
        }.toSet()

        frequentExtensions.forEach { runAlgorithm(pattern.toMutableList().apply { add(it) }, frequentExtensions) }
    }
}
