package org.cafejojo.schaapi.miningpipeline.patterndetector.spam

import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.models.SimpleNode
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

internal object SpamTest : Spek({
    describe("detecting patterns in a set of sequences") {
        it("should not find a pattern in a set of random nodes with support 2") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()
            val node4 = SimpleNode()
            val node5 = SimpleNode()
            val node6 = SimpleNode()
            val node7 = SimpleNode()
            val node8 = SimpleNode()
            val node9 = SimpleNode()
            val node10 = SimpleNode()

            val sequence1 = listOf(node1, node2, node3)
            val sequence2 = listOf(node4, node5, node6)
            val sequence3 = listOf(node7, node8, node9, node10)

            val sequences = listOf(sequence1, sequence2, sequence3)
            val frequent = Spam(sequences, 2, TestNodeComparator()).findFrequentPatterns()

            assertThat(frequent).isEmpty()
        }

        it("should find a pattern that occurs in two different sequences") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()
            val node4 = SimpleNode()
            val node5 = SimpleNode()
            val node6 = SimpleNode()
            val node7 = SimpleNode()
            val node8 = SimpleNode()
            val node9 = SimpleNode()

            val sequence1 = listOf(node1, node2, node2)
            val sequence2 = listOf(node3, node4, node5)
            val sequence3 = listOf(node6, node7, node8, node9, node1, node2, node2)

            val sequences = listOf(sequence1, sequence2, sequence3)
            val frequent = Spam(sequences, 2, TestNodeComparator()).findFrequentPatterns()

            assertThat(frequent).contains(sequence1)
        }
    }

    describe("When mapping between sequences and patterns") {
        it("should find a mapping from patterns to sequences") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()
            val node4 = SimpleNode()

            val sequence1 = listOf(node1, node2, node3, node4)
            val sequence2 = listOf(node2, node1, node2)

            val sequences = listOf(sequence1, sequence2)
            val patternDetector = Spam(sequences, 2, TestNodeComparator())

            patternDetector.findFrequentPatterns()
            val patterns = patternDetector.mapFrequentPatternsToSequences()

            assertThat(patterns[listOf(node1, node2)]).isEqualTo(listOf(sequence1, sequence2))
        }
    }
})
