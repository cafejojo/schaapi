package org.cafejojo.schaapi.pipeline.patterndetector.ccspan

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.models.SimpleNode
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class FrequentSequenceFinderTest : Spek({
    describe("Good weather") {
        it("finds all frequent closed contiguous sequences") {
            val node1 = mock<SimpleNode> {}
            val node2 = mock<SimpleNode> {}
            val node3 = mock<SimpleNode> {}

            val sequence1 = listOf(node3, node1, node1, node2, node3)
            val sequence2 = listOf(node1, node2, node3, node2)
            val sequence3 = listOf(node3, node1, node2, node3)
            val sequence4 = listOf(node1, node2, node2, node3, node1)

            val frequentSequences = FrequentSequenceFinder(
                listOf(sequence1, sequence2, sequence3, sequence4),
                2,
                TestNodeComparator()
            ).findFrequentSequences()

            assertThat(frequentSequences).containsExactlyInAnyOrder(
                listOf(node3, node1),
                listOf(node1, node2),
                listOf(node2, node3),
                listOf(node1, node2, node3)
            )
        }
    }
})
