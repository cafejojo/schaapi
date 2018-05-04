package org.cafejojo.schaapi.patterndetector

import org.cafejojo.schaapi.usagegraphgenerator.Node

/**
 * Finds all the frequent sequences of [Node]s in the given collection of paths.
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
                for (sequencePos in 0 until sequence.size) {
                    val endOfPathOrNodeUnequal =
                        pathPos + sequencePos > path.size - 1 || path[pathPos + sequencePos] != sequence[sequencePos]

                    if (endOfPathOrNodeUnequal) break
                    if (sequencePos == sequence.size - 1) return true
                }
            }

            return false
        }
    }

    /**
     * Finds frequent (sub)sequences of [Node]s using the PrefixSpan algorithm by Pei et al. (2004). The algorithm uses
     * the 'divide and conquer' principle, and is partially inspired by the FP-tree structure used to mine sets of
     * unordered items.
     *
     * During each iteration, a new prefix is generated based on the current prefix and nodes observed to be frequent in
     * the set of paths. If this prefix is observed in the set of paths it is added to the set of frequent sequences.
     * After this, all suffixes of sequences which start with this prefix are then mined recursively, with the suffix of
     * a sequence being everything that follows said prefix. This improves upon the a priori method by foregoing the
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
     * @param initialMinimumCount the minimum amount of times a node must appear in the collection of sequences for it
     * to be considered a frequent item. The found set of frequent items are used during the mining process.
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
                    prefix.isNotEmpty() && pathContainsSequence(path, listOf(prefix.last(), frequentItem))
            }

            if (aPathContainsPrefixPlusFrequentItem) {
                val newPrefix = prefix + frequentItem
                frequentSequences += newPrefix

                val newProjectedSequences: List<List<Node>> = extractSuffixes(prefix, projectedPaths)
                prefixSpace(newPrefix, frequentItems, newProjectedSequences, frequentSequences)
            }
        }

        return frequentSequences
    }

    private fun extractSuffixes(prefix: List<Node>, sequences: Collection<List<Node>>): List<List<Node>> {
        val suffixes: MutableList<List<Node>> = mutableListOf()

        sequences.forEach { sequence ->
            val extractedPrefix = sequence.subList(0, prefix.size)
            if (extractedPrefix == prefix) {
                val extractedSuffix = sequence.subList(prefix.size, sequence.size)
                suffixes += extractedSuffix
            }
        }

        return suffixes
    }

    private fun getFrequentItems(minimumCount: Int): Set<Node> {
        val values: MutableMap<Node, Int> = HashMap()
        allPaths.forEach { path ->
            path.forEach { node -> values[node] = values[node]?.inc() ?: 1 }
        }

        return values.filter { (_, amount) -> amount >= minimumCount }.keys
    }
}
