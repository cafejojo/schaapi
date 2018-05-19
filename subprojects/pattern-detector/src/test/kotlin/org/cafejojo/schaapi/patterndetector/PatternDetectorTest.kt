package org.cafejojo.schaapi.patterndetector

import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.patterndetector.PatternDetector.Companion.extractSuffixes
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

internal class PatternDetectorTest : Spek({
    describe("when looking for common sequences of simple nodes in a path") {
        it("it should find a sequence in a path of length 1") {
            val node1 = TestNode()
            val path = listOf(node1)

            val detector = PatternDetector(listOf(path), 1, TestNodeComparator())
            detector.findFrequentSequences()

            assertThat(detector.pathContainsSequence(path, listOf(node1))).isTrue()
        }

        it("should not find a sequence that isn't in the path") {
            val node1 = TestNode()
            val node2 = TestNode()
            val path = listOf(node1)

            val detector = PatternDetector(listOf(path), 1, TestNodeComparator())
            detector.findFrequentSequences()

            assertThat(detector.pathContainsSequence(path, listOf(node2))).isFalse()
        }

        it("should find a sequence at the start of a path") {
            val node1 = TestNode()
            val node2 = TestNode()
            val node3 = TestNode()
            val node4 = TestNode()
            val node5 = TestNode()
            val node6 = TestNode()
            val path = listOf(node1, node2, node3, node4, node5, node6)

            val detector = PatternDetector(listOf(path), 1, TestNodeComparator())
            detector.findFrequentSequences()

            assertThat(detector.pathContainsSequence(path, listOf(node1, node2))).isTrue()
        }

        it("should find a sequence in the middle of a path") {
            val node1 = TestNode()
            val node2 = TestNode()
            val node3 = TestNode()
            val node4 = TestNode()
            val node5 = TestNode()
            val node6 = TestNode()
            val path = listOf(node1, node2, node3, node4, node5, node6)

            val detector = PatternDetector(listOf(path), 1, TestNodeComparator())
            detector.findFrequentSequences()

            assertThat(detector.pathContainsSequence(path, listOf(node3, node4, node5))).isTrue()
        }

        it("should find a sequence at the end of a path") {
            val node1 = TestNode()
            val node2 = TestNode()
            val node3 = TestNode()
            val node4 = TestNode()
            val node5 = TestNode()
            val node6 = TestNode()
            val path = listOf(node1, node2, node3, node4, node5, node6)

            val detector = PatternDetector(listOf(path), 1, TestNodeComparator())
            detector.findFrequentSequences()

            assertThat(detector.pathContainsSequence(path, listOf(node5, node6))).isTrue()
        }

        it("should not find an out-of-order sequence that is not in a path") {
            val node1 = TestNode()
            val node2 = TestNode()
            val node3 = TestNode()
            val node4 = TestNode()
            val node5 = TestNode()
            val node6 = TestNode()
            val path = listOf(node1, node2, node3, node4, node5, node6)

            val detector = PatternDetector(listOf(path), 1, TestNodeComparator())
            detector.findFrequentSequences()

            assertThat(detector.pathContainsSequence(path, listOf(node5, node4))).isFalse()
        }

        it("should not find a non-consecutive sequence that is not in a path") {
            val node2 = TestNode()
            val node3 = TestNode()
            val node4 = TestNode()
            val node5 = TestNode()
            val path = listOf(node2, node3, node4, node5)

            val detector = PatternDetector(listOf(path), 1, TestNodeComparator())
            detector.findFrequentSequences()

            assertThat(detector.pathContainsSequence(path, listOf(node2, node4, node5))).isFalse()
        }
    }

    describe("when extracting suffixes from sequences") {
        it("should not return any suffixes if no path has prefix") {
            val prefix = listOf(TestNode(), TestNode())
            val path1 = listOf(TestNode(), TestNode())
            val path2 = listOf(TestNode())

            assertThat(extractSuffixes(prefix, listOf(path1, path2))).isEmpty()
        }

        it("should extract the suffix from paths with the given prefix") {
            val node1 = TestNode()
            val node2 = TestNode()

            val prefix = listOf(node1)
            val path1 = listOf(node1, node2)
            val path2 = listOf(TestNode())

            assertThat(extractSuffixes(prefix, listOf(path1, path2))).containsExactly(listOf(node2))
        }
    }

    describe("detecting patterns in a set of paths") {
        it("should find the entire pattern in one path") {
            val node1 = TestNode()
            val node2 = TestNode()
            val node3 = TestNode()

            val path = listOf(node1, node2, node3)

            val paths = listOf(path)
            val frequent = PatternDetector(paths, 1, TestNodeComparator()).findFrequentSequences()

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
            val frequent = PatternDetector(paths, 2, TestNodeComparator()).findFrequentSequences()

            assertThat(frequent).isEmpty()
        }

        it("should find a pattern that occurs twice in the same path") {
            val node1 = TestNode()
            val node2 = TestNode()
            val node3 = TestNode()

            val path = listOf(node1, node2, node3, node1, node2, node3)

            val paths = listOf(path)
            val frequent = PatternDetector(paths, 2, TestNodeComparator()).findFrequentSequences()

            assertThat(frequent).contains(listOf(node1, node2, node3))
        }

        it("should find a pattern that occurs in two different paths") {
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
            val frequent = PatternDetector(paths, 2, TestNodeComparator()).findFrequentSequences()

            assertThat(frequent).contains(path1)
        }
    }

    describe("When mapping between sequences and patterns") {
        it("should find a mapping from sequences to paths") {
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
            val patternDetector = PatternDetector(paths, 2, TestNodeComparator())

            patternDetector.findFrequentSequences()
            val patterns = patternDetector.mapFrequentSequencesToPaths()

            assertThat(patterns[listOf(node1, node2, node3)]).isEqualTo(listOf(path1, path3))
        }
    }
})
