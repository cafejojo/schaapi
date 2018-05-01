package org.cafejojo.schaapi.usagegraphgenerator

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

internal class DotGraphRendererTest : Spek({
    describe("rendering of statement control flow graphs to dot graph files") {
        it("renders as simple graph") {
            val cfg = EntryNode(
                id = CustomNodeId(1),
                successors = arrayListOf(
                    BranchNode(
                        id = CustomNodeId(2),
                        successors = arrayListOf(
                            StatementNode(
                                id = CustomNodeId(3),
                                successors = arrayListOf(
                                    ExitNode(
                                        id = CustomNodeId(4)
                                    )
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
                        "3" -> "4"
                        "4" [shape=ellipse, label=method_exit]
                    }
                """.trimIndent()
            )
        }
    }
})
