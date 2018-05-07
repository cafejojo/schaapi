package org.cafejojo.schaapi.patterndetector

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

internal class PathEnumeratorTest : Spek({
    describe("enumerating all paths in a control flow graph") {
        it("finds a single path in a linear graph") {
            val node1 = TestNode()
            val node2 = TestNode()
            val node3 = TestNode()

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3))

            val paths = PathEnumerator(node1).enumerate()
            assertThat(paths)
                .isEqualTo(
                    listOf(
                        listOf(node1, node2, node3)
                    )
                )
        }

        it("finds the paths of a single if-branch") {
            val node1 = TestNode()
            val node2 = TestNode()
            val node3 = TestNode()
            val node4 = TestNode()
            val node5 = TestNode()

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3, node4))
            node3.successors.addAll(listOf(node5))
            node4.successors.addAll(listOf(node5))

            val paths = PathEnumerator(node1).enumerate()

            assertThat(paths)
                .isEqualTo(
                    listOf(
                        listOf(node1, node2, node3, node5),
                        listOf(node1, node2, node4, node5)
                    )
                )
        }

        it("finds the paths of a nested if-branch") {
            val node1 = TestNode()
            val node2 = TestNode()
            val node3 = TestNode()
            val node4 = TestNode()
            val node5 = TestNode()
            val node6 = TestNode()
            val node7 = TestNode()
            val node8 = TestNode()

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3, node4))
            node3.successors.addAll(listOf(node5, node6))
            node4.successors.addAll(listOf(node8))
            node5.successors.addAll(listOf(node7))
            node6.successors.addAll(listOf(node7))
            node7.successors.addAll(listOf(node8))

            val paths = PathEnumerator(node1).enumerate()

            assertThat(paths)
                .isEqualTo(
                    listOf(
                        listOf(node1, node2, node3, node5, node7, node8),
                        listOf(node1, node2, node3, node6, node7, node8),
                        listOf(node1, node2, node4, node8)
                    )
                )
        }

        it("finds the simple path in a graph containing a cycle") {
            val node1 = TestNode()
            val node2 = TestNode()
            val node3 = TestNode()
            val node4 = TestNode()

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3))
            node3.successors.addAll(listOf(node4, node2))

            val paths = PathEnumerator(node1).enumerate()

            assertThat(paths)
                .isEqualTo(
                    listOf(
                        listOf(node1, node2, node3, node4),
                        listOf(node1, node2, node3, node2, node3, node4)
                    )
                )
        }

        it("finds the simple path in a graph containing a nested cycle") {
            val node1 = TestNode()
            val node2 = TestNode()
            val node3 = TestNode()
            val node4 = TestNode()
            val node5 = TestNode()
            val node6 = TestNode()

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3))
            node3.successors.addAll(listOf(node4))
            node4.successors.addAll(listOf(node5, node3))
            node5.successors.addAll(listOf(node6, node2))

            val paths = PathEnumerator(node1).enumerate()

            assertThat(paths)
                .isEqualTo(
                    listOf(
                        listOf(node1, node2, node3, node4, node5, node6),
                        listOf(node1, node2, node3, node4, node5, node2, node3, node4, node5, node6),
                        listOf(node1, node2, node3, node4, node3, node4, node5, node6)
                    )
                )
        }
    }

    describe("addition of exit nodes to the graph") {
        it("adds an exit node as successor to two leaves") {
            val node1 = TestNode()
            val node2 = TestNode()
            val node3 = TestNode()
            val node4 = TestNode()

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3, node4))

            val exitNode = node1.connectLeavesToExitNode()

            assertThat(node1.successors).doesNotContain(exitNode)
            assertThat(node2.successors).doesNotContain(exitNode)
            assertThat(node3.successors).contains(exitNode)
            assertThat(node4.successors).contains(exitNode)
        }

        it("throws an an exception when faced with a tree without leaves") {
            val node1 = TestNode()
            val node2 = TestNode()
            val node3 = TestNode()

            node1.successors.add(node2)
            node2.successors.add(node3)
            node3.successors.add(node2)

            assertThatThrownBy {
                node1.connectLeavesToExitNode()
            }.isInstanceOf(IllegalStateException::class.java)
        }
    }

    describe("removal of exit nodes from the graph") {
        it("removes an exit node from two successor lists") {
            val node1 = TestNode()
            val node2 = TestNode()
            val node3 = TestNode()

            node1.successors.addAll(listOf(node2, node3))

            node1.connectLeavesToExitNode()
            node1.removeExitNodes()

            assertThat(node1.successors).doesNotHaveAnyElementsOfTypes(ExitNode::class.java)
            assertThat(node2.successors).doesNotHaveAnyElementsOfTypes(ExitNode::class.java)
            assertThat(node3.successors).doesNotHaveAnyElementsOfTypes(ExitNode::class.java)
        }

        it("keeps the list intact if no exit nodes are present") {
            val node1 = TestNode()
            val node2 = TestNode()
            val node3 = TestNode()

            node1.successors.addAll(listOf(node2, node3))
            node1.removeExitNodes()

            assertThat(node1.iterator()).containsExactly(node1, node2, node3)
        }
    }
})
