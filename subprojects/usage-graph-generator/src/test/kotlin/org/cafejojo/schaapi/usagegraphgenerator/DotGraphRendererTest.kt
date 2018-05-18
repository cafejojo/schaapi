package org.cafejojo.schaapi.usagegraphgenerator

import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.common.Node
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

internal class DotGraphRendererTest : Spek({
    describe("rendering of statement control flow graphs to dot graph files") {
        it("renders as simple graph") {
            val exitNode = TestNode(id = 5)

            val cfg = TestNode(
                id = 1,
                successors = arrayListOf(
                    TestNode(
                        id = 2,
                        successors = arrayListOf(
                            TestNode(
                                id = 3,
                                successors = arrayListOf(
                                    exitNode
                                )
                            ),
                            TestNode(
                                id = 4,
                                successors = arrayListOf(
                                    exitNode
                                )
                            )
                        )
                    )
                )
            )
            val result = DotGraphRenderer("graph-name", cfg).render()

            assertThat(result).isEqualTo(
                """
                    digraph "graph-name()" {
                        "1" [shape=ellipse, label="node-number-1"]
                        "1" -> "2"
                        "2" [shape=ellipse, label="node-number-2"]
                        "2" -> "3"
                        "3" [shape=ellipse, label="node-number-3"]
                        "3" -> "5"
                        "5" [shape=ellipse, label="node-number-5"]
                        "2" -> "4"
                        "4" [shape=ellipse, label="node-number-4"]
                        "4" -> "5"
                    }
                """.trimIndent()
            )
        }
    }
})

private class TestNode(override val successors: MutableList<Node> = mutableListOf(), val id: Int) : Node {
    override fun hashCode() = id
    override fun equals(other: Any?) = when {
        this === other -> true
        other !is TestNode -> false
        id != other.id -> false
        else -> true
    }

    override fun toString() = "node-number-$id"
}
