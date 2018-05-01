package org.cafejojo.schaapi.usagegraphgenerator

import de.codesourcery.asm.controlflow.ControlFlowAnalyzer
import de.codesourcery.asm.controlflow.ControlFlowGraph
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode

internal class InstructionGrouperTest : Spek({
    describe("grouping of instructions to statements while remaining the control flow") {

        it("should remain the control flow") {
            val cfg = constructCFG("org.cafejojo.schaapi.usagegraphgenerator.testclasses.users.Test1")

            val scfg = InstructionGrouper(cfg).groupToStatements()

            assertThatStructureMatches(
                entryNode(
                    statementNode(
                        branchNode(
                            statementNode(
                                statementNode(
                                    statementNode(
                                        exitNode()
                                    )
                                )
                            ),
                            statementNode(
                                statementNode(
                                    statementNode(
                                        exitNode()
                                    )
                                )
                            )
                        )
                    )
                ),
                scfg
            )
        }
    }
})

private fun constructCFG(className: String): ControlFlowGraph {
    val classNode = ClassNode()
    ClassReader(className).accept(classNode, 0)

    val method = classNode.methods[1]

    return ControlFlowAnalyzer().analyze(className, method)
}

private fun assertThatStructureMatches(structure: Node, scfg: Node?) {
    assertThat(scfg?.javaClass).isEqualTo(structure.javaClass)
    assertThat(scfg?.successors).hasSameSizeAs(structure.successors)
    structure.successors.forEachIndexed { index, structureSuccessor ->
        assertThatStructureMatches(structureSuccessor, scfg?.successors?.get(index))
    }
}

private fun entryNode(vararg nodes: Node) = EntryNode(nodes.toCollection(ArrayList()))
private fun exitNode(vararg nodes: Node) = ExitNode(nodes.toCollection(ArrayList()))
private fun statementNode(vararg nodes: Node) = StatementNode(nodes.toCollection(ArrayList()))
private fun branchNode(vararg nodes: Node) = BranchNode(nodes.toCollection(ArrayList()))
