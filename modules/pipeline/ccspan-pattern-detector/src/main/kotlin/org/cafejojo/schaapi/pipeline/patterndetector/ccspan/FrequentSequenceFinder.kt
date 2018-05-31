package org.cafejojo.schaapi.pipeline.patterndetector.ccspan

import org.cafejojo.schaapi.models.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.PathUtil
import org.cafejojo.schaapi.pipeline.Pattern

class FrequentSequenceFinder<N : Node>(
    private val userPaths: List<List<N>>,
    private val minimumCount: Int,
    private val comparator: GeneralizedNodeComparator<N>
) {
    private val pathUtil = PathUtil<N>()
    private fun List<N>.post() = this.subList(1, this.size)
    private fun List<N>.pre() = this.subList(0, this.size - 1)

    private val previous = mutableSetOf<SequenceTriple<N>>()
    private val current = mutableSetOf<SequenceTriple<N>>()

    fun findFrequentSequences(): List<Pattern<N>> {
        val frequentClosedContiguousSequences = mutableListOf<List<N>>()
        initGen()

        var k = 2
        while (previous.isNotEmpty()) {
            val checkedSequences = mutableListOf<List<N>>()

            userPaths
                .filter { it.size >= k }
                .forEach { path ->
                    path.dropLast(k - 1).indices.forEach {
                        val subSequence = path.subList(it, it + k)
                        generateContiguousSequences(subSequence, checkedSequences)
                    }
                }

            generateClosedContiguousSequences()
            frequentClosedContiguousSequences.addAll(
                current
                    .filter { it.isClosedSequence }
                    .map { it.sequence }
            )

            k++

            previous.clear()
            previous.addAll(current)
            current.clear()
        }

        return frequentClosedContiguousSequences
    }

    private fun initGen() {
        val checkedSequences = mutableListOf<List<N>>()

        userPaths.forEach { path ->
            path.forEach { element ->
                if (!checkedSequences.contains(listOf(element))) {
                    val support = userPaths.filter { pathUtil.pathContainsSequence(it, listOf(element), comparator) }.size
                    if (support >= minimumCount) previous += SequenceTriple(listOf(element), support, true)
                    checkedSequences += listOf(element)
                }
            }
        }
    }

    private fun generateContiguousSequences(subSequence: List<N>, checkedSequences: MutableList<List<N>>) {
        if (checkedSequences.contains(subSequence)) return

        if (previous.any { it.sequence == subSequence.pre() } && previous.any { it.sequence == subSequence.post() }) {
            val support = userPaths.filter { pathUtil.pathContainsSequence(it, subSequence, comparator) }.size
            if (support >= minimumCount) current += SequenceTriple(subSequence, support, true)
        }

        checkedSequences += subSequence
    }

    private fun generateClosedContiguousSequences() {
        current.forEach { (sequence, _, _) ->
            previous
                .filter { it.isClosedSequence && it.sequence == sequence.pre() }
                .forEach { it.isClosedSequence = false }

            previous
                .filter { it.isClosedSequence && it.sequence == sequence.post() }
                .forEach { it.isClosedSequence = false }
        }
    }
}

data class SequenceTriple<N>(val sequence: List<N>, val support: Int, var isClosedSequence: Boolean)
