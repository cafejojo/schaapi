package org.cafejojo.schaapi.models

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

internal class PathEnumeratorTest : Spek({
    describe("enumerating all paths in a control flow graph") {
        it("finds a single path in a linear graph") {
            val node1 = SimpleNodeWithId(1)
            val node2 = SimpleNodeWithId(2)
            val node3 = SimpleNodeWithId(3)

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3))

            val paths = PathEnumerator(node1, 100).enumerate()
            assertThat(paths)
                .isEqualTo(
                    listOf(
                        listOf(SimpleNodeWithId(1), SimpleNodeWithId(2), SimpleNodeWithId(3))
                    )
                )
        }

        it("finds the paths of a single if-branch") {
            val node1 = SimpleNodeWithId(1)
            val node2 = SimpleNodeWithId(2)
            val node3 = SimpleNodeWithId(3)
            val node4 = SimpleNodeWithId(4)
            val node5 = SimpleNodeWithId(5)

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3, node4))
            node3.successors.addAll(listOf(node5))
            node4.successors.addAll(listOf(node5))

            val paths = PathEnumerator(node1, 100).enumerate()

            assertThat(paths)
                .isEqualTo(
                    listOf(
                        listOf(SimpleNodeWithId(1), SimpleNodeWithId(2), SimpleNodeWithId(3), SimpleNodeWithId(5)),
                        listOf(SimpleNodeWithId(1), SimpleNodeWithId(2), SimpleNodeWithId(4), SimpleNodeWithId(5))
                    )
                )
        }

        it("finds the paths of a nested if-branch") {
            val node1 = SimpleNodeWithId(1)
            val node2 = SimpleNodeWithId(2)
            val node3 = SimpleNodeWithId(3)
            val node4 = SimpleNodeWithId(4)
            val node5 = SimpleNodeWithId(5)
            val node6 = SimpleNodeWithId(6)
            val node7 = SimpleNodeWithId(7)
            val node8 = SimpleNodeWithId(8)

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3, node4))
            node3.successors.addAll(listOf(node5, node6))
            node4.successors.addAll(listOf(node8))
            node5.successors.addAll(listOf(node7))
            node6.successors.addAll(listOf(node7))
            node7.successors.addAll(listOf(node8))

            val paths = PathEnumerator(node1, 100).enumerate()

            assertThat(paths)
                .isEqualTo(
                    listOf(
                        listOf(
                            SimpleNodeWithId(1), SimpleNodeWithId(2), SimpleNodeWithId(3),
                            SimpleNodeWithId(5), SimpleNodeWithId(7), SimpleNodeWithId(8)
                        ),
                        listOf(
                            SimpleNodeWithId(1), SimpleNodeWithId(2), SimpleNodeWithId(3),
                            SimpleNodeWithId(6), SimpleNodeWithId(7), SimpleNodeWithId(8)
                        ),
                        listOf(
                            SimpleNodeWithId(1), SimpleNodeWithId(2), SimpleNodeWithId(4),
                            SimpleNodeWithId(8)
                        )
                    )
                )
        }

        it("finds the simple path in a graph containing a cycle") {
            val node1 = SimpleNodeWithId(1)
            val node2 = SimpleNodeWithId(2)
            val node3 = SimpleNodeWithId(3)
            val node4 = SimpleNodeWithId(4)

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3))
            node3.successors.addAll(listOf(node4, node2))

            val paths = PathEnumerator(node1, 100).enumerate()

            assertThat(paths)
                .isEqualTo(
                    listOf(
                        listOf(SimpleNodeWithId(1), SimpleNodeWithId(2), SimpleNodeWithId(3), SimpleNodeWithId(4))
                    )
                )
        }

        it("finds the simple path in a graph containing a nested cycle") {
            val node1 = SimpleNodeWithId(1)
            val node2 = SimpleNodeWithId(2)
            val node3 = SimpleNodeWithId(3)
            val node4 = SimpleNodeWithId(4)
            val node5 = SimpleNodeWithId(5)
            val node6 = SimpleNodeWithId(6)

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3))
            node3.successors.addAll(listOf(node4))
            node4.successors.addAll(listOf(node5, node3))
            node5.successors.addAll(listOf(node6, node2))

            val paths = PathEnumerator(node1, 100).enumerate()

            assertThat(paths)
                .isEqualTo(
                    listOf(
                        listOf(
                            SimpleNodeWithId(1), SimpleNodeWithId(2), SimpleNodeWithId(3),
                            SimpleNodeWithId(4), SimpleNodeWithId(5), SimpleNodeWithId(6)
                        )
                    )
                )
        }
    }

    describe("addition of exit nodes to the graph") {
        it("adds an exit node as successor to two leaves") {
            val node1 = SimpleNodeWithId(1)
            val node2 = SimpleNodeWithId(2)
            val node3 = SimpleNodeWithId(3)
            val node4 = SimpleNodeWithId(4)

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3, node4))

            val exitNode = node1.connectLeavesToExitNode()

            assertThat(node1.successors).doesNotContain(exitNode)
            assertThat(node2.successors).doesNotContain(exitNode)
            assertThat(node3.successors).contains(exitNode)
            assertThat(node4.successors).contains(exitNode)
        }

        it("throws an exception when faced with a tree without leaves") {
            val node1 = SimpleNodeWithId(1)
            val node2 = SimpleNodeWithId(2)
            val node3 = SimpleNodeWithId(3)

            node1.successors.add(node2)
            node2.successors.add(node3)
            node3.successors.add(node2)

            val exitNode = node1.connectLeavesToExitNode()
            assertThat(node3.successors).contains(exitNode)
        }
    }

    describe("removal of exit nodes from the graph") {
        it("removes an exit node from two successor lists") {
            val node1 = SimpleNodeWithId(1)
            val node2 = SimpleNodeWithId(2)
            val node3 = SimpleNodeWithId(3)

            node1.successors.addAll(listOf(node2, node3))

            node1.connectLeavesToExitNode()
            node1.removeExitNodes()

            assertThat(node1.successors).doesNotHaveAnyElementsOfTypes(ExitNode::class.java)
            assertThat(node2.successors).doesNotHaveAnyElementsOfTypes(ExitNode::class.java)
            assertThat(node3.successors).doesNotHaveAnyElementsOfTypes(ExitNode::class.java)
        }

        it("keeps the list intact if no exit nodes are present") {
            val node1 = SimpleNodeWithId(1)
            val node2 = SimpleNodeWithId(2)
            val node3 = SimpleNodeWithId(3)

            node1.successors.addAll(listOf(node2, node3))
            node1.removeExitNodes()

            assertThat(node1.iterator()).containsExactly(SimpleNodeWithId(1), SimpleNodeWithId(2), SimpleNodeWithId(3))
        }
    }

    describe("enumeration with maximum sequence length restriction") {
        it("should not find paths greater than the maximum length threshold") {
            val node1 = SimpleNodeWithId(1)
            val node2 = SimpleNodeWithId(2)
            val node3 = SimpleNodeWithId(3)
            val node4 = SimpleNodeWithId(4)
            val node5 = SimpleNodeWithId(5)

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3, node4))
            node3.successors.addAll(listOf(node5))

            val paths = PathEnumerator(node1, 3).enumerate()

            assertThat(paths)
                .isEqualTo(
                    listOf(
                        listOf(SimpleNodeWithId(1), SimpleNodeWithId(2), SimpleNodeWithId(4))
                    )
                )
        }
    }

    describe("duplication of node instances for output paths") {
        it("does not use original instances for path lists") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()

            node1.successors.addAll(listOf(node2))

            val paths = PathEnumerator(node1, 100).enumerate()

            assertThat(paths).hasSize(1)
            assertThat(paths.first())
                .doesNotContainAnyElementsOf(listOf(node1, node2))
        }
    }
})

private class SimpleNodeWithId(
    val id: Int,
    override val successors: MutableList<Node> = mutableListOf()
) : SimpleNode(successors) {
    override fun equivTo(other: Node?) = other is SimpleNodeWithId && this.id == other.id

    override fun equivHashCode() = id

    override fun copy() = SimpleNodeWithId(id, successors.toMutableList())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SimpleNodeWithId

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }
}
