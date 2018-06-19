package org.cafejojo.schaapi.models

/**
 * Utilities for sequences of [Node]s.
 */
class NodeSequenceUtil<N : Node> {
    /**
     * Checks whether a given [subSequence] can be found within a given [subSequence].
     *
     * @param sequence the [sequence] which may contain the given [subSequence]
     * @param subSequence the [subSequence] which may be contained in [sequence]
     * @return true if [sequence] contains the given [subSequence]
     */
    fun sequenceContainsSubSequence(sequence: List<N>, subSequence: List<N>, comparator: GeneralizedNodeComparator<N>) =
        sequence.indices.any { sequenceIndex ->
            subSequence.indices.all { subSequenceIndex ->
                sequenceIndex + subSequenceIndex < sequence.size &&
                    comparator.satisfies(sequence[sequenceIndex + subSequenceIndex], subSequence[subSequenceIndex])
            }
        }

    /**
     * Finds all nodes occurring at least [minimumCount] times in the given [sequences].
     *
     * @param sequences the sequences to search in
     * @param minimumCount the minimum number of times a node needs to occur
     * @return the set of nodes occurring at least [minimumCount] times
     */
    fun findFrequentNodesInSequences(sequences: Collection<List<N>>, minimumCount: Int): Map<N, Long> {
        val nodeCounts = CustomEqualsHashMap<N, Long>(Node.Companion::equiv, Node::equivHashCode)
        val sequenceSets = sequences.map { sequence ->
            CustomEqualsHashSet<N>(Node.Companion::equiv, Node::equivHashCode).also { it.addAll(sequence) }
        }

        sequenceSets.forEach { sequenceSet ->
            sequenceSet.forEach { node -> nodeCounts[node] = nodeCounts[node]?.inc() ?: 1 }
        }

        return nodeCounts.filter { (_, amount) -> amount >= minimumCount }
    }
}
