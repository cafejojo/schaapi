package org.cafejojo.schaapi.pipeline.patterndetector.ccspan

import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.SimpleNode
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class CCSpanTest : Spek({
    describe("mining of frequent closed contiguous sequences") {
        it("finds all frequent closed contiguous sequences") {
            val nodeConverter = NodeConverter()
            val sequences = nodeConverter.convertToList(
                listOf(3, 1, 1, 2, 3),
                listOf(1, 2, 3, 2),
                listOf(3, 1, 2, 3),
                listOf(1, 2, 2, 3, 1)
            )

            val frequentSequences = CCSpan(sequences, 2, TestNodeComparator()).findFrequentPatterns()

            assertThat(frequentSequences).containsExactlyInAnyOrderElementsOf(nodeConverter.convertToList(
                listOf(3, 1),
                listOf(1, 2),
                listOf(2, 3),
                listOf(1, 2, 3)
            ))
        }

        it("finds only one closed contiguous sequence in a single sequence with support 1") {
            val nodeConverter = NodeConverter()
            val sequences = nodeConverter.convertToList(
                listOf(1, 2, 3)
            )

            val frequentSequences = CCSpan(sequences, 1, TestNodeComparator()).findFrequentPatterns()

            assertThat(frequentSequences).containsExactlyInAnyOrderElementsOf(nodeConverter.convertToList(
                listOf(1, 2, 3)
            ))
        }

        it("finds no frequent patterns in an empty list of sequences") {
            val sequences = listOf<List<Node>>()

            val frequentSequences = CCSpan(sequences, 2, TestNodeComparator()).findFrequentPatterns()

            assertThat(frequentSequences).isEmpty()
        }

        it("finds no patterns with a high support") {
            val nodeConverter = NodeConverter()
            val sequences = nodeConverter.convertToList(
                listOf(3, 1, 1, 2, 3),
                listOf(1, 2, 3, 2),
                listOf(3, 1, 2, 3),
                listOf(1, 2, 2, 3, 1)
            )

            val frequentSequences = CCSpan(sequences, 5, TestNodeComparator()).findFrequentPatterns()

            assertThat(frequentSequences).isEmpty()
        }
    }
})

class NodeConverter {
    private val nodes = mutableMapOf<Int, Node>()

    fun convertToList(vararg sequences: List<Int>) =
        sequences.map { sequence ->
            sequence.map { nodeId ->
                nodes[nodeId] ?: SimpleNode().also { nodes[nodeId] = it }
            }
        }
}
