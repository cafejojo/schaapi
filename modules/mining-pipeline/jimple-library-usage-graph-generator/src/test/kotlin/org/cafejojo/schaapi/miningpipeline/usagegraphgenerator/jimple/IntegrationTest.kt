package org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.SimpleNode
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

private const val TEST_CLASSES_PACKAGE = "org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.testclasses"
private val testClassesClassPath = IntegrationTest::class.java.getResource("../../../../../../").toURI().path

internal object IntegrationTest : Spek({
    describe("the integration of different components of the package for simple classes") {
        it("converts a simple class to a library usage graph") {
            val libraryUsageGraph = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, setOf("$TEST_CLASSES_PACKAGE.users.SimpleTest"))
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
                libraryUsageGraph
            )
        }
    }

    describe("the integration of different components of the package for classes containing if statements") {
        it("converts a class containing an if with a library usage in the false-branch to a library usage graph") {
            val libraryUsageGraph = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, setOf("$TEST_CLASSES_PACKAGE.users.ifconditional.IfFalseUseTest"))
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
                libraryUsageGraph
            )
        }

        it("converts a class containing an if with a library usage in the true-branch to a library usage graph") {
            val libraryUsageGraph = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, setOf("$TEST_CLASSES_PACKAGE.users.ifconditional.IfTrueUseTest"))
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
                libraryUsageGraph
            )
        }

        it("converts a class containing an if with a library usage in both branches to a library usage graph") {
            val libraryUsageGraph = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, setOf("$TEST_CLASSES_PACKAGE.users.ifconditional.IfBothUseTest"))
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
                libraryUsageGraph
            )
        }

        it("converts a class containing an if with a library usage in both branches to a library usage graph") {
            val libraryUsageGraph = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, setOf("$TEST_CLASSES_PACKAGE.users.ifconditional.IfNoUseTest"))
            )[0]

            assertThatStructureMatches(
                node<JAssignStmt>(
                    node<JInvokeStmt>(
                        node<JAssignStmt>(
                            node<JReturnStmt>()
                        )
                    )
                ),
                libraryUsageGraph
            )
        }

        it("converts a class containing an if with no successors of the true/false branch") {
            val libraryUsageGraph = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, setOf("$TEST_CLASSES_PACKAGE.users.ifconditional.IfNoEndTest"))
            )[0]

            assertThatStructureMatches(
                node<JAssignStmt>(
                    node<JInvokeStmt>(
                        node<JAssignStmt>()
                    )
                ),
                libraryUsageGraph
            )
        }

        it("filters out a class containing an if with method exitting return statements") {
            val libraryUsageGraphs = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, setOf("$TEST_CLASSES_PACKAGE.users.ifconditional.IfReturnsTest"))
            )

            assertThat(libraryUsageGraphs).isEmpty()
        }
    }

    describe("the integration of different components of the package for classes containing switch statements") {
        it("converts a class containing a switch with a library usage in a branch to a library usage graph") {
            val libraryUsageGraph = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, setOf(
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
                libraryUsageGraph
            )
        }

        it("converts a class containing a switch with a library usage in the default branch to a library usage graph") {
            val libraryUsageGraph = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, setOf(
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
                libraryUsageGraph
            )
        }

        it("converts a class containing a switch with no library usage in its branches to a library usage graph") {
            val libraryUsageGraph = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, setOf(
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
                libraryUsageGraph
            )
        }
    }

    describe("the integration of different components of the package for classes containing complex statements") {
        it("converts a class containing an arraylist and a lambda") {
            val libraryUsageGraph = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, setOf("$TEST_CLASSES_PACKAGE.users.ArrayListAndLambdaTest"))
            )[0]

            assertThatStructureMatches(
                node<JAssignStmt>(
                    node<JInvokeStmt>(
                        node<JInvokeStmt>(
                            node<JReturnStmt>()
                        )
                    )
                ),
                libraryUsageGraph
            )
        }

        it("converts a class containing annotations") {
            val libraryUsageGraph = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, setOf("$TEST_CLASSES_PACKAGE.users.AnnotationTest"))
            )[0]

            assertThatStructureMatches(
                node<JAssignStmt>(
                    node<JInvokeStmt>(
                        node<JReturnStmt>()
                    )
                ),
                libraryUsageGraph
            )
        }

        it("converts a class containing a throw statement with library usage to a library usage graph") {
            val libraryUsageGraph = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, setOf("$TEST_CLASSES_PACKAGE.users.ThrowTest"))
            )[0]

            assertThatStructureMatches(
                node<JAssignStmt>(
                    node<JInvokeStmt>()
                ),
                libraryUsageGraph
            )
        }

        it("does not convert a class containing a checked throw statement without library usage") {
            val libraryUsageGraphs = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath,
                    setOf("$TEST_CLASSES_PACKAGE.users.ThrowOtherUncheckedExceptionTest"))
            )

            assertThat(libraryUsageGraphs).isEmpty()
        }

        it("does not convert a class containing an unchecked throw statement without library usage") {
            val libraryUsageGraphs = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, setOf("$TEST_CLASSES_PACKAGE.users.ThrowOtherCheckedExceptionTest"))
            )

            assertThat(libraryUsageGraphs).isEmpty()
        }

        it("converts a class with a try-catch with library usage in the try block to a library usage graph") {
            val libraryUsageGraph = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, setOf("$TEST_CLASSES_PACKAGE.users.TryCatchTest"))
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
                libraryUsageGraph
            )
        }

        it("converts a class containing a static class call to a library usage graph") {
            val libraryUsageGraph = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, setOf("$TEST_CLASSES_PACKAGE.users.StaticTest"))
            )[0]

            assertThatStructureMatches(
                node<JInvokeStmt>(
                    node<JReturnVoidStmt>()
                ),
                libraryUsageGraph
            )
        }

        it("converts a class containing a nested loop to a library usage graph") {
            val libraryUsageGraph = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, setOf("$TEST_CLASSES_PACKAGE.users.LoopTest"))
            )[0]

            assertThatStructureMatches(
                node<JAssignStmt>(
                    node<JInvokeStmt>(
                        node<JAssignStmt>(
                            node<JIfStmt>(
                                node<JInvokeStmt>(
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
                                ),
                                node<JReturnVoidStmt>()
                            )
                        )
                    )
                ),
                libraryUsageGraph
            )
        }

        it("converts a class containing a loop with a continue statement to a library usage graph") {
            val libraryUsageGraph = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, setOf("$TEST_CLASSES_PACKAGE.users.LoopContinueTest"))
            )[0]

            assertThatStructureMatches(
                node<JAssignStmt>(
                    node<JInvokeStmt>(
                        node<JAssignStmt>(
                            node<JIfStmt>(
                                node<JInvokeStmt>(
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
                                ),
                                node<JReturnVoidStmt>()
                            )
                        )
                    )
                ),
                libraryUsageGraph
            )
        }
    }

    describe("the integration of different components for types containing non-concrete method declarations") {
        it("ignores a non-concrete interface method declaration") {
            val libraryUsageGraphs = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, setOf(
                    "$TEST_CLASSES_PACKAGE.users.InterfaceTest"
                ))
            )

            assertThat(libraryUsageGraphs).hasSize(0)
        }

        it("ignores a non-concrete abstract class method declaration") {
            val libraryUsageGraphs = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, setOf(
                    "$TEST_CLASSES_PACKAGE.users.AbstractClassTest"
                ))
            )

            assertThat(libraryUsageGraphs).hasSize(0)
        }

        it("ignores a non-concrete abstract class method declaration") {
            val libraryUsageGraphs = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, setOf(
                    "$TEST_CLASSES_PACKAGE.users.PartiallyAbstractClassTest"
                ))
            )

            // Should contain the fully specified method (not the declared method)
            assertThat(libraryUsageGraphs).hasSize(1)
        }
    }

    describe("it filters non-'empty' patterns") {
        it("filters out patterns with only return statements") {
            val libraryUsageGraphs = LibraryUsageGraphGenerator.generate(
                libraryProject,
                TestProject(testClassesClassPath, setOf(
                    "$TEST_CLASSES_PACKAGE.users.EmptyPatternTest"
                ))
            )

            assertThat(libraryUsageGraphs).isEmpty()
        }
    }
})

private fun assertThatStructureMatches(structure: Node, libraryUsageGraph: Node) {
    assertThat(libraryUsageGraph::class).isEqualTo(structure::class)
    assertThat(libraryUsageGraph.successors).hasSameSizeAs(structure.successors)

    if (libraryUsageGraph is JimpleNode && structure is JimpleNode) {
        assertThat(structure.statement).isInstanceOf(libraryUsageGraph.statement::class.java)
    }

    structure.successors.forEachIndexed { index, structureSuccessor ->
        if (structureSuccessor !is PreviousBranchNode)
            assertThatStructureMatches(structureSuccessor, libraryUsageGraph.successors[index])
    }
}

private class PreviousBranchNode(successors: MutableList<Node> = arrayListOf()) : SimpleNode(successors)

private inline fun <reified T : Stmt> node(vararg successors: Node) = JimpleNode(mock<T>(), successors.toMutableList())
