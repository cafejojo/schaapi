package org.cafejojo.schaapi.models

/**
 * Utilities for paths of [Node]s.
 */
class PathUtil<N> {
    /**
     * Checks whether a given sequence can be found within a given path.
     *
     * @param path the path which may contain the given sequence
     * @param sequence the sequence which may be contained in path
     * @return true if path contains the given sequence
     */
    fun pathContainsSequence(path: List<N>, sequence: List<N>, comparator: GeneralizedNodeComparator<Node>) =
        path.indices.any { pathIndex ->
            !sequence.indices.any { sequenceIndex ->
                true
//                pathIndex + sequenceIndex >= path.size ||
//                    !comparator.satisfies(path[pathIndex + sequenceIndex], sequence[sequenceIndex])
            }
        }

    fun pathContainsSequenceCool(path: List<N>, sequence: List<N>) =
        path.indices.any { pathIndex ->
            !sequence.indices.any { sequenceIndex ->
                pathIndex + sequenceIndex >= path.size || path[pathIndex + sequenceIndex] != sequence[sequenceIndex]
            }
        }

    /**
     * Finds all nodes occurring at least [minimumCount] times in the given [paths].
     *
     * @param paths the paths to search in
     * @param minimumCount the minimum number of times a node needs to occur
     * @return the set of nodes occurring at least [minimumCount] times
     */
    fun findFrequentNodesInPaths(paths: Collection<List<N>>, minimumCount: Int): Set<N> {
//        val nodeCounts = CustomEqualsHashMap<N, Int>(Node.Companion::equiv, Node::equivHashCode)
//        paths.forEach { it.forEach { node -> nodeCounts[node] = nodeCounts[node]?.inc() ?: 1 } }
//
//        return nodeCounts.filter { (_, amount) -> amount >= minimumCount }.keys
        return emptySet()
    }

    fun findFrequentNodesInPathsCool(paths: Collection<List<N>>, minimumCount: Int): Set<N> {
        val nodeCounts = hashMapOf<N, Int>()
        paths.forEach { it.forEach { node -> nodeCounts[node] = nodeCounts[node]?.inc() ?: 1 } }

        return nodeCounts.filter { (_, amount) -> amount >= minimumCount }.keys
    }
}
