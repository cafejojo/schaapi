package org.cafejojo.schaapi.usagegraphgenerator

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.common.Node
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.Unit
import soot.jimple.ReturnStmt
import soot.jimple.internal.JAssignStmt
import soot.jimple.internal.JGotoStmt
import soot.jimple.internal.JIfStmt
import soot.jimple.internal.JInvokeStmt
import soot.jimple.internal.JReturnStmt

private const val TEST_CLASSES_PACKAGE = "org.cafejojo.schaapi.usagegraphgenerator.testclasses"
private val testClassesClassPath = IntegrationTest::class.java.getResource("../../../../").toURI().path

internal class IntegrationTest : Spek({
    describe("the integration of different components of the library usage graph generation") {
        it("converts a simple class to a filtered cfg") {
            val cfg = generateLibraryUsageGraph(
                libraryProject,
                TestProject(classpath = testClassesClassPath),
                "$TEST_CLASSES_PACKAGE.users.SimpleTest",
                "test"
            )

            assertThatStructureMatches(
                node<JAssignStmt>(
                    node<JInvokeStmt>(
                        node<JAssignStmt>(
                            node<JInvokeStmt>(
                                node<ReturnStmt>()
                            )
                        )
                    )
                ),
                cfg
            )
        }

        it("converts a class containing an if with a library usage in the false-branch to a filtered cfg") {
            val cfg = generateLibraryUsageGraph(
                libraryProject,
                TestProject(classpath = testClassesClassPath),
                "$TEST_CLASSES_PACKAGE.users.IfFalseUseTest",
                "test"
            )

            assertThatStructureMatches(
                node<JAssignStmt>(
                    node<JInvokeStmt>(
                        node<JAssignStmt>(
                            node<JIfStmt>(
                                node<JGotoStmt>(
                                    node<JReturnStmt>()
                                ),
                                node<JInvokeStmt>(
                                    node<JReturnStmt>()
                                )
                            )
                        )
                    )
                ),
                cfg
            )
        }

        it("converts a class containing an if with a library usage in the true-branch to a filtered cfg") {
            val cfg = generateLibraryUsageGraph(
                libraryProject,
                TestProject(classpath = testClassesClassPath),
                "$TEST_CLASSES_PACKAGE.users.IfTrueUseTest",
                "test"
            )

            assertThatStructureMatches(
                node<JAssignStmt>(
                    node<JInvokeStmt>(
                        node<JAssignStmt>(
                            node<JIfStmt>(
                                node<JInvokeStmt>(
                                    node<JGotoStmt>(
                                        node<JReturnStmt>()
                                    )
                                ),
                                node<JReturnStmt>()
                            )
                        )
                    )
                ),
                cfg
            )
        }

        it("converts a class containing an if with a library usage in both branches to a filtered cfg") {
            val cfg = generateLibraryUsageGraph(
                libraryProject,
                TestProject(classpath = testClassesClassPath),
                "$TEST_CLASSES_PACKAGE.users.IfBothUseTest",
                "test"
            )

            assertThatStructureMatches(
                node<JAssignStmt>(
                    node<JInvokeStmt>(
                        node<JAssignStmt>(
                            node<JIfStmt>(
                                node<JInvokeStmt>(
                                    node<JGotoStmt>(
                                        node<JReturnStmt>()
                                    )
                                ),
                                node<JInvokeStmt>(
                                    node<JReturnStmt>()
                                )
                            )
                        )
                    )
                ),
                cfg
            )
        }

        it("converts a class containing an if with a library usage in both branches to a filtered cfg") {
            val cfg = generateLibraryUsageGraph(
                libraryProject,
                TestProject(classpath = testClassesClassPath),
                "$TEST_CLASSES_PACKAGE.users.IfNoUseTest",
                "test"
            )

            assertThatStructureMatches(
                node<JAssignStmt>(
                    node<JInvokeStmt>(
                        node<JAssignStmt>(
                            node<JReturnStmt>()
                        )
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
