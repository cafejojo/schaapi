package org.cafejojo.schaapi.patterndetector

import org.cafejojo.schaapi.common.Node
import org.cafejojo.schaapi.usagegraphgenerator.compare.GeneralizedNodeComparator

/**
 * Finds all the frequent sequences of [Node]s in the given collection of paths.
 *
 * @property allPaths all paths in which patterns should be detected. Each path is a list of [Node]s.
 * @property minimumCount the minimum amount of times a node must appear in [allPaths] for it to be considered a
 * frequent node. This node will then be used by the Prefix Space algorithm to find frequent sequences of [Node]s.
 *
 */
class PatternDetector(
    private val allPaths: Collection<List<Node>>,
    private val minimumCount: Int,
    private val comparator: GeneralizedNodeComparator
) {
    companion object {
        private fun extractSuffixes(prefix: List<Node>, paths: Collection<List<Node>>): List<List<Node>> {
            val suffixes: MutableList<List<Node>> = mutableListOf()

            paths.forEach { path ->
                val extractedPrefix = path.subList(0, prefix.size)
                if (extractedPrefix == prefix) {
                    suffixes += path.subList(prefix.size, path.size)
                }
            }

            return suffixes
        }
    }

    private val frequentSequences = mutableListOf<List<Node>>()
    private val frequentItems = mutableSetOf<Node>()

    /**
     * Finds frequent (sub)sequences of [Node]s using the PrefixSpan algorithm by Pei et al. (2004). The algorithm uses
     * the 'divide and conquer' principle, and is partially inspired by the FP-tree structure used to mine sets of
     * unordered items.
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
     * @return the list of sequences, each a list of nodes, that are common within [allPaths].
     */
    fun findFrequentSequences(): List<List<Node>> {
        generateFrequentItems(minimumCount)
        runPrefixSpaceAlgorithm()

        return frequentSequences
    }

    /**
     * Creates a mapping from the found frequent patterns to [allPaths] which contain said sequence.
     *
     * If [findFrequentSequences] has not been run before, the resulting map will not contain any keys.
     *
     * @return a mapping from the frequent patterns to sequences which contain said sequence.
     */
    fun mapFrequentSequencesToPaths(): Map<List<Node>, List<List<Node>>> =
        frequentSequences.map { sequence ->
            Pair(sequence, allPaths.filter { pathContainsSequence(it, sequence) })
        }.toMap()

    private fun runPrefixSpaceAlgorithm(
        prefix: List<Node> = emptyList(),
        projectedPaths: Collection<List<Node>> = allPaths
    ) {
        frequentItems.forEach { frequentItem ->
            val aPathContainsPrefixPlusFrequentItem = projectedPaths.any { path ->
                pathContainsSequence(path, prefix + frequentItem) ||
                    prefix.isNotEmpty() && pathContainsSequence(path, listOf(prefix.last(), frequentItem))
            }

            if (aPathContainsPrefixPlusFrequentItem) {
                val newPrefix = prefix + frequentItem
                frequentSequences += newPrefix

                val newProjectedSequences: List<List<Node>> = extractSuffixes(prefix, projectedPaths)
                runPrefixSpaceAlgorithm(newPrefix, newProjectedSequences)
            }
        }
    }

    /**
     * Checks whether a given sequence can be found within a given path.
     *
     * @param path the path which may contain the given sequence.
     * @param sequence the sequence which may be contained in path.
     * @return true if path contains the given sequence.
     */
    internal fun pathContainsSequence(path: List<Node>, sequence: List<Node>): Boolean {
        for (pathPos in 0 until path.size) {
            for (sequencePos in 0 until sequence.size) {
                val endOfPathOrNodeUnequal =
                    pathPos + sequencePos > path.size - 1 ||
                        !comparator.satisfies(path[pathPos + sequencePos], sequence[sequencePos])

                if (endOfPathOrNodeUnequal) break
                if (sequencePos == sequence.size - 1) return true
            }
        }

        return false
    }

    private fun generateFrequentItems(minimumCount: Int) {
        val nodeCounts: MutableMap<Node, Int> = HashMap()
        allPaths.forEach { path ->
            path.forEach { node -> nodeCounts[node] = nodeCounts[node]?.inc() ?: 1 }
        }

        frequentItems.addAll(nodeCounts.filter { (_, amount) -> amount >= minimumCount }.keys)
    }
}
