package org.cafejojo.schaapi.miningpipeline.patterndetector.prefixspan

import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimpleNode
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xit
import soot.ByteType
import soot.Local
import soot.RefType
import soot.jimple.Jimple
import soot.jimple.internal.JEqExpr

class PatternDetectorJimpleNodeIntegrationTest : Spek({
    /**
     * Calculates how many sub-sequences a given sequence may have.
     */
    fun amountOfPossibleSubSequences(sequenceLength: Int): Int = (0..sequenceLength).sum()

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
     * Creates a local that has [type] as its name and type.
     *
     * @param type the type and name of the local to create
     * @return a local that has [type] as its name and type
     */
    fun createLocal(type: String) = Jimple.v().newLocal(type, RefType.v(type))

    /**
     * Creates a [JimpleNode] without any [soot.Value]s.
     */
    fun createJimpleNode(): JimpleNode {
        val local = Jimple.v().newLocal("local", ByteType.v())
        val condition = Jimple.v().newConditionExprBox(JEqExpr(local, local)).value
        val target = Jimple.v().newBreakpointStmt()

        return JimpleNode(Jimple.v().newIfStmt(condition, target))
    }

    /**
     * Creates a [JimpleNode] with two [soot.Value]s.
     *
     * @param leftVal the first local to use
     * @param rightVal the second local to use
     */
    fun createJimpleNode(leftVal: Local, rightVal: Local) = JimpleNode(Jimple.v().newAssignStmt(leftVal, rightVal))

    /**
     * Returns true iff the given paths have the same length and the contained nodes are equivalent.
     *
     * @param expected the expected path
     * @param actual the actual path
     */
    fun pathsAreEquivalent(expected: List<Node>, actual: List<Node>) =
        expected.size == actual.size && expected.zip(actual).all { it.first.equivTo(it.second) }

    /**
     * Returns true iff [expected] contains paths that are equivalent to those in [actual].
     *
     * @param expected the expected paths
     * @param actual the actual paths
     */
    fun containsEquivalentPaths(expected: List<List<Node>>, actual: List<List<Node>>) =
        actual.all { needle -> expected.any { candidate -> pathsAreEquivalent(needle, candidate) } }

    describe("when looking for common sequences in patterns of statements using the generalized soot comparator") {
        it("should find a pattern with multiple nodes which have different values with the same type") {
            val locals = Array(3, { createLocal(it.toString()) })

            val node1 = createJimpleNode(locals[0], locals[2])
            val node2 = createJimpleNode(locals[1], locals[1])
            val node3 = createJimpleNode(locals[2], locals[0])
            val node4 = createJimpleNode(locals[0], locals[2])
            val node5 = createJimpleNode(locals[1], locals[1])
            val node6 = createJimpleNode(locals[2], locals[0])
            val node7 = createJimpleNode()
            val node8 = createJimpleNode()
            val node9 = createJimpleNode()
            val node10 = createJimpleNode()

            val graphs = listOf(
                makeGraph(node1, node2, node3),
                makeGraph(node7, node8, node9, node10, node4, node5, node6)
            )

            val frequent = PatternDetector(2, 100, GeneralizedNodeComparator())
                .findPatterns(graphs)

            assertThat(containsEquivalentPaths(
                frequent,
                listOf(
                    listOf(node1, node2, node3)
                )
            ))
        }

        // TODO make test pass
        xit("should not store duplicate patterns") {
            val locals = Array(3, { createLocal(it.toString()) })

            val node1 = createJimpleNode(locals[0], locals[2])
            val node2 = createJimpleNode(locals[1], locals[1])
            val node3 = createJimpleNode(locals[2], locals[0])
            val node4 = createJimpleNode(locals[1], locals[0])
            val node5 = createJimpleNode(locals[0], locals[1])

            val node6 = createJimpleNode(locals[0], locals[2])
            val node7 = createJimpleNode(locals[1], locals[1])
            val node8 = createJimpleNode(locals[2], locals[0])
            val node9 = createJimpleNode(locals[1], locals[0])
            val node10 = createJimpleNode(locals[0], locals[1])

            val node11 = createJimpleNode()
            val node12 = createJimpleNode()

            val graphs = listOf(
                makeGraph(node1, node2, node3, node4, node5),
                makeGraph(node11, node12, node6, node7, node8, node9, node10)
            )

            val frequent = PatternDetector(2, 100, GeneralizedNodeComparator())
                .findPatterns(graphs)

            assertThat(frequent).hasSize(amountOfPossibleSubSequences(5))
        }

        it("should find a pattern with multiple nodes which have the same value") {
            val locals = Array(3, { createLocal(it.toString()) })

            val node1 = createJimpleNode(locals[1], locals[1])
            val node2 = createJimpleNode(locals[2], locals[0])
            val node3 = createJimpleNode(locals[0], locals[2])
            val node4 = createJimpleNode(locals[1], locals[0])

            val node5 = createJimpleNode(locals[1], locals[1])
            val node6 = createJimpleNode(locals[2], locals[0])
            val node7 = createJimpleNode(locals[0], locals[2])
            val node8 = createJimpleNode(locals[1], locals[0])

            val node9 = createJimpleNode()
            val node10 = createJimpleNode()

            val graphs = listOf(
                makeGraph(node1, node2, node3, node4),
                makeGraph(node9, node10, node5, node6, node7, node8)
            )

            val frequent = PatternDetector(2, 100, GeneralizedNodeComparator())
                .findPatterns(graphs)

            assertThat(containsEquivalentPaths(
                frequent,
                listOf(
                    listOf(node1, node2, node3, node4)
                )
            ))
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

            val frequent = PatternDetector(2, 100, GeneralizedNodeComparator())
                .findPatterns(graphs)

            assertThat(containsEquivalentPaths(
                frequent,
                listOf(
                    listOf(node1, node2, node3)
                )
            ))
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

            val frequent = PatternDetector(2, 100, GeneralizedNodeComparator())
                .findPatterns(graps)

            assertThat(frequent).isEmpty()
        }

        // TODO make test pass
        xit("should not find a pattern when multiple patterns use the same values") {
            val locals = Array(3, { createLocal(it.toString()) })

            val node1 = createJimpleNode(locals[1], locals[1])
            val node2 = createJimpleNode(locals[2], locals[0])
            val node3 = createJimpleNode(locals[0], locals[2])
            val node4 = createJimpleNode(locals[1], locals[0])

            val node5 = createJimpleNode(locals[0], locals[2])
            val node6 = createJimpleNode(locals[1], locals[1])
            val node7 = createJimpleNode(locals[2], locals[0])
            val node8 = createJimpleNode(locals[1], locals[0])

            val node9 = createJimpleNode()
            val node10 = createJimpleNode()

            val graphs = listOf(
                makeGraph(node4, node2, node3, node1),
                makeGraph(node9, node10, node5, node6, node7, node8)
            )

            val frequent = PatternDetector(2, 100, GeneralizedNodeComparator())
                .findPatterns(graphs)

            assertThat(frequent).hasSize(4)
        }
    }
})
