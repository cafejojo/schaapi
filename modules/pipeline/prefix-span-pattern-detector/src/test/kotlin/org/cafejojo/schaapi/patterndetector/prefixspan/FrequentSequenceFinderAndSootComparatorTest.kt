package org.cafejojo.schaapi.patterndetector.prefixspan

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.libraryusagegraph.jimple.compare.GeneralizedNodeComparator
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xit
import soot.Type

class FrequentSequenceFinderAndSootComparatorTest : Spek({
    describe("when looking for common sequences in patterns of statements using the generalized soot comparator") {
        it("should find a sequence of path length 1") {
            val node = mockJimpleNode()
            val path = listOf(node)

            val detector = FrequentSequenceFinder(listOf(path), 1, GeneralizedNodeComparator())
            detector.findFrequentSequences()

            assertThat(detector.pathContainsSequence(path, listOf(node))).isTrue()
        }

        it("should find a pattern with multiple nodes which have different values with the same type") {
            val type1 = mock<Type> {}
            val type2 = mock<Type> {}
            val type3 = mock<Type> {}

            val node1 = mockJimpleNode(type1, type3)
            val node2 = mockJimpleNode(type2, type2)
            val node3 = mockJimpleNode(type3, type1)
            val node4 = mockJimpleNode(type1, type3)
            val node5 = mockJimpleNode(type2, type2)
            val node6 = mockJimpleNode(type3, type1)
            val node7 = mockJimpleNode()
            val node8 = mockJimpleNode()
            val node9 = mockJimpleNode()
            val node10 = mockJimpleNode()

            val path1 = listOf(node1, node2, node3)
            val path2 = listOf(node7, node8, node9, node10, node4, node5, node6)

            val paths = listOf(path1, path2)
            val frequent = FrequentSequenceFinder(paths, 2, GeneralizedNodeComparator()).findFrequentSequences()

            assertThat(frequent).contains(listOf(node1, node2, node3))
        }

        // TODO make test pass
        xit("should not store duplicate patterns") {
            val type1 = mock<Type> {}
            val type2 = mock<Type> {}
            val type3 = mock<Type> {}

            val node1 = mockJimpleNode(type1, type3)
            val node2 = mockJimpleNode(type2, type2)
            val node3 = mockJimpleNode(type3, type1)
            val node4 = mockJimpleNode(type2, type1)
            val node5 = mockJimpleNode(type1, type2)

            val node6 = mockJimpleNode(type1, type3)
            val node7 = mockJimpleNode(type2, type2)
            val node8 = mockJimpleNode(type3, type1)
            val node9 = mockJimpleNode(type2, type1)
            val node10 = mockJimpleNode(type1, type2)

            val node11 = mockJimpleNode()
            val node12 = mockJimpleNode()

            val path1 = listOf(node1, node2, node3, node4, node5)
            val path2 = listOf(node11, node12, node6, node7, node8, node9, node10)

            val paths = listOf(path1, path2)
            val frequent = FrequentSequenceFinder(paths, 2, GeneralizedNodeComparator()).findFrequentSequences()

            assertThat(frequent).hasSize(amountOfPossibleSubSequences(5))
        }

        it("should find a pattern with multiple nodes which have the same value") {
            val value1 = mockTypedValue()
            val value2 = mockTypedValue()
            val value3 = mockTypedValue()

            val node1 = mockJimpleNode(value2, value2)
            val node2 = mockJimpleNode(value3, value1)
            val node3 = mockJimpleNode(value1, value3)
            val node4 = mockJimpleNode(value2, value1)

            val node5 = mockJimpleNode(value2, value2)
            val node6 = mockJimpleNode(value3, value1)
            val node7 = mockJimpleNode(value1, value3)
            val node8 = mockJimpleNode(value2, value1)

            val node9 = mockJimpleNode()
            val node10 = mockJimpleNode()

            val path1 = listOf(node1, node2, node3, node4)
            val path2 = listOf(node9, node10, node5, node6, node7, node8)

            val paths = listOf(path1, path2)
            val frequent = FrequentSequenceFinder(paths, 2, GeneralizedNodeComparator()).findFrequentSequences()

            assertThat(frequent).contains(listOf(node1, node2, node3, node4))
        }

        it("should find a pattern when nodes don't have the same value but are the same node") {
            val node1 = mockJimpleNode()
            val node2 = mockJimpleNode()
            val node3 = mockJimpleNode()
            val node7 = mockJimpleNode()
            val node8 = mockJimpleNode()
            val node9 = mockJimpleNode()
            val node10 = mockJimpleNode()

            val path1 = listOf(node1, node2, node3)
            val path2 = listOf(node7, node8, node9, node10, node1, node2, node3)

            val paths = listOf(path1, path2)
            val frequent = FrequentSequenceFinder(paths, 2, GeneralizedNodeComparator()).findFrequentSequences()

            assertThat(frequent).contains(listOf(node1, node2, node3))
        }

        it("should not find a pattern when there are only unique nodes") {
            val node1 = mockJimpleNode()
            val node2 = mockJimpleNode()
            val node3 = mockJimpleNode()
            val node4 = mockJimpleNode()
            val node5 = mockJimpleNode()
            val node6 = mockJimpleNode()
            val node7 = mockJimpleNode()
            val node8 = mockJimpleNode()
            val node9 = mockJimpleNode()
            val node10 = mockJimpleNode()

            val path1 = listOf(node1, node2, node3)
            val path2 = listOf(node7, node8, node9, node10, node4, node5, node6)

            val paths = listOf(path1, path2)
            val frequent = FrequentSequenceFinder(paths, 2, GeneralizedNodeComparator()).findFrequentSequences()

            assertThat(frequent).isEmpty()
        }

        // TODO make test pass
        xit("should not find a pattern when multiple patterns use the same values") {
            val value1 = mockTypedValue()
            val value2 = mockTypedValue()
            val value3 = mockTypedValue()

            val node1 = mockJimpleNode(value2, value2)
            val node2 = mockJimpleNode(value3, value1)
            val node3 = mockJimpleNode(value1, value3)
            val node4 = mockJimpleNode(value2, value1)

            val node5 = mockJimpleNode(value1, value3)
            val node6 = mockJimpleNode(value2, value2)
            val node7 = mockJimpleNode(value3, value1)
            val node8 = mockJimpleNode(value2, value1)

            val node9 = mockJimpleNode()
            val node10 = mockJimpleNode()

            val path1 = listOf(node4, node2, node3, node1)
            val path2 = listOf(node9, node10, node5, node6, node7, node8)

            val paths = listOf(path1, path2)
            val frequent = FrequentSequenceFinder(paths, 2, GeneralizedNodeComparator()).findFrequentSequences()

            assertThat(frequent).hasSize(4)
        }
    }
})
