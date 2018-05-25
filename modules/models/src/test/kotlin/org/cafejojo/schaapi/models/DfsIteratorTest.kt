package org.cafejojo.schaapi.models

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

internal class DfsIteratorTest : Spek({
    describe("iteration of a graph in DFS order") {
        it("handles a single-node graph") {
            val node1 = SimpleNode()

            val iterator = DfsIterator(node1)

            assertThat(iterator).containsExactly(node1)
        }

        it("handles a linear 2-node graph") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()

            node1.successors.add(node2)

            val iterator = DfsIterator(node1)

            assertThat(iterator).containsExactly(node1, node2)
        }

        it("handles a split in a graph") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()
            val node4 = SimpleNode()

            node1.successors.add(node2)
            node2.successors.addAll(listOf(node3, node4))

            val iterator = DfsIterator(node1)

            assertThat(iterator).containsExactly(node1, node2, node3, node4)
        }

        it("handles a split in a graph that is joined again afterwards") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()
            val node4 = SimpleNode()
            val node5 = SimpleNode()
            val node6 = SimpleNode()

            node1.successors.add(node2)
            node2.successors.addAll(listOf(node3, node5))
            node3.successors.add(node4)
            node5.successors.add(node6)
            node6.successors.add(node4)

            val iterator = DfsIterator(node1)

            assertThat(iterator).containsExactly(node1, node2, node3, node4, node5, node6)
        }

        it("handles a double edge cycle") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()
            val node4 = SimpleNode()
            val node5 = SimpleNode()

            node1.successors.add(node2)
            node2.successors.add(node3)
            node3.successors.addAll(listOf(node4, node2))
            node4.successors.add(node5)

            val iterator = DfsIterator(node1)

            assertThat(iterator).containsExactly(node1, node2, node3, node4, node5)
        }

        it("handles a 1-node cycle") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()
            val node4 = SimpleNode()
            val node5 = SimpleNode()
            val node6 = SimpleNode()

            node1.successors.add(node2)
            node2.successors.add(node3)
            node3.successors.addAll(listOf(node4, node6))
            node4.successors.add(node5)
            node6.successors.add(node2)

            val iterator = DfsIterator(node1)

            assertThat(iterator).containsExactly(node1, node2, node3, node4, node5, node6)
        }

        it("handles reflexive edges") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()

            node1.successors.addAll(listOf(node1, node2))
            node2.successors.addAll(listOf(node2, node3))

            val iterator = DfsIterator(node1)

            assertThat(iterator).containsExactly(node1, node2, node3)
        }
    }
})
