package org.cafejojo.schaapi.miningpipeline.patterndetector.prefixspan

import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.models.SimpleNode
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xit

internal object PrefixSpanTest : Spek({
    describe("when extracting suffixes from sequences") {
        it("should not return any suffixes if no sequence has prefix") {
            val prefix = listOf(SimpleNode(), SimpleNode())
            val sequence1 = listOf(SimpleNode(), SimpleNode())
            val sequence2 = listOf(SimpleNode())

            val sequenceFinder = PrefixSpan(emptyList(), 0, TestNodeComparator())
            assertThat(sequenceFinder.extractSuffixes(prefix, listOf(sequence1, sequence2))).isEmpty()
        }

        it("should extract the suffix from sequences with the given prefix") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()

            val prefix = listOf(node1)
            val sequence1 = listOf(node1, node2)
            val sequence2 = listOf(SimpleNode())

            val sequenceFinder = PrefixSpan(emptyList(), 0, TestNodeComparator())
            assertThat(sequenceFinder.extractSuffixes(prefix, listOf(sequence1, sequence2)))
                .containsExactly(listOf(node2))
        }
    }

    describe("detecting patterns in a set of sequences") {
        it("should find the entire pattern in one sequence") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()

            val sequence = listOf(node1, node2, node3)

            val sequences = listOf(sequence)
            val frequent = PrefixSpan(sequences, 1, TestNodeComparator()).findFrequentPatterns()

            assertThat(frequent).hasSize(6)
            assertThat(frequent).contains(sequence)
        }

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
            val frequent = PrefixSpan(sequences, 2, TestNodeComparator()).findFrequentPatterns()

            assertThat(frequent).isEmpty()
        }

        it("should not find a pattern that occurs twice in the same sequence") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()

            val sequence = listOf(node1, node2, node3, node1, node2, node3)

            val sequences = listOf(sequence)
            val frequent = PrefixSpan(sequences, 1, TestNodeComparator()).findFrequentPatterns()

            assertThat(frequent).contains(listOf(node1, node2, node3))
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
            val node10 = SimpleNode()

            val sequence1 = listOf(node1, node2, node3)
            val sequence2 = listOf(node4, node5, node6)
            val sequence3 = listOf(node7, node8, node9, node10, node1, node2, node3)

            val sequences = listOf(sequence1, sequence2, sequence3)
            val frequent = PrefixSpan(sequences, 2, TestNodeComparator()).findFrequentPatterns()

            assertThat(frequent).contains(sequence1)
        }
    }

    describe("When mapping from patterns to sequences") {
        it("should find a mapping from patterns to sequences") {
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

            val sequence1 = listOf(node1, node2, node3, node4)
            val sequence2 = listOf(node4, node5, node6)
            val sequence3 = listOf(node7, node8, node9, node10, node1, node2, node3, node4)

            val sequences = listOf(sequence1, sequence2, sequence3)
            val patternDetector = PrefixSpan(sequences, 2, TestNodeComparator())

            patternDetector.findFrequentPatterns()
            val patterns = patternDetector.mapFrequentPatternsToSequences()

            assertThat(patterns[listOf(node1, node2, node3, node4)]).isEqualTo(listOf(sequence1, sequence3))
        }

        // TODO make test pass
        xit("should not store duplicate patterns") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()
            val node4 = SimpleNode()
            val node5 = SimpleNode()

            val node11 = SimpleNode()
            val node12 = SimpleNode()

            val sequence1 = listOf(node1, node2, node3, node4, node5)
            val sequence2 = listOf(node11, node12, node1, node2, node3, node4, node5)

            val sequences = listOf(sequence1, sequence2)
            val frequent = PrefixSpan(sequences, 2, TestNodeComparator()).findFrequentPatterns()

            assertThat(frequent).hasSize(amountOfPossibleSubSequences(5))
        }
    }
})
