package org.cafejojo.schaapi.models

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.util.UUID

internal class PathEnumeratorTest : Spek({
    /**
     * A node with an ID that is equivalent only to nodes with the same id.
     */
    class UniqueNode(
        override val successors: MutableList<Node> = mutableListOf(),
        private val id: UUID = UUID.randomUUID()
    ) : Node {
        override fun equivTo(other: Node?) = other is UniqueNode && this.id == other.id

        override fun equivHashCode() = id.hashCode()

        override fun copy() = UniqueNode(successors.toMutableList(), id)
    }

    /**
     * Returns true iff the given paths have the same length and the contained nodes are equivalent.
     *
     * @param expected the expected path
     * @param actual the actual path
     */
    fun pathsAreEquivalent(expected: List<Node>, actual: List<Node>) =
        expected.size == actual.size && expected.zip(actual).all { it.first.equivTo(it.second) }

    /**
     * Returns true iff all paths are equivalent.
     *
     * @param expected the expected paths
     * @param actual the actual paths
     */
    fun allPathsAreEquivalent(expected: List<List<Node>>, actual: List<List<Node>>) =
        expected.size == actual.size && expected.zip(actual).all { pathsAreEquivalent(it.first, it.second) }

    describe("enumerating all paths in a control flow graph") {
        it("finds a single path in a linear graph") {
            val node1 = UniqueNode()
            val node2 = UniqueNode()
            val node3 = UniqueNode()

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3))

            val paths = PathEnumerator(node1, 100).enumerate()

            assertThat(allPathsAreEquivalent(
                paths,
                listOf(
                    listOf(node1, node2, node3)
                )
            )).isTrue()
        }

        it("finds the paths of a single if-branch") {
            val node1 = UniqueNode()
            val node2 = UniqueNode()
            val node3 = UniqueNode()
            val node4 = UniqueNode()
            val node5 = UniqueNode()

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3, node4))
            node3.successors.addAll(listOf(node5))
            node4.successors.addAll(listOf(node5))

            val paths = PathEnumerator(node1, 100).enumerate()

            assertThat(allPathsAreEquivalent(
                paths,
                listOf(
                    listOf(node1, node2, node3, node5),
                    listOf(node1, node2, node4, node5)
                )
            )).isTrue()
        }

        it("finds the paths of a nested if-branch") {
            val node1 = UniqueNode()
            val node2 = UniqueNode()
            val node3 = UniqueNode()
            val node4 = UniqueNode()
            val node5 = UniqueNode()
            val node6 = UniqueNode()
            val node7 = UniqueNode()
            val node8 = UniqueNode()

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3, node4))
            node3.successors.addAll(listOf(node5, node6))
            node4.successors.addAll(listOf(node8))
            node5.successors.addAll(listOf(node7))
            node6.successors.addAll(listOf(node7))
            node7.successors.addAll(listOf(node8))

            val paths = PathEnumerator(node1, 100).enumerate()

            assertThat(allPathsAreEquivalent(
                paths,
                listOf(
                    listOf(node1, node2, node3, node5, node7, node8),
                    listOf(node1, node2, node3, node6, node7, node8),
                    listOf(node1, node2, node4, node8)
                )
            )).isTrue()
        }

        it("finds the simple path in a graph containing a cycle") {
            val node1 = UniqueNode()
            val node2 = UniqueNode()
            val node3 = UniqueNode()
            val node4 = UniqueNode()

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3))
            node3.successors.addAll(listOf(node4, node2))

            val paths = PathEnumerator(node1, 100).enumerate()

            assertThat(allPathsAreEquivalent(
                paths,
                listOf(
                    listOf(node1, node2, node3, node4),
                    listOf(node1, node2, node3, node2, node3, node4)
                )
            )).isTrue()
        }

        it("finds the simple path in a graph containing a nested cycle") {
            val node1 = UniqueNode()
            val node2 = UniqueNode()
            val node3 = UniqueNode()
            val node4 = UniqueNode()
            val node5 = UniqueNode()
            val node6 = UniqueNode()

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3))
            node3.successors.addAll(listOf(node4))
            node4.successors.addAll(listOf(node5, node3))
            node5.successors.addAll(listOf(node6, node2))

            val paths = PathEnumerator(node1, 100).enumerate()

            assertThat(allPathsAreEquivalent(
                paths,
                listOf(
                    listOf(node1, node2, node3, node4, node5, node6),
                    listOf(node1, node2, node3, node4, node5, node2, node3, node4, node5, node6),
                    listOf(node1, node2, node3, node4, node3, node4, node5, node6)
                )
            )).isTrue()
        }
    }

    describe("addition of exit nodes to the graph") {
        it("adds an exit node as successor to two leaves") {
            val node1 = UniqueNode()
            val node2 = UniqueNode()
            val node3 = UniqueNode()
            val node4 = UniqueNode()

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3, node4))

            val exitNode = node1.connectLeavesToExitNode()

            assertThat(node1.successors).doesNotContain(exitNode)
            assertThat(node2.successors).doesNotContain(exitNode)
            assertThat(node3.successors).contains(exitNode)
            assertThat(node4.successors).contains(exitNode)
        }

        it("throws an exception when faced with a tree without leaves") {
            val node1 = UniqueNode()
            val node2 = UniqueNode()
            val node3 = UniqueNode()

            node1.successors.add(node2)
            node2.successors.add(node3)
            node3.successors.add(node2)

            val exitNode = node1.connectLeavesToExitNode()
            assertThat(node3.successors).contains(exitNode)
        }
    }

    describe("removal of exit nodes from the graph") {
        it("removes an exit node from two successor lists") {
            val node1 = UniqueNode()
            val node2 = UniqueNode()
            val node3 = UniqueNode()

            node1.successors.addAll(listOf(node2, node3))

            node1.connectLeavesToExitNode()
            node1.removeExitNodes()

            assertThat(node1.successors).doesNotHaveAnyElementsOfTypes(ExitNode::class.java)
            assertThat(node2.successors).doesNotHaveAnyElementsOfTypes(ExitNode::class.java)
            assertThat(node3.successors).doesNotHaveAnyElementsOfTypes(ExitNode::class.java)
        }

        it("keeps the list intact if no exit nodes are present") {
            val node1 = UniqueNode()
            val node2 = UniqueNode()
            val node3 = UniqueNode()

            node1.successors.addAll(listOf(node2, node3))
            node1.removeExitNodes()

            assertThat(node1.iterator()).containsExactly(node1, node2, node3)
        }
    }

    describe("enumeration with maximum sequence length restriction") {
        it("should not find paths greater than the maximum length threshold") {
            val node1 = UniqueNode()
            val node2 = UniqueNode()
            val node3 = UniqueNode()
            val node4 = UniqueNode()
            val node5 = UniqueNode()

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3, node4))
            node3.successors.addAll(listOf(node5))

            val paths = PathEnumerator(node1, 3).enumerate()

            assertThat(allPathsAreEquivalent(
                paths,
                listOf(
                    listOf(node1, node2, node4)
                )
            )).isTrue()
        }
    }
})
