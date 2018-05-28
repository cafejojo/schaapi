package org.cafejojo.schaapi.pipeline.patterndetector.spam

import org.cafejojo.schaapi.models.CustomEqualsHashSet
import org.cafejojo.schaapi.models.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.PathUtil.findFrequentNodesInPaths
import org.cafejojo.schaapi.models.PathUtil.pathContainsSequence

/**
 * Finds frequent sequences of [Node]s in the given collection of paths, using the SPAM algorithm.
 *
 * @property allPaths all paths in which patterns should be detected. Each path is a list of [Node]s.
 * @property minimumCount the minimum amount of times a node must appear in [allPaths] for it to be considered a
 * frequent node.
 * @property comparator the comparator used to determine whether two [Node]s are equal
 */
class FrequentSequenceFinder(
    private val allPaths: Collection<List<Node>>,
    private val minimumCount: Int,
    private val comparator: GeneralizedNodeComparator
) {
    private val frequentSequences = mutableListOf<List<Node>>()
    private val frequentItems = CustomEqualsHashSet(Node.Companion::equiv, Node::equivHashCode)

    /**
     * Finds frequent sequences in [allPaths], using the SPAM algorithm by Ayres et al. (2002).
     *
     * @return frequent sequences in [allPaths]
     */
    fun findFrequentSequences(): List<List<Node>> {
        frequentItems.addAll(findFrequentNodesInPaths(allPaths, minimumCount))
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
    fun mapFrequentSequencesToPaths(): Map<List<Node>, List<List<Node>>> =
        frequentSequences.map { sequence ->
            Pair(sequence, allPaths.filter { pathContainsSequence(it, sequence, comparator) })
        }.toMap()

    private fun runAlgorithm(pattern: List<Node>, extensions: Set<Node>) {
        frequentSequences.add(pattern)

        val frequentExtensions = extensions.mapNotNull { extension ->
            val extendedPattern = pattern + extension
            val support = allPaths.count { path -> pathContainsSequence(path, extendedPattern, comparator) }

            if (support >= minimumCount) extension
            else null
        }.toSet()

        frequentExtensions.forEach { runAlgorithm(pattern + it, frequentExtensions) }
    }
}
