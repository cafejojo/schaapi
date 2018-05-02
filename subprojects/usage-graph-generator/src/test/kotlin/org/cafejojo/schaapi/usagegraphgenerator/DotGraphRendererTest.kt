package org.cafejojo.schaapi.usagegraphgenerator

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

internal class DotGraphRendererTest : Spek({
    describe("rendering of statement control flow graphs to dot graph files") {
        it("renders as simple graph") {
            val exitNode = ExitNode(id = CustomNodeId(5))

            val cfg = EntryNode(
                id = CustomNodeId(1),
                successors = arrayListOf(
                    BranchNode(
                        id = CustomNodeId(2),
                        successors = arrayListOf(
                            StatementNode(
                                id = CustomNodeId(3),
                                successors = arrayListOf(
                                    exitNode
                                )
                            ),
                            StatementNode(
                                id = CustomNodeId(4),
                                successors = arrayListOf(
                                    exitNode
                                )
                            )
                        )
                    )
                )
            )
            val result = DotGraphRenderer("graph-name", cfg).render().result.toString()

            assertThat(result).isEqualTo(
                """
                    digraph "graph-name()" {
                        "1" [shape=ellipse, label=method_entry]
                        "1" -> "2"
                        "2" [shape=box, label=branch]
                        "2" -> "3"
                        "3" [shape=box, label=statement]
                        "3" -> "5"
                        "5" [shape=ellipse, label=method_exit]
                        "2" -> "4"
                        "4" [shape=box, label=statement]
                        "4" -> "5"
                    }
                """.trimIndent()
            )
        }
    }
})
