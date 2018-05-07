package org.cafejojo.schaapi.patterndetector

import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.patterndetector.PatternDetector.Companion.pathContainsSequence
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

internal class PatternDetectorTest : Spek({
    describe("when looking for a pattern in a path") {
        it("it should find a sequence in a path of length 1") {
            val node1 = TestNode()
            val path = listOf(node1)

            assertThat(pathContainsSequence(path, listOf(node1))).isTrue()
        }

        it("it should not find a sequence that isn't in the path") {
            val node1 = TestNode()
            val node2 = TestNode()
            val path = listOf(node1)

            assertThat(pathContainsSequence(path, listOf(node2))).isFalse()
        }

        it("it should find a sequence at the start of a path") {
            val node1 = TestNode()
            val node2 = TestNode()
            val node3 = TestNode()
            val node4 = TestNode()
            val node5 = TestNode()
            val node6 = TestNode()
            val path = listOf(node1, node2, node3, node4, node5, node6)

            assertThat(pathContainsSequence(path, listOf(node1, node2))).isTrue()
        }

        it("it should find a sequence in the middle of a path") {
            val node1 = TestNode()
            val node2 = TestNode()
            val node3 = TestNode()
            val node4 = TestNode()
            val node5 = TestNode()
            val node6 = TestNode()
            val path = listOf(node1, node2, node3, node4, node5, node6)

            assertThat(pathContainsSequence(path, listOf(node3, node4, node5))).isTrue()
        }

        it("it should find a sequence at the end of a path") {
            val node1 = TestNode()
            val node2 = TestNode()
            val node3 = TestNode()
            val node4 = TestNode()
            val node5 = TestNode()
            val node6 = TestNode()
            val path = listOf(node1, node2, node3, node4, node5, node6)

            assertThat(pathContainsSequence(path, listOf(node5, node6))).isTrue()
        }

        it("it should not find a out of order sequence that is not in a path") {
            val node1 = TestNode()
            val node2 = TestNode()
            val node3 = TestNode()
            val node4 = TestNode()
            val node5 = TestNode()
            val node6 = TestNode()
            val path = listOf(node1, node2, node3, node4, node5, node6)

            assertThat(pathContainsSequence(path, listOf(node5, node4))).isFalse()
        }

        it("it should not find a non-consecutive sequence that is not in a path") {
            val node2 = TestNode()
            val node3 = TestNode()
            val node4 = TestNode()
            val node5 = TestNode()
            val path = listOf(node2, node3, node4, node5)

            assertThat(pathContainsSequence(path, listOf(node2, node4, node5))).isFalse()
        }
    }

    describe("detecting patterns in a set of paths") {
        it("should find the entire pattern in one path") {
            val node1 = TestNode()
            val node2 = TestNode()
            val node3 = TestNode()

            val path = listOf(node1, node2, node3)

            val paths = listOf(path)
            val frequent = PatternDetector(paths, 1).findFrequentSequences()

            assertThat(frequent).hasSize(6)
            assertThat(frequent).contains(path)
        }

        it("should not find a pattern in a set of random nodes with support 2") {
            val node1 = TestNode()
            val node2 = TestNode()
            val node3 = TestNode()
            val node4 = TestNode()
            val node5 = TestNode()
            val node6 = TestNode()
            val node7 = TestNode()
            val node8 = TestNode()
            val node9 = TestNode()
            val node10 = TestNode()

            val path1 = listOf(node1, node2, node3)
            val path2 = listOf(node4, node5, node6)
            val path3 = listOf(node7, node8, node9, node10)

            val paths = listOf(path1, path2, path3)
            val frequent = PatternDetector(paths, 2).findFrequentSequences()

            assertThat(frequent).hasSize(0)
        }

        it("should find a pattern that occurs twice in the same path") {
            val node1 = TestNode()
            val node2 = TestNode()
            val node3 = TestNode()

            val path = listOf(node1, node2, node3, node1, node2, node3)

            val paths = listOf(path)
            val frequent = PatternDetector(paths, 2).findFrequentSequences()

            assertThat(frequent).contains(listOf(node1, node2, node3))
        }

        it("should find a pattern that occurs twice in two different paths") {
            val node1 = TestNode()
            val node2 = TestNode()
            val node3 = TestNode()
            val node4 = TestNode()
            val node5 = TestNode()
            val node6 = TestNode()
            val node7 = TestNode()
            val node8 = TestNode()
            val node9 = TestNode()
            val node10 = TestNode()

            val path1 = listOf(node1, node2, node3)
            val path2 = listOf(node4, node5, node6)
            val path3 = listOf(node7, node8, node9, node10, node1, node2, node3)

            val paths = listOf(path1, path2, path3)
            val frequent = PatternDetector(paths, 2).findFrequentSequences()

            assertThat(frequent).contains(path1)
        }
    }

    describe("When mapping between sequences and patterns") {
        it("should find a mapping from nodes to ") {
            val node1 = TestNode()
            val node2 = TestNode()
            val node3 = TestNode()
            val node4 = TestNode()
            val node5 = TestNode()
            val node6 = TestNode()
            val node7 = TestNode()
            val node8 = TestNode()
            val node9 = TestNode()
            val node10 = TestNode()

            val path1 = listOf(node1, node2, node3)
            val path2 = listOf(node4, node5, node6)
            val path3 = listOf(node7, node8, node9, node10, node1, node2, node3)

            val paths = listOf(path1, path2, path3)
            val patternDetector = PatternDetector(paths, 2)

            patternDetector.findFrequentSequences()
            val patterns = patternDetector.mapFrequentSequencesToPaths()

            assertThat(patterns[listOf(node1, node2, node3)]).isEqualTo(listOf(path1, path3))
        }
    }
})
