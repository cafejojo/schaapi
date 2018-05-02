package org.cafejojo.schaapi.patterndetector

import org.cafejojo.schaapi.usagegraphgenerator.Node

/**
 * Pattern detector class, which aims to find all the frequent sequences of [Node]s in the given collection of paths.
 *
 * @param allPaths all paths in which patterns should be detected. Each path is a list of [Node]s.
 */
class PatternDetector(private val allPaths: Collection<List<Node>>) {

    /**
     * Find all the frequent sequences of [Node]s in the given collection of paths set using the prefix space algorithm
     * by Pei et al.
     *
     * The algorithm operates on the 'divide and conquer' principle. During each iteration, a new prefix is generated.
     * All sequences which start with this prefix are then mined during the next iteration. However, only their suffixes
     * need to be mined as they have a common prefix.
     *
     * Below is a pseudocode representation of the algorithm.
     *
     * ```
     * Procedure PrefixSpace(frequent_items, all_paths)
     *   return PrefixSpace([], frequent_items, all_paths, [])
     * EndProcedure
     *
     * SubProcedure PrefixSpace(prefix, frequent_items, projected_paths, frequent_sequences)
     *   for all item in frequent_items
     *     if (prefix + item) contained in a path in projected_paths or
     *        (prefix.last + item) contained in a path in projected_paths
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
            val newPrefix = prefix + frequentItem
            val lastCharNewPrefix = if (prefix.isEmpty()) listOf(frequentItem) else listOf(prefix.last(), frequentItem)

            if (newPrefix.isEmpty() || projectedPaths.any { pathContainsPattern(it, newPrefix) } ||
                projectedPaths.any { pathContainsPattern(it, lastCharNewPrefix) }
            ) {
                frequentSequences += newPrefix

                val newProjectedDataSet: MutableList<List<Node>> = mutableListOf()
                projectedPaths.forEach { sequence ->
                    val extractedPrefix = sequence.subList(0, newPrefix.size)
                    if (extractedPrefix == newPrefix && sequence.size > newPrefix.size) {
                        newProjectedDataSet += sequence.subList(newPrefix.size + 1, sequence.size)
                    }
                }

                prefixSpace(newPrefix, frequentItems, projectedPaths, frequentSequences)
            }
        }

        return frequentSequences
    }

    private fun pathContainsPattern(path: List<Node>, pattern: List<Node>): Boolean {
       for (pathPos in 0 until path.size) {
           for (patternPos in 0 until pattern.size) {
               if (pathPos + patternPos > path.size - 1 || path[pathPos + patternPos] != pattern[patternPos]) break
               else if (patternPos == pattern.size - 1) return true
           }
        }

       return false
    }

    private fun frequentItems(minimumCount: Int): Set<Node> {
        val values: MutableMap<Node, Int> = HashMap()
        allPaths.forEach { sequence ->
            sequence.forEach { node ->
                if (!values.containsKey(node)) values[node] = 1
                values[node]?.inc()
            }
        }

        val items: MutableSet<Node> = HashSet()
        values.forEach { node, amount -> if (amount >= minimumCount) items += node }

        return items
    }
}
