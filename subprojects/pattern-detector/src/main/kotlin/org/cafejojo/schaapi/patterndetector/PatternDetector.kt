package org.cafejojo.schaapi.patterndetector

import org.cafejojo.schaapi.usagegraphgenerator.Node

fun main(args: Array<String>) {
    println("I am the pattern detector!")
}

/**
 * Pattern detector class, which aims to find all the frequent sequences of nodes in the given data set.
 */
class PatternDetector(private val dataSet: List<List<Node>>) {
    /**
     * Find all the frequent patterns in the projected data set using the prefix space algorithm.
     *
     * @param minimumSupport The minimum amount of times a node must appear in the dataset for it to be
     * considered a frequent item.
     * @return List of sequences, each a list of nodes, that are common within the given dataset. Note that
     * sub sequences can also be given, as these can also be common.
     */
    fun prefixSpace(minimumSupport: Int) = prefixSpace(frequentItems = frequentItems(minimumSupport))

    private fun prefixSpace(
        prefix: List<Node> = emptyList(),
        frequentItems: Set<Node>,
        projectedDataSet: List<List<Node>> = dataSet,
        frequentSequences: MutableList<List<Node>> = mutableListOf()
    ): List<List<Node>> {
        frequentItems.forEach({ frequentItem ->
            val newSequence = prefix + frequentItem
            val newSequenceOnlyLast = listOf(prefix.last(), frequentItem)

            if (projectedDataSet.contains(newSequence) || projectedDataSet.contains(newSequenceOnlyLast)) {
                frequentSequences += newSequence

                val newProjectedDataSet: MutableList<List<Node>> = mutableListOf()
                projectedDataSet.forEach({ sequence ->
                    val extractedPrefix = sequence.subList(0, newSequence.size)
                    if (extractedPrefix == newSequence) {
                        newProjectedDataSet += sequence.subList(newSequence.size + 1, sequence.size)
                    }
                })

                prefixSpace(newSequence, frequentItems, projectedDataSet, frequentSequences)
            }
        })

        return frequentSequences
    }

    private fun frequentItems(minimumSupport: Int): Set<Node> {
        val values: MutableMap<Node, Int> = HashMap()
        dataSet.forEach({ sequence ->
            sequence.forEach({ node ->
                if (!values.containsKey(node)) values[node] = 0
                values[node]?.inc()
            })
        })

        val items: MutableSet<Node> = HashSet()
        values.forEach({ (node, amount) -> if (amount >= minimumSupport) items += node })

        return items
    }
}
