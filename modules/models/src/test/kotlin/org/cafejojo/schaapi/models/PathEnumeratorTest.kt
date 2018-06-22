package org.cafejojo.schaapi.models

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

internal class PathEnumeratorTest : Spek({
    describe("enumerating all paths in a control flow graph") {
        it("finds a single path in a linear graph") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3))

            val paths = SimplePathEnumerator(node1, 100).enumerate()
            assertThat(paths)
                .isEqualTo(
                    listOf(
                        listOf(node1, node2, node3)
                    )
                )
        }

        it("finds the paths of a single if-branch") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()
            val node4 = SimpleNode()
            val node5 = SimpleNode()

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3, node4))
            node3.successors.addAll(listOf(node5))
            node4.successors.addAll(listOf(node5))

            val paths = SimplePathEnumerator(node1, 100).enumerate()

            assertThat(paths)
                .isEqualTo(
                    listOf(
                        listOf(node1, node2, node3, node5),
                        listOf(node1, node2, node4, node5)
                    )
                )
        }

        it("finds the paths of a nested if-branch") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()
            val node4 = SimpleNode()
            val node5 = SimpleNode()
            val node6 = SimpleNode()
            val node7 = SimpleNode()
            val node8 = SimpleNode()

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3, node4))
            node3.successors.addAll(listOf(node5, node6))
            node4.successors.addAll(listOf(node8))
            node5.successors.addAll(listOf(node7))
            node6.successors.addAll(listOf(node7))
            node7.successors.addAll(listOf(node8))

            val paths = SimplePathEnumerator(node1, 100).enumerate()

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
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()
            val node4 = SimpleNode()

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3))
            node3.successors.addAll(listOf(node4, node2))

            val paths = SimplePathEnumerator(node1, 100).enumerate()

            assertThat(paths)
                .isEqualTo(
                    listOf(
                        listOf(node1, node2, node3, node4)
                    )
                )
        }

        it("finds the simple path in a graph containing a nested cycle") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()
            val node4 = SimpleNode()
            val node5 = SimpleNode()
            val node6 = SimpleNode()

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3))
            node3.successors.addAll(listOf(node4))
            node4.successors.addAll(listOf(node5, node3))
            node5.successors.addAll(listOf(node6, node2))

            val paths = SimplePathEnumerator(node1, 100).enumerate()

            assertThat(paths)
                .isEqualTo(
                    listOf(
                        listOf(node1, node2, node3, node4, node5, node6)
                    )
                )
        }
    }

    describe("addition of exit nodes to the graph") {
        it("adds an exit node as successor to two leaves") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()
            val node4 = SimpleNode()

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3, node4))

            val exitNode = ExitNode()
            node1.connectLeavesTo(exitNode)

            assertThat(node1.successors).doesNotContain(exitNode)
            assertThat(node2.successors).doesNotContain(exitNode)
            assertThat(node3.successors).contains(exitNode)
            assertThat(node4.successors).contains(exitNode)
        }

        it("throws an exception when faced with a tree without leaves") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()

            node1.successors.add(node2)
            node2.successors.add(node3)
            node3.successors.add(node2)

            val exitNode = ExitNode()
            node1.connectLeavesTo(exitNode)
            assertThat(node3.successors).contains(exitNode)
        }
    }

    describe("removal of exit nodes from the graph") {
        it("removes an exit node from two successor lists") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()

            node1.successors.addAll(listOf(node2, node3))

            val exitNode = ExitNode()
            node1.connectLeavesTo(exitNode)
            node1.removeExitNodes()

            assertThat(node1.successors).doesNotContain(exitNode)
            assertThat(node2.successors).doesNotContain(exitNode)
            assertThat(node3.successors).doesNotContain(exitNode)
        }

        it("keeps the list intact if no exit nodes are present") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()

            node1.successors.addAll(listOf(node2, node3))
            node1.removeExitNodes()

            assertThat(node1.iterator()).containsExactly(node1, node2, node3)
        }
    }

    describe("enumeration with maximum sequence length restriction") {
        it("should not find paths greater than the maximum length threshold") {
            val node1 = SimpleNode()
            val node2 = SimpleNode()
            val node3 = SimpleNode()
            val node4 = SimpleNode()
            val node5 = SimpleNode()

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3, node4))
            node3.successors.addAll(listOf(node5))

            val paths = SimplePathEnumerator(node1, 3).enumerate()

            assertThat(paths)
                .isEqualTo(
                    listOf(
                        listOf(node1, node2, node4)
                    )
                )
        }
    }
})
