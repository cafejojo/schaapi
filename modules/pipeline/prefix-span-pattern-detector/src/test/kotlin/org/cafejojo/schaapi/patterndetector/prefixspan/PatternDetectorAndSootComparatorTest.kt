package org.cafejojo.schaapi.patterndetector.prefixspan

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
            val node = mockJimpleNode()
            val path = listOf(node)

            val detector = PatternDetector(listOf(path), 1, GeneralizedSootComparator())
            detector.findFrequentSequences()

            Assertions.assertThat(detector.pathContainsSequence(path, listOf(node))).isTrue()
        }

        it("should find a pattern with multiple nodes which have different values with the same type") {
            val type1 = mock<Type> {}
            val type2 = mock<Type> {}
            val type3 = mock<Type> {}

            val node1 = mockJimpleNode(valueTypeLeft = type1, valueTypeRight = type3)
            val node2 = mockJimpleNode(valueTypeLeft = type2, valueTypeRight = type2)
            val node3 = mockJimpleNode(valueTypeLeft = type3, valueTypeRight = type1)
            val node4 = mockJimpleNode(valueTypeLeft = type1, valueTypeRight = type3)
            val node5 = mockJimpleNode(valueTypeLeft = type2, valueTypeRight = type2)
            val node6 = mockJimpleNode(valueTypeLeft = type3, valueTypeRight = type1)
            val node7 = mockJimpleNode()
            val node8 = mockJimpleNode()
            val node9 = mockJimpleNode()
            val node10 = mockJimpleNode()

            val path1 = listOf(node1, node2, node3)
            val path2 = listOf(node7, node8, node9, node10, node4, node5, node6)

            val paths = listOf(path1, path2)
            val frequent = PatternDetector(paths, 2, GeneralizedSootComparator()).findFrequentSequences()

            Assertions.assertThat(frequent).contains(
                listOf(
                    mockJimpleNode(valueTypeLeft = type1, valueTypeRight = type3),
                    mockJimpleNode(valueTypeLeft = type2, valueTypeRight = type2),
                    mockJimpleNode(valueTypeLeft = type3, valueTypeRight = type1)
                )
            )
        }

        it("should not find a pattern with multiple nodes which have different values and different types") {
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
            val frequent = PatternDetector(paths, 2, GeneralizedSootComparator()).findFrequentSequences()

            Assertions.assertThat(frequent).isEmpty()
        }
    }
})
