package org.cafejojo.schaapi.pipeline.patterndetector.ccspan

import org.cafejojo.schaapi.models.PathUtil

class FrequentSequenceFinder<N : Any>(private val userPaths: List<List<N>>, private val minimumCount: Int) {
    val frequentClosedContigiousSequences = mutableListOf<List<N>>()
    val pathUtil = PathUtil<N>()
    val frequentItems = pathUtil.findFrequentNodesInPaths(userPaths, minimumCount)

    val previous = mutableListOf<Triple<List<N>, Int, MutableBoolean>>()
    val current = mutableListOf<Triple<List<N>, Int, MutableBoolean>>()

    fun findFrequentSequences(): List<List<N>> {
        var k = 2

        initGen()
        while (previous.isNotEmpty()) {
            val checkedSequences = mutableListOf<List<N>>()

            userPaths
                .filter { it.size >= k }
                .forEach { path ->
                    path.dropLast(k - 1).indices.forEach {
                        val subSequence = path.subList(it, it + k)
                        generateContiguousSequences(subSequence, checkedSequences, path)
                    }
                }

            generateClosedContiguousSequences()
            frequentClosedContigiousSequences.addAll(previous.map { it.first })

            k++

            previous.clear()
            previous.addAll(current)
            current.clear()
        }

        return frequentClosedContigiousSequences
    }

    private fun initGen() {

    }

    private fun generateContiguousSequences(
        subSequence: List<N>, checkedSequences: MutableList<List<N>>, parentPath: List<N>) {
        if (checkedSequences.contains(subSequence))
        else if (previous.any { it.first == makePre(it.first) } && current.any { it.first == makePost(it.first) }) {
            var support = 0

            userPaths.dropLastWhile { parentPath !== it }.dropLast(1).forEach {
                if (pathUtil.pathContainsSequenceCool(it, subSequence)) {
                    support++
                }
            }

            if (support >= minimumCount) {
                current += Triple(subSequence, support, MutableBoolean(true))
            } else {
                checkedSequences += subSequence
            }
        } else {
            checkedSequences += subSequence
        }
    }

    private fun makePre(sequence: List<N>) = sequence.subList(0, sequence.size - 2)
    private fun makePost(sequence: List<N>) = sequence.subList(1, sequence.size - 1)

    private fun generateClosedContiguousSequences() {
        current.forEach { (sequence, _, _) ->
            previous
                .filter { it.first == makePre(sequence) && it.third.bool }
                .forEach { it.third.bool = false }

            previous
                .filter { it.first == makePost(sequence) && it.third.bool }
                .forEach { it.third.bool = false }
        }
    }
}

data class MutableBoolean(var bool: Boolean)
