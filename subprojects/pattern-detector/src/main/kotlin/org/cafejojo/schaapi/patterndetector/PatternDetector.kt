package org.cafejojo.schaapi.patterndetector

import org.cafejojo.schaapi.usagegraphgenerator.Node

/**
 * Aims to find all the frequent sequences of [Node]s in the given collection of paths.
 *
 * @property allPaths all paths in which patterns should be detected. Each path is a list of [Node]s.
 */
class PatternDetector(private val allPaths: Collection<List<Node>>) {
    companion object {
        /**
         * Checks whether a given sequence can be found within a given path.
         *
         * @param path the path which may contain the given sequence.
         * @param sequence the sequence which may be contained in path.
         * @return true if path contains the given sequence.
         */
        internal fun pathContainsSequence(path: List<Node>, sequence: List<Node>): Boolean {
            for (pathPos in 0 until path.size) {
                for (patternPos in 0 until sequence.size) {
                    if (pathPos + patternPos > path.size - 1 ||
                        path[pathPos + patternPos] != sequence[patternPos]
                    ) break

                    if (patternPos == sequence.size - 1) return true
                }
            }

            return false
        }
    }

    /**
     * Finds all the frequent sequences of [Node]s in the given collection of paths set using the PrefixSpace algorithm
     * by Pei et al. (2004).
     *
     * The algorithm operates on the 'divide and conquer' principle. During each iteration, a new prefix is generated
     * based on the observed sequences and items in the ```frequent_item``` set. All suffixes of sequences which start
     * with this prefix are then mined during the next call, with the suffix of a path being everything that follows the
     * prefix.
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
     * @param initialMinimumCount the minimum amount of times a node must appear in ```all_paths``` for it to be
     * considered a frequent item. These nodes are passed as the ```frequent_items``` argument in the above algorithm.
     * @return the list of sequences, each a list of nodes, that are common within the given paths.
     */
    fun findFrequentPatterns(initialMinimumCount: Int) =
        prefixSpace(frequentItems = getFrequentItems(initialMinimumCount))

    private fun prefixSpace(
        prefix: List<Node> = emptyList(),
        frequentItems: Set<Node>,
        projectedPaths: Collection<List<Node>> = allPaths,
        frequentSequences: MutableList<List<Node>> = mutableListOf()
    ): List<List<Node>> {
        frequentItems.forEach { frequentItem ->
            val aPathContainsPrefixPlusFrequentItem = projectedPaths.any { path ->
                pathContainsSequence(path, prefix + frequentItem) ||
                    (prefix.isNotEmpty() && pathContainsSequence(path, listOf(prefix.last(), frequentItem)))
            }

            if (aPathContainsPrefixPlusFrequentItem) {
                val newPrefix = prefix + frequentItem
                frequentSequences += newPrefix

                val newProjectedPaths: List<List<Node>> = createProjectedPaths(prefix, projectedPaths)
                prefixSpace(newPrefix, frequentItems, newProjectedPaths, frequentSequences)
            }
        }

        return frequentSequences
    }

    /**
     * Creates a projection of the projected Paths.
     *
     * A projection is a list of suffixes of all paths which contain the given prefix as a prefix. In this context, the
     * suffix of a path is everything following the prefix of the path.
     *
     * @param prefix the prefix which a path should have.
     * @param projectedPaths the list of paths which should be checked for said prefix.
     * @return suffixes of paths with the given prefix.
     */
    private fun createProjectedPaths(prefix: List<Node>, projectedPaths: Collection<List<Node>>): List<List<Node>> {
        val newProjectedPaths: MutableList<List<Node>> = mutableListOf()

        projectedPaths.forEach { sequence ->
            val extractedPrefix = sequence.subList(0, prefix.size)
            if (extractedPrefix == prefix) {
                val extractedSuffix = sequence.subList(prefix.size, sequence.size)
                newProjectedPaths += extractedSuffix
            }
        }

        return newProjectedPaths
    }

    /**
     * Gives all nodes in the collection of paths which occur the given minimum amount of times. These length-1
     * sequential patterns.
     *
     * @param minimumCount the minimum amount of times a node should occur.
     * @return set of nodes which occur at least the given minimum amount of times.
     */
    private fun getFrequentItems(minimumCount: Int): Set<Node> {
        val values: MutableMap<Node, Int> = HashMap()
        allPaths.forEach { sequence ->
            sequence.forEach { node -> values[node] = values[node]?.inc() ?: 1 }
        }

        val items: MutableSet<Node> = HashSet()
        values.forEach { node, amount -> if (amount >= minimumCount) items += node }

        return items
    }
}
