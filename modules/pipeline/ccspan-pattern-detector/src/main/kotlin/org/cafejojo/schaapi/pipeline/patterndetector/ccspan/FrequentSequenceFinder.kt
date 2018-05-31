package org.cafejojo.schaapi.pipeline.patterndetector.ccspan

import org.cafejojo.schaapi.models.PathUtil

class FrequentSequenceFinder<N : Any>(private val userPaths: List<List<N>>, private val minimumCount: Int) {
    private val pathUtil = PathUtil<N>()

    private val previous = mutableSetOf<Triple<List<N>, Int, MutableBoolean>>()
    private val current = mutableSetOf<Triple<List<N>, Int, MutableBoolean>>()

    fun findFrequentSequences(): List<List<N>> {
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
                        generateContiguousSequences(subSequence, checkedSequences, path)
                    }
                }

            generateClosedContiguousSequences()
            frequentClosedContiguousSequences.addAll(previous.map { it.first })

            k++

            previous.clear()
            previous.addAll(current)
            current.clear()
        }

        frequentClosedContiguousSequences.addAll(previous.map { it.first })

        return frequentClosedContiguousSequences
    }

    private fun initGen() {
        val checkedSequences = mutableListOf<List<N>>()

        userPaths.forEach { path ->
            path.forEach { element ->
                if (!checkedSequences.contains(listOf(element))) {
                    var support = 0
                    userPaths.forEach {
                        if (pathUtil.pathContainsSequenceCool(it, listOf(element))) {
                            support++
                        }
                    }

                    if (support >= minimumCount) {
                        previous += Triple(listOf(element), support, MutableBoolean(true))
                    }

                    checkedSequences += listOf(element)
                }
            }
        }
    }

    private fun generateContiguousSequences(
        subSequence: List<N>, checkedSequences: MutableList<List<N>>, parentPath: List<N>
    ) {
        if (checkedSequences.contains(subSequence)) return
        else if (previous.any { it.first == makePre(subSequence) } && previous.any { it.first == makePost(subSequence) }) {
            var support = 0

            userPaths.forEach {
                if (pathUtil.pathContainsSequenceCool(it, subSequence)) {
                    support++
                }
            }

            if (support >= minimumCount) {
                current += Triple(subSequence, support, MutableBoolean(true))
            }

            checkedSequences += subSequence
        } else {
            checkedSequences += subSequence
        }
    }

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

    private fun makePre(sequence: List<N>) = sequence.subList(0, sequence.size - 1)

    private fun makePost(sequence: List<N>) = sequence.subList(1, sequence.size)
}

data class MutableBoolean(var bool: Boolean)
