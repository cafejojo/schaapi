package org.cafejojo.schaapi.pipeline.usagegraphgenerator.jimple

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimpleNode
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.jimple.Stmt
import soot.jimple.internal.JAssignStmt
import soot.jimple.internal.JGotoStmt
import soot.jimple.internal.JIfStmt
import soot.jimple.internal.JInvokeStmt
import soot.jimple.internal.JLookupSwitchStmt
import soot.jimple.internal.JReturnStmt
import soot.jimple.internal.JReturnVoidStmt
import soot.jimple.internal.JTableSwitchStmt

private const val TEST_CLASSES_PACKAGE = "org.cafejojo.schaapi.pipeline.usagegraphgenerator.jimple.testclasses"
private val testClassesClassPath = IntegrationTest::class.java.getResource("../../../../../../").toURI().path

internal class IntegrationTest : Spek({
    describe("the integration of different components of the package for simple classes") {
        it("converts a simple class to a filtered cfg") {
            val cfg = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf("$TEST_CLASSES_PACKAGE.users.SimpleTest"))
            )[1]

            assertThatStructureMatches(
                node<JAssignStmt>(
                    node<JInvokeStmt>(
                        node<JAssignStmt>(
                            node<JInvokeStmt>(
                                node<JReturnStmt>()
                            )
                        )
                    )
                ),
                cfg
            )
        }
    }

    describe("the integration of different components of the package for classes containing if statements") {
        it("converts a class containing an if with a library usage in the false-branch to a filtered cfg") {
            val cfg = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf("$TEST_CLASSES_PACKAGE.users.ifconditional.IfFalseUseTest"))
            )[1]

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
            val cfg = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf("$TEST_CLASSES_PACKAGE.users.ifconditional.IfTrueUseTest"))
            )[1]

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
            val cfg = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf("$TEST_CLASSES_PACKAGE.users.ifconditional.IfBothUseTest"))
            )[1]

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
            val cfg = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf("$TEST_CLASSES_PACKAGE.users.ifconditional.IfNoUseTest"))
            )[1]

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

        it("converts a class containing an if with no successors of the true/false branch") {
            val cfg = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf("$TEST_CLASSES_PACKAGE.users.ifconditional.IfNoEndTest"))
            )[1]

            assertThatStructureMatches(
                node<JAssignStmt>(
                    node<JInvokeStmt>(
                        node<JAssignStmt>(
                            node<JIfStmt>(
                                node<JReturnStmt>(),
                                node<JReturnStmt>()
                            )
                        )
                    )
                ),
                cfg
            )
        }
    }

    describe("the integration of different components of the package for classes containing switch statements") {
        it("converts a class containing a switch with a library usage in a branch to a filtered cfg") {
            val cfg = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf(
                    "$TEST_CLASSES_PACKAGE.users.switchconditional.SwitchOneUseTest"
                ))
            )[1]

            assertThatStructureMatches(
                node<JAssignStmt>(
                    node<JInvokeStmt>(
                        node<JAssignStmt>(
                            node<JTableSwitchStmt>(
                                node<JGotoStmt>(
                                    node<JReturnVoidStmt>()
                                ),
                                node<JInvokeStmt>(
                                    node<JGotoStmt>(
                                        node<JReturnVoidStmt>()
                                    )
                                ),
                                node<JReturnVoidStmt>()
                            )
                        )
                    )
                ),
                cfg
            )
        }

        it("converts a class containing a switch with a library usage in the default branch to a filtered cfg") {
            val cfg = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf(
                    "$TEST_CLASSES_PACKAGE.users.switchconditional.SwitchDefaultUseTest"
                ))
            )[1]

            assertThatStructureMatches(
                node<JAssignStmt>(
                    node<JInvokeStmt>(
                        node<JAssignStmt>(
                            node<JLookupSwitchStmt>(
                                node<JGotoStmt>(
                                    node<JReturnStmt>()
                                ),
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

        it("converts a class containing a switch with no library usage in its branches to a filtered cfg") {
            val cfg = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf(
                    "$TEST_CLASSES_PACKAGE.users.switchconditional.SwitchNoUseTest"
                ))
            )[1]

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

    describe("the integration of different components of the package for classes containing complex statements") {
        it("converts a class containing an arraylist and a lambda") {
            val cfg = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf("$TEST_CLASSES_PACKAGE.users.ArrayListAndLambdaTest"))
            )[1]

            assertThatStructureMatches(
                node<JAssignStmt>(
                    node<JInvokeStmt>(
                        node<JReturnStmt>()
                    )
                ),
                cfg
            )
        }

        it("converts a class containing annotations") {
            val cfg = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf("$TEST_CLASSES_PACKAGE.users.AnnotationTest"))
            )[1]

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

        it("converts a class containing a throw statement with library usage to a filtered cfg") {
            val cfg = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf("$TEST_CLASSES_PACKAGE.users.ThrowTest"))
            )[1]

            assertThatStructureMatches(
                node<JAssignStmt>(
                    node<JInvokeStmt>(
                        node<JAssignStmt>()
                    )
                ),
                cfg
            )
        }

        it("converts a class containing a try-catch statement with library usage in the try block to a filtered cfg") {
            val cfg = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf("$TEST_CLASSES_PACKAGE.users.TryCatchTest"))
            )[1]

            assertThatStructureMatches(
                node<JAssignStmt>(
                    node<JInvokeStmt>(
                        node<JInvokeStmt>(
                            node<JGotoStmt>(
                                node<JReturnVoidStmt>()
                            )
                        )
                    )
                ),
                cfg
            )
        }

        it("converts a class containing a static class call to a filtered cfg") {
            val cfg = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf("$TEST_CLASSES_PACKAGE.users.StaticTest"))
            )[1]

            assertThatStructureMatches(
                node<JInvokeStmt>(
                    node<JReturnVoidStmt>()
                ),
                cfg
            )
        }

        it("converts a class containing a nested loop to a filtered cfg") {
            val cfg = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf("$TEST_CLASSES_PACKAGE.users.LoopTest"))
            )[1]

            assertThatFlattenedStructureMatches(
                listOf(
                    node<JAssignStmt>(),
                    node<JInvokeStmt>(),
                    node<JAssignStmt>(),
                    node<JAssignStmt>(),
                    node<JIfStmt>(),
                    node<JInvokeStmt>(),
                    node<JAssignStmt>(),
                    node<JIfStmt>(),
                    node<JInvokeStmt>(),
                    node<JGotoStmt>(),
                    node<JInvokeStmt>(),
                    node<JGotoStmt>(),
                    node<JReturnVoidStmt>()
                ),
                cfg.iterator().asSequence().toList()
            )
        }

        it("converts a class containing a loop with a continue statement to a filtered cfg") {
            val cfg = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf("$TEST_CLASSES_PACKAGE.users.LoopContinueTest"))
            )[1]

            assertThatFlattenedStructureMatches(
                listOf(
                    node<JAssignStmt>(),
                    node<JInvokeStmt>(),
                    node<JAssignStmt>(),
                    node<JAssignStmt>(),
                    node<JIfStmt>(),
                    node<JInvokeStmt>(),
                    node<JAssignStmt>(),
                    node<JIfStmt>(),
                    node<JGotoStmt>(),
                    node<JGotoStmt>(),
                    node<JInvokeStmt>(),
                    node<JGotoStmt>(),
                    node<JReturnVoidStmt>()
                ),
                cfg.iterator().asSequence().toList()
            )
        }
    }
})

private fun assertThatStructureMatches(structure: Node, cfg: Node) {
    assertThat(cfg::class).isEqualTo(structure::class)
    assertThat(cfg.successors).hasSameSizeAs(structure.successors)

    if (cfg is JimpleNode && structure is JimpleNode) {
        assertThat(structure.statement).isInstanceOf(cfg.statement::class.java)
    }

    structure.successors.forEachIndexed { index, structureSuccessor ->
        if (structureSuccessor !is PreviousBranchNode)
            assertThatStructureMatches(structureSuccessor, cfg.successors[index])
    }
}

private fun assertThatFlattenedStructureMatches(structure: List<Node>, instance: List<Node>) {
    assertThat(structure.size == instance.size)

    structure.forEachIndexed { index, structureNode ->
        val instanceNode = instance[index]

        if (structureNode is JimpleNode && instanceNode is JimpleNode) {
            assertThat(structureNode.statement).isInstanceOf(instanceNode.statement::class.java)
        }
    }
}

private class PreviousBranchNode(override val successors: MutableList<Node> = arrayListOf()) : Node

private inline fun <reified T : Stmt> node(vararg successors: Node) = JimpleNode(mock<T>(), successors.toMutableList())
