package org.cafejojo.schaapi.patterndetector

import org.cafejojo.schaapi.usagegraphgenerator.Node

/**
 * Pattern detector class, which aims to find all the frequent sequences of [Node]s in the given collection of paths.
 *
 * @param allPaths all paths in which patterns should be detected. Each path is a list of [Node]s.
 */
class PatternDetector(private val allPaths: Collection<List<Node>>) {

    /**
     * Find all the frequent sequences of [Node]s in the given collection of paths set using the PrefixSpace algorithm
     * by Pei et al. (2004).
     *
     * The algorithm operates on the 'divide and conquer' principle. During each iteration, a new prefix is generated
     * based on the observed sequences and items in the ```frequent_item``` set. All sequences which start with this
     * prefix are then mined during the next call. However, only their suffixes need to be mined as they have a common
     * prefix.
     *
     * Below is a pseudocode representation of this algorithm.
     *
     * ```
     * Procedure PrefixSpace(frequent_items, all_paths)
     *   return PrefixSpace([], frequent_items, all_paths, [])
     * EndProcedure
     *
     * SubProcedure PrefixSpace(prefix, frequent_items, projected_paths, frequent_sequences)
     *   for all item in frequent_items
     *     if item is first node in path in projected_paths
     *
     *       new_prefix <- prefix + item
     *       frequent_sequences <- frequent_sequences U new_prefix
     *       new_projected_paths <- empty set
     *
     *       for all sequence in projected_paths
     *         if item starts with new_prefix
     *           new_projected_paths <- new_projected_paths U (item - new_prefix)
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
     * @param initialMinimumCount The minimum amount of times a node must appear in allPaths for it to be
     * considered a frequent item. These nodes are passed as the ```frequent_items``` argument in the above algorithm.
     * @return List of sequences, each a list of nodes, that are common within the given paths.
     */
    fun frequentPatterns(initialMinimumCount: Int) = prefixSpace(frequentItems = frequentItems(initialMinimumCount))

    private fun prefixSpace(
        prefix: List<Node> = emptyList(),
        frequentItems: Set<Node>,
        projectedPaths: Collection<List<Node>> = allPaths,
        frequentSequences: MutableList<List<Node>> = mutableListOf()
    ): List<List<Node>> {
        frequentItems.forEach { frequentItem ->
            if (projectedPaths.any { path -> path.first() == frequentItem }) {
                val newPrefix = prefix + frequentItem
                frequentSequences += newPrefix

                val newProjectedPaths: MutableList<List<Node>> = mutableListOf()
                projectedPaths.forEach { sequence ->
                    val extractedPrefix = sequence.subList(0, newPrefix.size)

                    if (extractedPrefix == newPrefix) {
                        val extractedSuffix = sequence.subList(newPrefix.size, sequence.size)
                        newProjectedPaths += extractedSuffix
                    }
                }

                prefixSpace(newPrefix, frequentItems, newProjectedPaths, frequentSequences)
            }
        }

        return frequentSequences
    }

    /**
     * Gives all nodes in the collection of paths which occur the given minimum amount of times. These length-1
     * sequential patterns.
     *
     * @param minimumCount the minimum amount of times a node should occur.
     * @return set of nodes which occur at least the given minimum amount of times.
     */
    private fun frequentItems(minimumCount: Int): Set<Node> {
        val values: MutableMap<Node, Int> = HashMap()
        allPaths.forEach { sequence ->
            sequence.forEach { node -> values[node] = if (values.contains(node)) values[node]!!.inc() else 1 }
        }

        val items: MutableSet<Node> = HashSet()
        values.forEach { node, amount -> if (amount >= minimumCount) items += node }

        return items
    }
}
