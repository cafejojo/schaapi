package org.cafejojo.schaapi.patterndetector

import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.usagegraphgenerator.CustomNodeId
import org.cafejojo.schaapi.usagegraphgenerator.EntryNode
import org.cafejojo.schaapi.usagegraphgenerator.ExitNode
import org.cafejojo.schaapi.usagegraphgenerator.StatementNode
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

internal class PatternDetectorTest : Spek({
    describe("detecting patterns in a set of paths") {
        it("should find the entire pattern in one path") {
            val node1 = EntryNode(id = CustomNodeId(1))
            val node2 = StatementNode(id = CustomNodeId(2))
            val node3 = ExitNode(id = CustomNodeId(3))

            val path = listOf(node1, node2, node3)

            val paths = listOf(path)
            val frequent = PatternDetector(paths).frequentPatterns(1)

            assertThat(frequent).hasSize(6)
            assertThat(frequent).contains(path)
        }

        it("should not find a pattern in a set of random nodes with support 2") {
            val node1 = EntryNode(id = CustomNodeId(1))
            val node2 = StatementNode(id = CustomNodeId(2))
            val node3 = ExitNode(id = CustomNodeId(3))
            val node4 = EntryNode(id = CustomNodeId(4))
            val node5 = StatementNode(id = CustomNodeId(5))
            val node6 = ExitNode(id = CustomNodeId(6))
            val node7 = EntryNode(id = CustomNodeId(7))
            val node8 = StatementNode(id = CustomNodeId(8))
            val node9 = StatementNode(id = CustomNodeId(9))
            val node10 = ExitNode(id = CustomNodeId(10))

            val path1 = listOf(node1, node2, node3)
            val path2 = listOf(node4, node5, node6)
            val path3 = listOf(node7, node8, node9, node10)

            val paths = listOf(path1, path2, path3)
            val frequent = PatternDetector(paths).frequentPatterns(2)

            assertThat(frequent).hasSize(0)
        }

        it("should find a pattern that occurs twice in the same path") {
            val node1 = EntryNode(id = CustomNodeId(1))
            val node2 = StatementNode(id = CustomNodeId(4))
            val node3 = ExitNode(id = CustomNodeId(7))

            val path = listOf(node1, node2, node3, node1, node2, node3)

            val paths = listOf(path)
            val frequent = PatternDetector(paths).frequentPatterns(2)

            assertThat(frequent).hasSize(6)
            assertThat(frequent).contains(listOf(node1, node2, node3))
        }

        it("should find a pattern that occurs twice in two different paths") {
            val node1 = EntryNode(id = CustomNodeId(1))
            val node2 = StatementNode(id = CustomNodeId(2))
            val node3 = ExitNode(id = CustomNodeId(3))
            val node4 = EntryNode(id = CustomNodeId(4))
            val node5 = StatementNode(id = CustomNodeId(5))
            val node6 = ExitNode(id = CustomNodeId(6))
            val node7 = EntryNode(id = CustomNodeId(7))
            val node8 = StatementNode(id = CustomNodeId(8))
            val node9 = StatementNode(id = CustomNodeId(9))
            val node10 = ExitNode(id = CustomNodeId(10))

            val path1 = listOf(node1, node2, node3)
            val path2 = listOf(node4, node5, node6)
            val path3 = listOf(node7, node8, node9, node10, node1, node2, node3)

            val paths = listOf(path1, path2, path3)
            val frequent = PatternDetector(paths).frequentPatterns(2)

            assertThat(frequent).hasSize(6)
            assertThat(frequent).contains(path1)
        }
    }
})
