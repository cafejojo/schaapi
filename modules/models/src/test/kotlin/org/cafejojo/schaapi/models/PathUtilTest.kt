package org.cafejojo.schaapi.models

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

internal class PathUtilTest : Spek({
    val pathUtil = PathUtil<Node>()

    describe("when looking for common sequences of simple nodes in a path") {
        it("should find a sequence in a path of length 1") {
            val node1 = SimpleNode()
            val path = listOf(node1)

            assertThat(pathUtil.pathContainsSequence(path, listOf(node1), TestNodeComparator())).isTrue()
        }

        it("should not find a sequence that isn't in the path") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val path = listOf(node1)

            assertThat(pathUtil.pathContainsSequence(path, listOf(node2), TestNodeComparator())).isFalse()
        }

        it("should find a sequence at the start of a path") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()
            val node4 = SimpleNode()
            val node5 = SimpleNode()
            val node6 = SimpleNode()
            val path = listOf(node1, node2, node3, node4, node5, node6)

            assertThat(pathUtil.pathContainsSequence(path, listOf(node1, node2), TestNodeComparator())).isTrue()
        }

        it("should find a sequence in the middle of a path") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()
            val node4 = SimpleNode()
            val node5 = SimpleNode()
            val node6 = SimpleNode()
            val path = listOf(node1, node2, node3, node4, node5, node6)

            assertThat(pathUtil.pathContainsSequence(path, listOf(node3, node4, node5), TestNodeComparator())).isTrue()
        }

        it("should find a sequence at the end of a path") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()
            val node4 = SimpleNode()
            val node5 = SimpleNode()
            val node6 = SimpleNode()
            val path = listOf(node1, node2, node3, node4, node5, node6)

            assertThat(pathUtil.pathContainsSequence(path, listOf(node5, node6), TestNodeComparator())).isTrue()
        }

        it("should not find an out-of-order sequence that is not in a path") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()
            val node4 = SimpleNode()
            val node5 = SimpleNode()
            val node6 = SimpleNode()
            val path = listOf(node1, node2, node3, node4, node5, node6)

            assertThat(pathUtil.pathContainsSequence(path, listOf(node5, node4), TestNodeComparator())).isFalse()
        }

        it("should not find a non-consecutive sequence that is not in a path") {
            val node2 = SimpleNode()
            val node3 = SimpleNode()
            val node4 = SimpleNode()
            val node5 = SimpleNode()
            val path = listOf(node2, node3, node4, node5)

            assertThat(pathUtil.pathContainsSequence(path, listOf(node2, node4, node5), TestNodeComparator())).isFalse()
        }
    }
})
