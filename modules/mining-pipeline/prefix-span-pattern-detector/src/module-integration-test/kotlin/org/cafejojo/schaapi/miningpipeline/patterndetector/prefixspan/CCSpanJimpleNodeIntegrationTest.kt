package org.cafejojo.schaapi.miningpipeline.patterndetector.prefixspan

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimpleNode
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xit
import soot.jimple.DefinitionStmt
import soot.jimple.IfStmt

internal object CCSpanJimpleNodeIntegrationTest : Spek({
    /**
     * Calculates how many sub-sequences a given sequence may have.
     */
    fun amountOfPossibleSubSequences(sequenceLength: Int): Int = (0..sequenceLength).sum()

    /**
     * Creates a [JimpleNode] without any [soot.Value]s.
     */
    fun createJimpleNode(): JimpleNode {
        val condition = mockUniqueValue()
        return JimpleNode(mock<IfStmt> { on { it.condition } doReturn condition })
    }

    /**
     * Create a graph where each node is the successor of the predecessor.
     */
    fun makeGraph(vararg nodes: JimpleNode): JimpleNode {
        nodes.forEachIndexed { index, node ->
            node.successors.addAll(nodes.getOrNull(index + 1)?.toMutableList() ?: emptyList())
        }
        return nodes.first()
    }

    /**
     * Creates a [JimpleNode] with two [soot.Value]s.
     *
     * @param leftType the [soot.Value.getType] of the first [soot.Value]
     * @param rightType the [soot.Value.getType] of the second [soot.Value]
     */
    fun createJimpleNode(leftType: String, rightType: String): JimpleNode {
        val leftOp = mockValue(leftType)
        val rightOp = mockValue(rightType)

        return JimpleNode(mock<DefinitionStmt> {
            on { it.leftOp } doReturn leftOp
            on { it.rightOp } doReturn rightOp
        })
    }

    describe("when looking for common sequences in patterns of statements using the generalized soot comparator") {
        it("should find a pattern with multiple nodes which have different values with the same type") {
            val node1 = createJimpleNode("A", "C")
            val node2 = createJimpleNode("B", "B")
            val node3 = createJimpleNode("C", "A")
            val node4 = createJimpleNode("A", "C")
            val node5 = createJimpleNode("B", "B")
            val node6 = createJimpleNode("C", "A")
            val node7 = createJimpleNode()
            val node8 = createJimpleNode()
            val node9 = createJimpleNode()
            val node10 = createJimpleNode()

            val graphs = listOf(
                makeGraph(node1, node2, node3),
                makeGraph(node7, node8, node9, node10, node4, node5, node6)
            )

            val frequent = PrefixSpanPatternDetector(2, 100, GeneralizedNodeComparator())
                .findPatterns(graphs)

            assertThat(frequent).contains(listOf(node1, node2, node3))
        }

        // TODO make test pass
        xit("should not store duplicate patterns") {
            val node1 = createJimpleNode("A", "C")
            val node2 = createJimpleNode("B", "B")
            val node3 = createJimpleNode("C", "A")
            val node4 = createJimpleNode("B", "A")
            val node5 = createJimpleNode("A", "B")

            val node6 = createJimpleNode("A", "C")
            val node7 = createJimpleNode("B", "B")
            val node8 = createJimpleNode("C", "A")
            val node9 = createJimpleNode("B", "A")
            val node10 = createJimpleNode("A", "B")

            val node11 = createJimpleNode()
            val node12 = createJimpleNode()

            val graphs = listOf(
                makeGraph(node1, node2, node3, node4, node5),
                makeGraph(node11, node12, node6, node7, node8, node9, node10)
            )

            val frequent = PrefixSpanPatternDetector(2, 100, GeneralizedNodeComparator())
                .findPatterns(graphs)

            assertThat(frequent).hasSize(amountOfPossibleSubSequences(5))
        }

        it("should find a pattern with multiple nodes which have the same value") {
            val node1 = createJimpleNode("B", "B")
            val node2 = createJimpleNode("C", "A")
            val node3 = createJimpleNode("A", "C")
            val node4 = createJimpleNode("B", "A")

            val node5 = createJimpleNode("B", "B")
            val node6 = createJimpleNode("C", "A")
            val node7 = createJimpleNode("A", "C")
            val node8 = createJimpleNode("B", "A")

            val node9 = createJimpleNode()
            val node10 = createJimpleNode()

            val graphs = listOf(
                makeGraph(node1, node2, node3, node4),
                makeGraph(node9, node10, node5, node6, node7, node8)
            )

            val frequent = PrefixSpanPatternDetector(2, 100, GeneralizedNodeComparator())
                .findPatterns(graphs)

            assertThat(frequent).contains(listOf(node1, node2, node3, node4))
        }

        it("should find a pattern when nodes don't have the same value but are the same node") {
            val node1 = createJimpleNode()
            val node2 = createJimpleNode()
            val node3 = createJimpleNode()
            val node7 = createJimpleNode()
            val node8 = createJimpleNode()
            val node9 = createJimpleNode()
            val node10 = createJimpleNode()

            val graphs = listOf(
                makeGraph(node1, node2, node3),
                makeGraph(node7, node8, node9, node10, node1, node2, node3)
            )

            val frequent = PrefixSpanPatternDetector(2, 100, GeneralizedNodeComparator())
                .findPatterns(graphs)

            assertThat(frequent).contains(listOf(node1, node2, node3))
        }

        it("should not find a pattern when there are only unique nodes") {
            val node1 = createJimpleNode()
            val node2 = createJimpleNode()
            val node3 = createJimpleNode()
            val node4 = createJimpleNode()
            val node5 = createJimpleNode()
            val node6 = createJimpleNode()
            val node7 = createJimpleNode()
            val node8 = createJimpleNode()
            val node9 = createJimpleNode()
            val node10 = createJimpleNode()

            val graps = listOf(
                makeGraph(node1, node2, node3),
                makeGraph(node7, node8, node9, node10, node4, node5, node6)
            )

            val frequent = PrefixSpanPatternDetector(2, 100, GeneralizedNodeComparator())
                .findPatterns(graps)

            assertThat(frequent).isEmpty()
        }

        // TODO make test pass
        xit("should not find a pattern when multiple patterns use the same values") {
            val node1 = createJimpleNode("B", "B")
            val node2 = createJimpleNode("C", "A")
            val node3 = createJimpleNode("A", "C")
            val node4 = createJimpleNode("B", "A")

            val node5 = createJimpleNode("A", "C")
            val node6 = createJimpleNode("B", "B")
            val node7 = createJimpleNode("C", "A")
            val node8 = createJimpleNode("B", "A")

            val node9 = createJimpleNode()
            val node10 = createJimpleNode()

            val graphs = listOf(
                makeGraph(node4, node2, node3, node1),
                makeGraph(node9, node10, node5, node6, node7, node8)
            )

            val frequent = PrefixSpanPatternDetector(2, 100, GeneralizedNodeComparator())
                .findPatterns(graphs)

            assertThat(frequent).hasSize(4)
        }
    }
})
