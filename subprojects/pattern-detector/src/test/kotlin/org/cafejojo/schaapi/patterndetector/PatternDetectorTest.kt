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
        it("Should find the entire pattern in one path") {
            val node1 = EntryNode(id = CustomNodeId(1))
            val node2 = StatementNode(id = CustomNodeId(2))
            val node3 = ExitNode(id = CustomNodeId(3))

            val path = listOf(node1, node2, node3)
            val paths = listOf(path)

            val frequent = PatternDetector(paths).frequentPatterns(1)

            assertThat(frequent).hasSize(6)
            assertThat(frequent).contains(path)
        }
    }
})
