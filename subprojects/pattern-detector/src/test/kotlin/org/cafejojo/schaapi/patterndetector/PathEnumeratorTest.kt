package org.cafejojo.schaapi.patterndetector

import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.usagegraphgenerator.BranchNode
import org.cafejojo.schaapi.usagegraphgenerator.EntryNode
import org.cafejojo.schaapi.usagegraphgenerator.ExitNode
import org.cafejojo.schaapi.usagegraphgenerator.StatementNode
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

internal class PathEnumeratorTest : Spek({
    describe("The path enumerator") {
        it("should find a single path in a linear graph") {
            val node1 = EntryNode()
            val node2 = StatementNode()
            val node3 = ExitNode()

            node1.successors = listOf(node2)
            node2.successors = listOf(node3)

            val paths = PathEnumerator(node1, node3).enumerate()
            assertThat(paths)
                .isEqualTo(
                    listOf(
                        listOf(node1, node2, node3)
                    )
                )
        }

        it("should find the paths of a single if-branch") {
            val node1 = EntryNode()
            val node2 = BranchNode()
            val node3 = StatementNode()
            val node4 = StatementNode()
            val node5 = ExitNode()

            node1.successors = listOf(node2)
            node2.successors = listOf(node3, node4)
            node3.successors = listOf(node5)
            node4.successors = listOf(node5)

            val node2True = BranchNode()
            val node2False = BranchNode()
            node2True.successors = listOf(node3, node5)
            node2False.successors = listOf(node5, node4)

            val paths = PathEnumerator(node1, node5).enumerate()

            paths.forEach { list -> list.filter { it is BranchNode }.forEach { println(it.successors) } }
            println(node2True.successors)
            println(node2False.successors)

            assertThat(paths)
                .isEqualTo(
                    listOf(
                        listOf(node1, node2True, node3, node5),
                        listOf(node1, node2False, node4, node5)
                    )
                )
        }

        it("should find the paths of a nested if-branch") {
            val node1 = EntryNode()
            val node2 = BranchNode()
            val node3 = BranchNode()
            val node4 = StatementNode()
            val node5 = StatementNode()
            val node6 = StatementNode()
            val node7 = StatementNode()
            val node8 = ExitNode()

            node1.successors = listOf(node2)
            node2.successors = listOf(node3, node4)
            node3.successors = listOf(node5, node6)
            node4.successors = listOf(node8)
            node5.successors = listOf(node7)
            node6.successors = listOf(node7)
            node7.successors = listOf(node8)

            val node2True = BranchNode()
            val node2False = BranchNode()
            val node3True = BranchNode()
            val node3False = BranchNode()
            node2True.successors = listOf(node3, node8)
            node2False.successors = listOf(node8, node4)
            node3True.successors = listOf(node5, node8)
            node3False.successors = listOf(node8, node6)

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

        it("should find the paths of a simple loop") {
            val node1 = EntryNode()
            val node2 = StatementNode()
            val node3 = BranchNode()
            val node4 = ExitNode()

            node1.successors = listOf(node2)
            node2.successors = listOf(node3, node2)
            node3.successors = listOf(node4)

            val node3True = BranchNode()
            val node3False = BranchNode()
            node3True.successors = listOf(node3, node2)
            node3False.successors = listOf(node4, node2)

            val paths = PathEnumerator(node1, node4).enumerate()

            assertThat(paths)
                .isEqualTo(
                    listOf(
                        listOf(node1, node2, node3True, node2, node3False, node4),
                        listOf(node1, node2, node3False, node4)
                    )
                )
        }
    }
})
