package org.cafejojo.schaapi.patterndetector

import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.usagegraphgenerator.BranchNode
import org.cafejojo.schaapi.usagegraphgenerator.CustomNodeId
import org.cafejojo.schaapi.usagegraphgenerator.EntryNode
import org.cafejojo.schaapi.usagegraphgenerator.ExitNode
import org.cafejojo.schaapi.usagegraphgenerator.StatementNode
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

internal class PathEnumeratorTest : Spek({
    describe("enumerating all paths in a control flow graph") {
        it("should find a single path in a linear graph") {
            val node1 = EntryNode(id = CustomNodeId(1))
            val node2 = StatementNode(id = CustomNodeId(2))
            val node3 = ExitNode(id = CustomNodeId(3))

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3))

            val paths = PathEnumerator(node1, node3).enumerate()
            assertThat(paths)
                .isEqualTo(
                    listOf(
                        listOf(node1, node2, node3)
                    )
                )
        }

        it("should find the paths of a single if-branch") {
            val node1 = EntryNode(id = CustomNodeId(1))
            val node2 = BranchNode(id = CustomNodeId(2))
            val node3 = StatementNode(id = CustomNodeId(3))
            val node4 = StatementNode(id = CustomNodeId(4))
            val node5 = ExitNode(id = CustomNodeId(5))

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3, node4))
            node3.successors.addAll(listOf(node5))
            node4.successors.addAll(listOf(node5))

            val node2False = BranchNode(id = CustomNodeId(2))
            val node2True = BranchNode(id = CustomNodeId(2))
            node2False.successors.addAll(listOf(node5, node4))
            node2True.successors.addAll(listOf(node3, node5))

            val paths = PathEnumerator(node1, node5).enumerate()

            assertThat(paths)
                .isEqualTo(
                    listOf(
                        listOf(node1, node2True, node3, node5),
                        listOf(node1, node2False, node4, node5)
                    )
                )
        }

        it("should find the paths of a nested if-branch") {
            val node1 = EntryNode(id = CustomNodeId(1))
            val node2 = BranchNode(id = CustomNodeId(2))
            val node3 = BranchNode(id = CustomNodeId(3))
            val node4 = StatementNode(id = CustomNodeId(4))
            val node5 = StatementNode(id = CustomNodeId(5))
            val node6 = StatementNode(id = CustomNodeId(6))
            val node7 = StatementNode(id = CustomNodeId(7))
            val node8 = ExitNode(id = CustomNodeId(8))

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3, node4))
            node3.successors.addAll(listOf(node5, node6))
            node4.successors.addAll(listOf(node8))
            node5.successors.addAll(listOf(node7))
            node6.successors.addAll(listOf(node7))
            node7.successors.addAll(listOf(node8))

            val node2False = BranchNode(id = CustomNodeId(2))
            val node2True = BranchNode(id = CustomNodeId(2))
            val node3False = BranchNode(id = CustomNodeId(3))
            val node3True = BranchNode(id = CustomNodeId(3))
            node2False.successors.addAll(listOf(node8, node4))
            node2True.successors.addAll(listOf(node3, node8))
            node3False.successors.addAll(listOf(node8, node6))
            node3True.successors.addAll(listOf(node5, node8))

            val paths = PathEnumerator(node1, node8).enumerate()

            assertThat(paths)
                .isEqualTo(
                    listOf(
                        listOf(node1, node2True, node3True, node5, node7, node8),
                        listOf(node1, node2True, node3False, node6, node7, node8),
                        listOf(node1, node2False, node4, node8)
                    )
                )
        }

        it("should find the simple path in a graph containing a cycle") {
            val node1 = EntryNode(id = CustomNodeId(1))
            val node2 = StatementNode(id = CustomNodeId(2))
            val node3 = BranchNode(id = CustomNodeId(3))
            val node4 = ExitNode(id = CustomNodeId(4))

            node1.successors.addAll(listOf(node2))
            node2.successors.addAll(listOf(node3))
            node3.successors.addAll(listOf(node4, node2))

            val node3False = BranchNode(id = CustomNodeId(3))
            val node3True = BranchNode(id = CustomNodeId(3))
            node3False.successors.addAll(listOf(node4, node2))
            node3True.successors.addAll(listOf(node3, node2))

            val paths = PathEnumerator(node1, node4).enumerate()

            assertThat(paths)
                .isEqualTo(
                    listOf(
                        listOf(node1, node2, node3False, node4)
                    )
                )
        }
    }
})
