package org.cafejojo.schaapi.common

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

internal class DfsIteratorTest : Spek({
    describe("iteration of a graph in DFS order") {
        it("handles a single-node graph") {
            val node1 = TestNode()

            val iterator = DfsIterator(node1)

            assertThat(iterator).containsExactly(node1)
        }

        it("handles a linear 2-node graph") {
            val node1 = TestNode()
            val node2 = TestNode()

            node1.successors.add(node2)

            val iterator = DfsIterator(node1)

            assertThat(iterator).containsExactly(node1, node2)
        }

        it("handles a split in a graph") {
            val node1 = TestNode()
            val node2 = TestNode()
            val node3 = TestNode()
            val node4 = TestNode()

            node1.successors.add(node2)
            node2.successors.addAll(listOf(node3, node4))

            val iterator = DfsIterator(node1)

            assertThat(iterator).containsExactly(node1, node2, node3, node4)
        }

        it("handles a split in a graph that is joined again afterwards") {
            val node1 = TestNode()
            val node2 = TestNode()
            val node3 = TestNode()
            val node4 = TestNode()
            val node5 = TestNode()
            val node6 = TestNode()

            node1.successors.add(node2)
            node2.successors.addAll(listOf(node3, node5))
            node3.successors.add(node4)
            node5.successors.add(node6)
            node6.successors.add(node4)

            val iterator = DfsIterator(node1)

            assertThat(iterator).containsExactly(node1, node2, node3, node4, node5, node6)
        }

        it("handles a double edge cycle") {
            val node1 = TestNode()
            val node2 = TestNode()
            val node3 = TestNode()
            val node4 = TestNode()
            val node5 = TestNode()

            node1.successors.add(node2)
            node2.successors.add(node3)
            node3.successors.addAll(listOf(node4, node2))
            node4.successors.add(node5)

            val iterator = DfsIterator(node1)

            assertThat(iterator).containsExactly(node1, node2, node3, node4, node5)
        }

        it("handles a 1-node cycle") {
            val node1 = TestNode()
            val node2 = TestNode()
            val node3 = TestNode()
            val node4 = TestNode()
            val node5 = TestNode()
            val node6 = TestNode()

            node1.successors.add(node2)
            node2.successors.add(node3)
            node3.successors.addAll(listOf(node4, node6))
            node4.successors.add(node5)
            node6.successors.add(node2)

            val iterator = DfsIterator(node1)

            assertThat(iterator).containsExactly(node1, node2, node3, node4, node5, node6)
        }
    }
})

private class TestNode(override val successors: MutableList<Node> = mutableListOf()) : Node
