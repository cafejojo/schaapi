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
        it("converts a simple class to a filtered lug") {
            val lug = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf("$TEST_CLASSES_PACKAGE.users.SimpleTest"))
            )[0]

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
                lug
            )
        }
    }

    describe("the integration of different components of the package for classes containing if statements") {
        it("converts a class containing an if with a library usage in the false-branch to a filtered lug") {
            val lug = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf("$TEST_CLASSES_PACKAGE.users.ifconditional.IfFalseUseTest"))
            )[0]

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
                lug
            )
        }

        it("converts a class containing an if with a library usage in the true-branch to a filtered lug") {
            val lug = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf("$TEST_CLASSES_PACKAGE.users.ifconditional.IfTrueUseTest"))
            )[0]

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
                lug
            )
        }

        it("converts a class containing an if with a library usage in both branches to a filtered lug") {
            val lug = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf("$TEST_CLASSES_PACKAGE.users.ifconditional.IfBothUseTest"))
            )[0]

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
                lug
            )
        }

        it("converts a class containing an if with a library usage in both branches to a filtered lug") {
            val lug = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf("$TEST_CLASSES_PACKAGE.users.ifconditional.IfNoUseTest"))
            )[0]

            assertThatStructureMatches(
                node<JAssignStmt>(
                    node<JInvokeStmt>(
                        node<JAssignStmt>(
                            node<JReturnStmt>()
                        )
                    )
                ),
                lug
            )
        }

        it("converts a class containing an if with no successors of the true/false branch") {
            val lug = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf("$TEST_CLASSES_PACKAGE.users.ifconditional.IfNoEndTest"))
            )[0]

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
                lug
            )
        }
    }

    describe("the integration of different components of the package for classes containing switch statements") {
        it("converts a class containing a switch with a library usage in a branch to a filtered lug") {
            val lug = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf(
                    "$TEST_CLASSES_PACKAGE.users.switchconditional.SwitchOneUseTest"
                ))
            )[0]

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
                lug
            )
        }

        it("converts a class containing a switch with a library usage in the default branch to a filtered lug") {
            val lug = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf(
                    "$TEST_CLASSES_PACKAGE.users.switchconditional.SwitchDefaultUseTest"
                ))
            )[0]

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
                lug
            )
        }

        it("converts a class containing a switch with no library usage in its branches to a filtered lug") {
            val lug = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf(
                    "$TEST_CLASSES_PACKAGE.users.switchconditional.SwitchNoUseTest"
                ))
            )[0]

            assertThatStructureMatches(
                node<JAssignStmt>(
                    node<JInvokeStmt>(
                        node<JAssignStmt>(
                            node<JReturnStmt>()
                        )
                    )
                ),
                lug
            )
        }
    }

    describe("the integration of different components of the package for classes containing complex statements") {
        it("converts a class containing an arraylist and a lambda") {
            val lug = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf("$TEST_CLASSES_PACKAGE.users.ArrayListAndLambdaTest"))
            )[0]

            assertThatStructureMatches(
                node<JAssignStmt>(
                    node<JInvokeStmt>(
                        node<JReturnStmt>()
                    )
                ),
                lug
            )
        }

        it("converts a class containing annotations") {
            val lug = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf("$TEST_CLASSES_PACKAGE.users.AnnotationTest"))
            )[0]

            assertThatStructureMatches(
                node<JAssignStmt>(
                    node<JInvokeStmt>(
                        node<JAssignStmt>(
                            node<JReturnStmt>()
                        )
                    )
                ),
                lug
            )
        }

        it("converts a class containing a throw statement with library usage to a filtered lug") {
            val lug = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf("$TEST_CLASSES_PACKAGE.users.ThrowTest"))
            )[0]

            assertThatStructureMatches(
                node<JAssignStmt>(
                    node<JInvokeStmt>(
                        node<JAssignStmt>()
                    )
                ),
                lug
            )
        }

        it("does not convert a class containing a checked throw statement without library usage to a filtered lug") {
            val lugs = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath,
                    listOf("$TEST_CLASSES_PACKAGE.users.ThrowOtherUncheckedExceptionTest"))
            )

            assertThat(lugs).isEmpty()
        }

        it("does not convert a class containing an unchecked throw statement without library usage to a filtered lug") {
            val lugs = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf("$TEST_CLASSES_PACKAGE.users.ThrowOtherCheckedExceptionTest"))
            )

            assertThat(lugs).isEmpty()
        }

        it("converts a class containing a try-catch statement with library usage in the try block to a filtered lug") {
            val lug = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf("$TEST_CLASSES_PACKAGE.users.TryCatchTest"))
            )[0]

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
                lug
            )
        }

        it("converts a class containing a static class call to a filtered lug") {
            val lug = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf("$TEST_CLASSES_PACKAGE.users.StaticTest"))
            )[0]

            assertThatStructureMatches(
                node<JInvokeStmt>(
                    node<JReturnVoidStmt>()
                ),
                lug
            )
        }

        it("converts a class containing a nested loop to a filtered lug") {
            val lug = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf("$TEST_CLASSES_PACKAGE.users.LoopTest"))
            )[0]

            assertThatStructureMatches(
                node<JAssignStmt>(
                    node<JInvokeStmt>(
                        node<JAssignStmt>(
                            node<JAssignStmt>(
                                node<JIfStmt>(
                                    node<JInvokeStmt>(
                                        node<JAssignStmt>(
                                            node<JIfStmt>(
                                                node<JInvokeStmt>(
                                                    node<JGotoStmt>(
                                                        PreviousBranchNode()
                                                    )
                                                ),
                                                node<JInvokeStmt>(
                                                    node<JGotoStmt>(
                                                        PreviousBranchNode()
                                                    )
                                                )
                                            )
                                        )
                                    ),
                                    node<JReturnVoidStmt>()
                                )
                            )
                        )
                    )
                ),
                lug
            )
        }

        it("converts a class containing a loop with a continue statement to a filtered lug") {
            val lug = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf("$TEST_CLASSES_PACKAGE.users.LoopContinueTest"))
            )[0]

            assertThatStructureMatches(
                node<JAssignStmt>(
                    node<JInvokeStmt>(
                        node<JAssignStmt>(
                            node<JAssignStmt>(
                                node<JIfStmt>(
                                    node<JInvokeStmt>(
                                        node<JAssignStmt>(
                                            node<JIfStmt>(
                                                node<JGotoStmt>(
                                                    node<JGotoStmt>(
                                                        PreviousBranchNode()
                                                    )
                                                ),
                                                node<JInvokeStmt>(
                                                    node<JGotoStmt>(
                                                        PreviousBranchNode()
                                                    )
                                                )
                                            )
                                        )
                                    ),
                                    node<JReturnVoidStmt>()
                                )
                            )
                        )
                    )
                ),
                lug
            )
        }
    }

    describe("the integration of different components for types containing non-concrete method declarations") {
        it("ignores a non-concrete interface method declaration") {
            val lugs = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf(
                    "$TEST_CLASSES_PACKAGE.users.InterfaceTest"
                ))
            )

            assertThat(lugs).hasSize(0)
        }

        it("ignores a non-concrete abstract class method declaration") {
            val lugs = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf(
                    "$TEST_CLASSES_PACKAGE.users.AbstractClassTest"
                ))
            )

            assertThat(lugs).hasSize(0)
        }

        it("ignores a non-concrete abstract class method declaration") {
            val lugs = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf(
                    "$TEST_CLASSES_PACKAGE.users.PartiallyAbstractClassTest"
                ))
            )

            // Should contain the fully specified method (not the declared method)
            assertThat(lugs).hasSize(1)
        }
    }

    describe("it filters non-'empty' patterns") {
        it("filters out patterns with only return statements") {
            val lugs = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, listOf(
                    "$TEST_CLASSES_PACKAGE.users.EmptyPatternTest"
                ))
            )

            assertThat(lugs).isEmpty()
        }
    }
})

private fun assertThatStructureMatches(structure: Node, lug: Node) {
    assertThat(lug::class).isEqualTo(structure::class)
    assertThat(lug.successors).hasSameSizeAs(structure.successors)

    if (lug is JimpleNode && structure is JimpleNode) {
        assertThat(structure.statement).isInstanceOf(lug.statement::class.java)
    }

    structure.successors.forEachIndexed { index, structureSuccessor ->
        if (structureSuccessor !is PreviousBranchNode)
            assertThatStructureMatches(structureSuccessor, lug.successors[index])
    }
}

private class PreviousBranchNode(override val successors: MutableList<Node> = arrayListOf()) : Node

private inline fun <reified T : Stmt> node(vararg successors: Node) = JimpleNode(mock<T>(), successors.toMutableList())
