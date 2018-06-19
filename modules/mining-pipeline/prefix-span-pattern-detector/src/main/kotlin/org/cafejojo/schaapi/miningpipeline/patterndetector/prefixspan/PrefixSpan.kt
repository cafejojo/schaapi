package org.cafejojo.schaapi.miningpipeline.patterndetector.prefixspan

import org.cafejojo.schaapi.miningpipeline.Pattern
import org.cafejojo.schaapi.models.CustomEqualsHashSet
import org.cafejojo.schaapi.models.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.NodeSequenceUtil

/**
 * Finds all the frequent patterns of [Node]s in the given collection of sequences using the PrefixSpan algorithm by
 * Pei et al. (2004).
 *
 * @property sequences all sequences in which patterns should be detected. Each path is a list of [Node]s.
 * @property minimumSupport the minimum amount of times a node must appear in [sequences] for it to be considered a
 * frequent node. This node will then be used by the Prefix Space algorithm to find frequent sequences of [Node]s.
 * @property nodeComparator the nodeComparator used to determine whether two [Node]s are equal
 */
internal class PrefixSpan<N : Node>(
    private val sequences: Collection<List<N>>,
    private val minimumSupport: Int,
    private val nodeComparator: GeneralizedNodeComparator<N>
) {
    private val nodeSequenceUtil = NodeSequenceUtil<N>()

    private val frequentPatterns = mutableListOf<Pattern<N>>()
    private val frequentItems = CustomEqualsHashSet<N>(Node.Companion::equiv, Node::equivHashCode)

    /**
     * Finds frequent (sub)sequences of [Node]s using the PrefixSpan algorithm by Pei et al. (2004). The algorithm uses
     * the 'divide and conquer' principle, and is partially inspired by the FP-tree structure used to mine sets of
     * unordered items.
     *
     * First, a set of frequent items, or [Node]s, is generated. For this to function, [Node]s which are considered
     * equal, which may depend on the desired level of abstraction, should have the same hash value.
     *
     * During each iteration, a new prefix is generated based on the current prefix and nodes observed to be frequent in
     * the set of paths. If this prefix is observed in the set of paths it is added to the set of frequent sequences.
     * After this, all suffixes of paths which start with this prefix are then mined recursively, with the suffix of
     * a path being everything that follows said prefix. This improves upon the a priori method by foregoing the
     * need to generate candidate sequences during each iteration. A pseudocode representation is given below.
     *
     * ```
     * Procedure PrefixSpace(all_paths, minimum_support)
     *   frequent_items = all nodes in all_paths with occurrence >= minimum_support
     *   return PrefixSpace([], frequent_items, all_paths, [])
     * EndProcedure
     *
     * SubProcedure PrefixSpace(prefix, frequent_items, projected_paths, frequent_sequences)
     *   for all item in frequent_items
     *     if (prefix + item) in a path in projected_paths or
     *        (prefix.last + item) in a path in projected_paths
     *
     *       new_prefix <- prefix + item
     *       frequent_sequences <- frequent_sequences U new_prefix
     *       new_projected_paths <- empty_set
     *
     *       for all sequence in projected_paths
     *         if item starts with prefix
     *           new_projected_paths <- new_projected_paths U (item - prefix)
     *         end if
     *       end for
     *
     *       PrefixSpace(new_prefix, frequent_items, new_projected_paths, frequent_sequences)
     *     end if
     *   end for
     *
     *   return frequent_sequences
     * SubEndProcedure
     * ```
     *
     * @return the list of sequences, each a list of nodes, that are common within [sequences]
     */
    internal fun findFrequentPatterns(): List<Pattern<N>> {
        frequentItems.addAll(nodeSequenceUtil.findFrequentNodesInSequences(sequences, minimumSupport).keys)
        runAlgorithm()

        return frequentPatterns
    }

    /**
     * Creates a mapping from the found frequent [Pattern]s to all sequences which contain said pattern.
     *
     * If [findFrequentPatterns] has not been run before, the resulting map will not contain any keys.
     *
     * @return a mapping from the frequent patterns to sequences which contain said sequence
     */
    internal fun mapFrequentPatternsToSequences(): Map<Pattern<N>, List<List<N>>> =
        frequentPatterns
            .map { sequence ->
                sequence to sequences.filter {
                    nodeSequenceUtil.sequenceContainsSubSequence(it,
                        sequence,
                        nodeComparator)
                }
            }
            .toMap()

    private fun runAlgorithm(prefix: List<N> = emptyList(), projectedSequences: Collection<List<N>> = sequences) {
        frequentItems.forEach { frequentItem ->
            if (projectedSequences.any { sequenceContainsPrefix(it, prefix, frequentItem) }) {
                val newPrefix = prefix.toMutableList().apply { add(frequentItem) }
                frequentPatterns += newPrefix.toList()

                runAlgorithm(newPrefix, extractSuffixes(prefix, sequences))
            }
        }
    }

    private fun sequenceContainsPrefix(sequence: List<N>, prefix: List<N>, frequentItem: N) =
        nodeSequenceUtil.sequenceContainsSubSequence(sequence,
            prefix.toMutableList().apply { add(frequentItem) },
            nodeComparator) ||
            prefix.isNotEmpty() &&
            nodeSequenceUtil.sequenceContainsSubSequence(sequence, listOf(prefix.last(), frequentItem), nodeComparator)

    internal fun extractSuffixes(prefix: List<N>, sequences: Collection<List<N>>): List<List<N>> =
        sequences.filter { it.size >= prefix.size && it.subList(0, prefix.size) == prefix }
            .map { it.subList(prefix.size, it.size) }
}
