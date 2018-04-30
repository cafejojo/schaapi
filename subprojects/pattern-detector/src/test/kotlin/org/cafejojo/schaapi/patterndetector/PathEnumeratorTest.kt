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

            val paths = enumeratePaths(node1, node3)
            assertThat(paths)
                .isEqualTo(
                    setOf(
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

            val node2WithoutFalse = BranchNode()
            val node2WithoutTrue = BranchNode()
            node2WithoutFalse.successors = listOf(node3, node5)
            node2WithoutTrue.successors = listOf(node5, node4)

//            val paths = enumeratePaths(node1, node5)
//            assertThat(paths)
//                .isEqualTo(setOf(
//                    listOf(node1, node2WithoutFalse, node3, node5),
//                    listOf(node1, node2WithoutTrue, node4, node5)
//                ))
        }
    }
})
