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
    private fun List<N>.pre() = this.subList(0, this.size - 1)
    private fun List<N>.post() = this.subList(1, this.size)

    private val previous = mutableSetOf<SequenceTriple<N>>()
    private val current = mutableSetOf<SequenceTriple<N>>()

    fun findFrequentSequences(): List<Pattern<N>> {
        initGen()
        val frequentClosedContiguousSequences = mutableListOf<Pattern<N>>()

        var subSequenceLength = 2
        while (previous.isNotEmpty()) {
            val checkedSequences = mutableListOf<List<N>>()

            userPaths
                .filter { it.size >= subSequenceLength }
                .forEach { generateAllContiguousSequences(it, subSequenceLength, checkedSequences) }

            generateClosedContiguousSequences()
            frequentClosedContiguousSequences.addAll(current.filter { it.isClosedSequence }.map { it.sequence })

            shiftCurrent()
            subSequenceLength++
        }

        return frequentClosedContiguousSequences
    }

    private fun generateAllContiguousSequences(
        path: List<N>,
        subSequenceLength: Int,
        checkedSequences: MutableList<List<N>>
    ) {
        path.dropLast(subSequenceLength - 1).indices.forEach {
            val subSequence = path.subList(it, it + subSequenceLength)
            generateContiguousSequences(path.subList(it, it + subSequenceLength), checkedSequences)

            checkedSequences += subSequence
        }
    }

    private fun shiftCurrent() {
        previous.clear()
        previous.addAll(current)
        current.clear()
    }

    private fun initGen() {
        val checkedSequences = mutableListOf<N>()

        userPaths.flatten().forEach { element ->
            if (!checkedSequences.contains(element)) {
                val support = userPaths.filter { pathUtil.pathContainsSequence(it, listOf(element), comparator) }.size
                if (support >= minimumCount) previous += SequenceTriple(listOf(element), support)

                checkedSequences += element
            }
        }
    }

    private fun generateContiguousSequences(subSequence: List<N>, checkedSequences: MutableList<List<N>>) {
        if (checkedSequences.contains(subSequence)) return

        if (previous.any { it.sequence == subSequence.pre() } && previous.any { it.sequence == subSequence.post() }) {
            val support = userPaths.filter { pathUtil.pathContainsSequence(it, subSequence, comparator) }.size
            if (support >= minimumCount) current += SequenceTriple(subSequence, support)
        }
    }

    private fun generateClosedContiguousSequences() {
        current.forEach { (sequence, _, _) ->
            previous
                .filter { it.isClosedSequence && (it.sequence == sequence.pre() || it.sequence == sequence.post() ) }
                .forEach { it.isClosedSequence = false }
        }
    }
}

data class SequenceTriple<N>(val sequence: List<N>, val support: Int, var isClosedSequence: Boolean = true)
