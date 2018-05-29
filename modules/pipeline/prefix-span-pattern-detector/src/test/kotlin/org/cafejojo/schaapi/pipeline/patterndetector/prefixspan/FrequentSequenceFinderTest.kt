package org.cafejojo.schaapi.pipeline.patterndetector.prefixspan

import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.models.SimpleNode
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xit

internal class FrequentSequenceFinderTest : Spek({
    describe("when extracting suffixes from sequences") {
        it("should not return any suffixes if no path has prefix") {
            val prefix = listOf(SimpleNode(), SimpleNode())
            val path1 = listOf(SimpleNode(), SimpleNode())
            val path2 = listOf(SimpleNode())

            val sequenceFinder = FrequentSequenceFinder(emptyList(), 0, TestNodeComparator())
            assertThat(sequenceFinder.extractSuffixes(prefix, listOf(path1, path2))).isEmpty()
        }

        it("should extract the suffix from paths with the given prefix") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()

            val prefix = listOf(node1)
            val path1 = listOf(node1, node2)
            val path2 = listOf(SimpleNode())

            val sequenceFinder = FrequentSequenceFinder(emptyList(), 0, TestNodeComparator())
            assertThat(sequenceFinder.extractSuffixes(prefix, listOf(path1, path2))).containsExactly(listOf(node2))
        }
    }

    describe("detecting patterns in a set of paths") {
        it("should find the entire pattern in one path") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()

            val path = listOf(node1, node2, node3)

            val paths = listOf(path)
            val frequent = FrequentSequenceFinder(paths, 1, TestNodeComparator()).findFrequentSequences()

            assertThat(frequent).hasSize(6)
            assertThat(frequent).contains(path)
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

            val path1 = listOf(node1, node2, node3)
            val path2 = listOf(node4, node5, node6)
            val path3 = listOf(node7, node8, node9, node10)

            val paths = listOf(path1, path2, path3)
            val frequent = FrequentSequenceFinder(paths, 2, TestNodeComparator()).findFrequentSequences()

            assertThat(frequent).isEmpty()
        }

        it("should find a pattern that occurs twice in the same path") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()

            val path = listOf(node1, node2, node3, node1, node2, node3)

            val paths = listOf(path)
            val frequent = FrequentSequenceFinder(paths, 2, TestNodeComparator()).findFrequentSequences()

            assertThat(frequent).contains(listOf(node1, node2, node3))
        }

        it("should find a pattern that occurs in two different paths") {
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

            val path1 = listOf(node1, node2, node3)
            val path2 = listOf(node4, node5, node6)
            val path3 = listOf(node7, node8, node9, node10, node1, node2, node3)

            val paths = listOf(path1, path2, path3)
            val frequent = FrequentSequenceFinder(paths, 2, TestNodeComparator()).findFrequentSequences()

            assertThat(frequent).contains(path1)
        }
    }

    describe("When mapping between sequences and patterns") {
        it("should find a mapping from sequences to paths") {
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

            val path1 = listOf(node1, node2, node3, node4)
            val path2 = listOf(node4, node5, node6)
            val path3 = listOf(node7, node8, node9, node10, node1, node2, node3, node4)

            val paths = listOf(path1, path2, path3)
            val patternDetector = FrequentSequenceFinder(paths, 2, TestNodeComparator())

            patternDetector.findFrequentSequences()
            val patterns = patternDetector.mapFrequentPatternsToPaths()

            assertThat(patterns[listOf(node1, node2, node3, node4)]).isEqualTo(listOf(path1, path3))
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

            val path1 = listOf(node1, node2, node3, node4, node5)
            val path2 = listOf(node11, node12, node1, node2, node3, node4, node5)

            val paths = listOf(path1, path2)
            val frequent = FrequentSequenceFinder(paths, 2, TestNodeComparator()).findFrequentSequences()

            assertThat(frequent).hasSize(amountOfPossibleSubSequences(5))
        }
    }
})
