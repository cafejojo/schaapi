package org.cafejojo.schaapi.usagegraphgenerator

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.common.Node
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.Unit
import soot.jimple.internal.JAssignStmt
import soot.jimple.internal.JInvokeStmt

private const val TEST_CLASSES_PACKAGE = "org.cafejojo.schaapi.usagegraphgenerator.testclasses"
private val testClassesClassPath = IntegrationTest::class.java.getResource("../../../../").toURI().path

internal class IntegrationTest : Spek({
    describe("the integration of different components of the library usage graph generation") {
        it("converts a class to a filtered cfg") {
            val cfg = generateLibraryUsageGraph(
                testClassesClassPath,
                "$TEST_CLASSES_PACKAGE.users.SimpleTest",
                "test"
            )

            assertThatStructureMatches(
                node<JAssignStmt>(
                    node<JInvokeStmt>(
                        node<JInvokeStmt>()
                    )
                ),
                cfg
            )
        }
    }
})

private fun assertThatStructureMatches(structure: Node, cfg: Node) {
    assertThat(cfg::class).isEqualTo(structure::class)
    assertThat(cfg.successors).hasSameSizeAs(structure.successors)
    structure.successors.forEachIndexed { index, structureSuccessor ->
        if (cfg is SootNode && structure is SootNode) assertThat(structure.unit).isInstanceOf(cfg.unit::class.java)
        if (structureSuccessor !is PreviousBranchNode)
            assertThatStructureMatches(structureSuccessor, cfg.successors[index])
    }
}

private class PreviousBranchNode(override val successors: MutableList<Node> = arrayListOf()) : Node

private inline fun <reified T : Unit> node(vararg successors: Node) = SootNode(mock<T>(), successors.toMutableList())
