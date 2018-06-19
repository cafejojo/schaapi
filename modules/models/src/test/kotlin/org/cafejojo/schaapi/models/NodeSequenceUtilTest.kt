package org.cafejojo.schaapi.models

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

internal class NodeSequenceUtilTest : Spek({
    val nodeSequenceUtil = NodeSequenceUtil<Node>()

    describe("when looking for common sequences of simple nodes in a sequence") {
        it("should find a sequence in a sequence of length 1") {
            val node1 = SimpleNode()
            val sequence = listOf(node1)

            assertThat(nodeSequenceUtil.sequenceContainsSubSequence(
                sequence, listOf(node1), TestNodeComparator()
            )).isTrue()
        }

        it("should not find a sequence that isn't in the sequence") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val sequence = listOf(node1)

            assertThat(nodeSequenceUtil.sequenceContainsSubSequence(
                sequence, listOf(node2), TestNodeComparator()
            )).isFalse()
        }

        it("should find a sequence at the start of a sequence") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()
            val node4 = SimpleNode()
            val node5 = SimpleNode()
            val node6 = SimpleNode()
            val sequence = listOf(node1, node2, node3, node4, node5, node6)

            assertThat(nodeSequenceUtil.sequenceContainsSubSequence(
                sequence, listOf(node1, node2), TestNodeComparator()
            )).isTrue()
        }

        it("should find a sequence in the middle of a sequence") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()
            val node4 = SimpleNode()
            val node5 = SimpleNode()
            val node6 = SimpleNode()
            val sequence = listOf(node1, node2, node3, node4, node5, node6)

            assertThat(nodeSequenceUtil.sequenceContainsSubSequence(
                sequence, listOf(node3, node4, node5), TestNodeComparator()
            )).isTrue()
        }

        it("should find a sequence at the end of a sequence") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()
            val node4 = SimpleNode()
            val node5 = SimpleNode()
            val node6 = SimpleNode()
            val sequence = listOf(node1, node2, node3, node4, node5, node6)

            assertThat(nodeSequenceUtil.sequenceContainsSubSequence(
                sequence, listOf(node5, node6), TestNodeComparator()
            )).isTrue()
        }

        it("should not find an out-of-order sequence that is not in a sequence") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()
            val node4 = SimpleNode()
            val node5 = SimpleNode()
            val node6 = SimpleNode()
            val sequence = listOf(node1, node2, node3, node4, node5, node6)

            assertThat(nodeSequenceUtil.sequenceContainsSubSequence(
                sequence, listOf(node5, node4), TestNodeComparator()
            )).isFalse()
        }

        it("should not find a non-consecutive sequence that is not in a sequence") {
            val node2 = SimpleNode()
            val node3 = SimpleNode()
            val node4 = SimpleNode()
            val node5 = SimpleNode()
            val sequence = listOf(node2, node3, node4, node5)

            assertThat(nodeSequenceUtil.sequenceContainsSubSequence(
                sequence, listOf(node2, node4, node5), TestNodeComparator()
            )).isFalse()
        }
    }
})
