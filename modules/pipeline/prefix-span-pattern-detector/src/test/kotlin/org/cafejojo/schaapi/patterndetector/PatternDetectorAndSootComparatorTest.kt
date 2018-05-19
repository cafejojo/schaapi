package org.cafejojo.schaapi.patterndetector

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.compare.GeneralizedSootComparator
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.Type

class PatternDetectorAndSootComparatorTest : Spek({
    describe("when looking for common sequences in patterns of statements using the generalized soot comparator") {
        it("should find a sequence of path length 1") {
            val node = mockSootNode()
            val path = listOf(node)

            val detector = PatternDetector(listOf(path), 1, GeneralizedSootComparator())
            detector.findFrequentSequences()

            Assertions.assertThat(detector.pathContainsSequence(path, listOf(node))).isTrue()
        }

        it("should find a pattern with multiple nodes which have different values with the same type") {
            val type1 = mock<Type> {}
            val type2 = mock<Type> {}
            val type3 = mock<Type> {}

            val node1 = mockSootNode(valueTypeLeft = type1, valueTypeRight = type3)
            val node2 = mockSootNode(valueTypeLeft = type2, valueTypeRight = type2)
            val node3 = mockSootNode(valueTypeLeft = type3, valueTypeRight = type1)
            val node4 = mockSootNode(valueTypeLeft = type1, valueTypeRight = type3)
            val node5 = mockSootNode(valueTypeLeft = type2, valueTypeRight = type2)
            val node6 = mockSootNode(valueTypeLeft = type3, valueTypeRight = type1)
            val node7 = mockSootNode()
            val node8 = mockSootNode()
            val node9 = mockSootNode()
            val node10 = mockSootNode()

            val path1 = listOf(node1, node2, node3)
            val path2 = listOf(node7, node8, node9, node10, node4, node5, node6)

            val paths = listOf(path1, path2)
            val frequent = PatternDetector(paths, 2, GeneralizedSootComparator()).findFrequentSequences()

            Assertions.assertThat(frequent).contains(
                listOf(
                    mockSootNode(valueTypeLeft = type1, valueTypeRight = type3),
                    mockSootNode(valueTypeLeft = type2, valueTypeRight = type2),
                    mockSootNode(valueTypeLeft = type3, valueTypeRight = type1)
                )
            )
        }

        it("should not find a pattern with multiple nodes which have different values and different types") {
            val node1 = mockSootNode()
            val node2 = mockSootNode()
            val node3 = mockSootNode()
            val node4 = mockSootNode()
            val node5 = mockSootNode()
            val node6 = mockSootNode()
            val node7 = mockSootNode()
            val node8 = mockSootNode()
            val node9 = mockSootNode()
            val node10 = mockSootNode()

            val path1 = listOf(node1, node2, node3)
            val path2 = listOf(node7, node8, node9, node10, node4, node5, node6)

            val paths = listOf(path1, path2)
            val frequent = PatternDetector(paths, 2, GeneralizedSootComparator()).findFrequentSequences()

            Assertions.assertThat(frequent).isEmpty()
        }
    }
})
