package org.cafejojo.schaapi.pipeline.patterndetector.spam

import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.models.SimpleNode
import org.cafejojo.schaapi.pipeline.patterndetector.spam.FrequentSequenceFinder
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

internal class FrequentSequenceFinderTest : Spek({
    describe("detecting patterns in a set of paths") {
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

            val path1 = listOf(node1, node2, node2)
            val path2 = listOf(node3, node4, node5)
            val path3 = listOf(node6, node7, node8, node9, node1, node2, node2)

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

            val path1 = listOf(node1, node2, node3, node4)
            val path2 = listOf(node2, node1, node2)

            val paths = listOf(path1, path2)
            val patternDetector = FrequentSequenceFinder(paths, 2, TestNodeComparator())

            patternDetector.findFrequentSequences()
            val patterns = patternDetector.mapFrequentSequencesToPaths()

            assertThat(patterns[listOf(node1, node2)]).isEqualTo(listOf(path1, path2))
        }
    }
})
