package org.cafejojo.schaapi.models.libraryusagegraph.jimple

import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.models.Node
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.IntType
import soot.Local
import soot.Value
import soot.jimple.IntConstant
import soot.jimple.Jimple

internal object JimplePathEnumeratorTest : Spek({
    describe("Jimple-aware path enumerator") {
        context("if-statements") {
            val local = Jimple.v().newLocal("local", IntType.v())
            val condition = Jimple.v().newEqExpr(local, IntConstant.v(3))
            val invertedCondition = Jimple.v().newNeExpr(local, IntConstant.v(3))

            it("constructs two converging paths for a regular if-statement") {
                val endNode = createReturnNode(local)
                val elseBranchNode = createAssignNode(local, 2, endNode)
                val ifBranchGotoNode = createGotoNode(endNode)
                val ifBranchNode = createAssignNode(local, 1, ifBranchGotoNode)
                val ifNode = createIfNode(condition, ifBranchNode, elseBranchNode)
                val localInitNode = createAssignNode(local, 0, ifNode)

                assertThatEnumeratorCreatesPaths(localInitNode,
                    listOf(
                        localInitNode,
                        createIfNode(condition, ifBranchNode, endNode),
                        ifBranchNode,
                        ifBranchGotoNode,
                        endNode
                    ),
                    listOf(
                        localInitNode,
                        createIfNode(invertedCondition, elseBranchNode, endNode),
                        elseBranchNode,
                        endNode
                    )
                )
            }

            it("makes the else-branch jump to the end if it contains a return") {
                val endNode = createReturnNode(local)
                val elseBranchNode = createReturnNode(local)
                val ifBranchGotoNode = createGotoNode(endNode)
                val ifBranchNode = createAssignNode(local, 1, ifBranchGotoNode)
                val ifNode = createIfNode(condition, ifBranchNode, elseBranchNode)
                val localInitNode = createAssignNode(local, 0, ifNode)

                assertThatEnumeratorCreatesPaths(localInitNode,
                    listOf(
                        localInitNode,
                        createIfNode(condition, ifBranchNode, endNode),
                        ifBranchNode,
                        ifBranchGotoNode,
                        endNode
                    ),
                    listOf(
                        localInitNode,
                        createIfNode(invertedCondition, endNode, endNode),
                        endNode
                    )
                )
            }

            it("makes the if-branch jump to the end if it contains a return") {
                val endNode = createReturnNode(local)
                val elseBranchNode = createAssignNode(local, 2, endNode)
                val ifBranchNode = createReturnNode(local)
                val ifNode = createIfNode(condition, ifBranchNode, elseBranchNode)
                val localInitNode = createAssignNode(local, 0, ifNode)

                assertThatEnumeratorCreatesPaths(localInitNode,
                    listOf(
                        localInitNode,
                        createIfNode(condition, ifBranchNode, endNode),
                        ifBranchNode
                    ),
                    listOf(
                        localInitNode,
                        createIfNode(invertedCondition, elseBranchNode, endNode),
                        elseBranchNode,
                        endNode
                    )
                )
            }

            it("makes both branches jump to the end if they both contain a return") {
                val elseBranchNode = createReturnNode(local)
                val ifBranchNode = createReturnNode(local)
                val ifNode = createIfNode(condition, ifBranchNode, elseBranchNode)
                val localInitNode = createAssignNode(local, 0, ifNode)

                assertThatEnumeratorCreatesPaths(localInitNode,
                    listOf(
                        localInitNode,
                        createIfNode(condition, ifBranchNode, ifBranchNode),
                        ifBranchNode
                    ),
                    listOf(
                        localInitNode,
                        createIfNode(invertedCondition, elseBranchNode, elseBranchNode),
                        elseBranchNode
                    )
                )
            }

            it("makes the else-branch jump past the if-branch's internal if-statement") {
                val endNode = createReturnNode(local)
                val elseBranchNode = createAssignNode(local, 4, endNode)
                val ifBranchGotoNode = createGotoNode(endNode)
                val ifBranchElseBranchNode = createAssignNode(local, 3, ifBranchGotoNode)
                val ifBranchIfBranchGotoNode = createGotoNode(ifBranchGotoNode)
                val ifBranchIfBranchNode = createAssignNode(local, 2, ifBranchIfBranchGotoNode)
                val ifBranchIfNode = createIfNode(condition, ifBranchIfBranchNode, ifBranchElseBranchNode)
                val ifBranchNode = createAssignNode(local, 1, ifBranchIfNode)
                val ifNode = createIfNode(condition, ifBranchNode, elseBranchNode)
                val localInitNode = createAssignNode(local, 0, ifNode)

                assertThatEnumeratorCreatesPaths(localInitNode,
                    listOf(
                        localInitNode,
                        createIfNode(condition, ifBranchNode, endNode),
                        ifBranchNode,
                        createIfNode(condition, ifBranchIfNode, ifBranchGotoNode),
                        ifBranchIfBranchNode,
                        ifBranchIfBranchGotoNode,
                        ifBranchGotoNode,
                        endNode
                    ),
                    listOf(
                        localInitNode,
                        createIfNode(condition, ifBranchNode, endNode),
                        ifBranchNode,
                        createIfNode(invertedCondition, ifBranchGotoNode, ifBranchElseBranchNode),
                        ifBranchElseBranchNode,
                        ifBranchGotoNode,
                        endNode
                    ),
                    listOf(
                        localInitNode,
                        createIfNode(invertedCondition, endNode, elseBranchNode),
                        elseBranchNode,
                        endNode
                    )
                )
            }
        }

        context("switch-statements") {
            val local = Jimple.v().newLocal("local", IntType.v())

            it("jumps over unused switch branches") {
                val endNode = createReturnNode(local)
                val switchDefaultTargetNode = createAssignNode(local, 3, endNode)
                val switchTargetBGoto = createGotoNode(endNode)
                val switchTargetBNode = createAssignNode(local, 2, switchTargetBGoto)
                val switchTargetAGoto = createGotoNode(endNode)
                val switchTargetANode = createAssignNode(local, 1, switchTargetAGoto)
                val switchNode = createSwitchNode(local, switchDefaultTargetNode, switchTargetANode, switchTargetBNode)
                val localInitNode = createAssignNode(local, 0, switchNode)

                assertThatEnumeratorCreatesPaths(localInitNode,
                    listOf(
                        localInitNode,
                        createSwitchNode(local, switchDefaultTargetNode, endNode, endNode),
                        switchDefaultTargetNode,
                        endNode
                    ),
                    listOf(
                        localInitNode,
                        createSwitchNode(local, endNode, switchTargetANode, endNode),
                        switchTargetANode,
                        switchTargetAGoto,
                        endNode
                    ),
                    listOf(
                        localInitNode,
                        createSwitchNode(local, endNode, endNode, switchTargetBNode),
                        switchTargetBNode,
                        switchTargetBGoto,
                        endNode
                    )
                )
            }

            it("jumps to the end if a branch does not converge with the path's active branch") {
                val endNode = createReturnNode(local)
                val switchDefaultTargetNode = createAssignNode(local, 3, endNode)
                val switchTargetBGoto = createGotoNode(endNode)
                val switchTargetBNode = createAssignNode(local, 2, switchTargetBGoto)
                val switchTargetANode = createReturnNode(local)
                val switchNode = createSwitchNode(local, switchDefaultTargetNode, switchTargetANode, switchTargetBNode)
                val localInitNode = createAssignNode(local, 0, switchNode)

                assertThatEnumeratorCreatesPaths(localInitNode,
                    listOf(
                        localInitNode,
                        createSwitchNode(local, switchDefaultTargetNode, endNode, endNode),
                        switchDefaultTargetNode,
                        endNode
                    ),
                    listOf(
                        localInitNode,
                        createSwitchNode(local, switchTargetANode, switchTargetANode, switchTargetANode),
                        switchTargetANode
                    ),
                    listOf(
                        localInitNode,
                        createSwitchNode(local, endNode, endNode, switchTargetBNode),
                        switchTargetBNode,
                        switchTargetBGoto,
                        endNode
                    )
                )
            }

            it("jumps to the end if the default branch does not converge with the path's active branch") {
                val endNode = createReturnNode(local)
                val switchDefaultTargetNode = createReturnNode(local)
                val switchTargetBGoto = createGotoNode(endNode)
                val switchTargetBNode = createAssignNode(local, 2, switchTargetBGoto)
                val switchTargetAGoto = createGotoNode(endNode)
                val switchTargetANode = createAssignNode(local, 1, switchTargetAGoto)
                val switchNode = createSwitchNode(local, switchDefaultTargetNode, switchTargetANode, switchTargetBNode)
                val localInitNode = createAssignNode(local, 0, switchNode)

                assertThatEnumeratorCreatesPaths(localInitNode,
                    listOf(
                        localInitNode,
                        createSwitchNode(local,
                            switchDefaultTargetNode,
                            switchDefaultTargetNode,
                            switchDefaultTargetNode),
                        switchDefaultTargetNode
                    ),
                    listOf(
                        localInitNode,
                        createSwitchNode(local, switchTargetANode, endNode, endNode),
                        switchTargetANode,
                        switchTargetAGoto,
                        endNode
                    ),
                    listOf(
                        localInitNode,
                        createSwitchNode(local, endNode, switchTargetBNode, endNode),
                        switchTargetBNode,
                        switchTargetBGoto,
                        endNode
                    )
                )
            }

            it("creates two different paths if switch targets go to the same location") {
                val endNode = createReturnNode(local)
                val switchDefaultTargetNode = createAssignNode(local, 3, endNode)
                val switchTargetCGoto = createGotoNode(endNode)
                val switchTargetCNode = createAssignNode(local, 2, switchTargetCGoto)
                val switchTargetABGoto = createGotoNode(endNode)
                val switchTargetABNode = createAssignNode(local, 1, switchTargetABGoto)
                val switchNode = createSwitchNode(local,
                    switchDefaultTargetNode,
                    switchTargetABNode,
                    switchTargetABNode,
                    switchTargetCNode
                )
                val localInitNode = createAssignNode(local, 0, switchNode)

                assertThatEnumeratorCreatesPaths(localInitNode,
                    listOf(
                        localInitNode,
                        createSwitchNode(local, switchDefaultTargetNode, endNode, endNode, endNode),
                        switchDefaultTargetNode,
                        endNode
                    ),
                    listOf(
                        localInitNode,
                        createSwitchNode(local, endNode, switchTargetABNode, endNode, endNode),
                        switchTargetABNode,
                        switchTargetABGoto,
                        endNode
                    ),
                    listOf(
                        localInitNode,
                        createSwitchNode(local, endNode, endNode, switchTargetABNode, endNode),
                        switchTargetABNode,
                        switchTargetABGoto,
                        endNode
                    ),
                    listOf(
                        localInitNode,
                        createSwitchNode(local, endNode, endNode, switchTargetCNode),
                        switchTargetCNode,
                        switchTargetCGoto,
                        endNode
                    )
                )
            }

            it("creates two different paths if a switch target is the same as the default target") {
                val endNode = createReturnNode(local)
                val switchTargetCDefaultNode = createAssignNode(local, 2, endNode)
                val switchTargetBGoto = createGotoNode(endNode)
                val switchTargetBNode = createAssignNode(local, 1, switchTargetBGoto)
                val switchTargetAGoto = createGotoNode(endNode)
                val switchTargetANode = createAssignNode(local, 1, switchTargetAGoto)
                val switchNode = createSwitchNode(local,
                    switchTargetCDefaultNode,
                    switchTargetANode,
                    switchTargetBNode,
                    switchTargetCDefaultNode
                )
                val localInitNode = createAssignNode(local, 0, switchNode)

                assertThatEnumeratorCreatesPaths(localInitNode,
                    listOf(
                        localInitNode,
                        createSwitchNode(local, switchTargetCDefaultNode, endNode, endNode, endNode),
                        switchTargetCDefaultNode,
                        endNode
                    ),
                    listOf(
                        localInitNode,
                        createSwitchNode(local, endNode, switchTargetANode, endNode, endNode),
                        switchTargetANode,
                        switchTargetAGoto,
                        endNode
                    ),
                    listOf(
                        localInitNode,
                        createSwitchNode(local, endNode, endNode, switchTargetBNode, endNode),
                        switchTargetBNode,
                        switchTargetBGoto,
                        endNode
                    ),
                    listOf(
                        localInitNode,
                        createSwitchNode(local, endNode, endNode, switchTargetCDefaultNode),
                        switchTargetCDefaultNode,
                        endNode
                    )
                )
            }

            it("jumps past the inner switch statement of another branch") {
                val endNode = createReturnNode(local)
                val switchDefault = createGotoNode(endNode)
                val switchGotoB = createGotoNode(endNode)
                val switchTargetB = createAssignNode(local, 5, switchGotoB)
                val switchGotoA = createGotoNode(endNode)
                val switchTargetASwitchDefault = createAssignNode(local, 4, switchGotoA)
                val switchTargetASwitchGotoB = createGotoNode(switchGotoA)
                val switchTargetASwitchTargetB = createAssignNode(local, 3, switchTargetASwitchGotoB)
                val switchTargetASwitchGotoA = createGotoNode(switchGotoA)
                val switchTargetASwitchTargetA = createAssignNode(local, 2, switchTargetASwitchGotoA)
                val switchTargetASwitch = createSwitchNode(
                    local,
                    switchTargetASwitchDefault,
                    switchTargetASwitchTargetA,
                    switchTargetASwitchTargetB
                )
                val switchTargetA = createAssignNode(local, 1, switchTargetASwitch)
                val switchNode = createSwitchNode(local, switchDefault, switchTargetA, switchTargetB)
                val localInitNode = createAssignNode(local, 0, switchNode)

                assertThatEnumeratorCreatesPaths(localInitNode,
                    listOf(
                        localInitNode,
                        createSwitchNode(local, switchDefault, endNode, endNode),
                        switchDefault,
                        endNode
                    ),
                    listOf(
                        localInitNode,
                        createSwitchNode(local, endNode, switchTargetA, endNode),
                        switchTargetA,
                        createSwitchNode(local, switchTargetASwitchDefault, switchGotoA, switchGotoA),
                        switchTargetASwitchDefault,
                        switchGotoA,
                        endNode
                    ),
                    listOf(
                        localInitNode,
                        createSwitchNode(local, endNode, switchTargetA, endNode),
                        switchTargetA,
                        createSwitchNode(local, switchGotoA, switchTargetASwitchTargetA, switchGotoA),
                        switchTargetASwitchTargetA,
                        switchTargetASwitchGotoA,
                        switchGotoA,
                        endNode
                    ),
                    listOf(
                        localInitNode,
                        createSwitchNode(local, endNode, switchTargetA, endNode),
                        switchTargetA,
                        createSwitchNode(local, switchGotoA, switchGotoA, switchTargetASwitchTargetB),
                        switchTargetASwitchTargetB,
                        switchTargetASwitchGotoB,
                        switchGotoA,
                        endNode
                    ),
                    listOf(
                        localInitNode,
                        createSwitchNode(local, endNode, endNode, switchTargetB),
                        switchTargetB,
                        switchGotoB,
                        endNode
                    )
                )
            }

            it("correctly identifies the current branch when the default is empty") {
                val endNode = createReturnNode(local)
                val switchTargetBGoto = createGotoNode(endNode)
                val switchTargetBNode = createAssignNode(local, 2, switchTargetBGoto)
                val switchTargetAGoto = createGotoNode(endNode)
                val switchTargetANode = createAssignNode(local, 1, switchTargetAGoto)
                val switchNode = createSwitchNode(local, endNode, switchTargetANode, switchTargetBNode)
                val localInitNode = createAssignNode(local, 0, switchNode)

                assertThatEnumeratorCreatesPaths(localInitNode,
                    listOf(
                        localInitNode,
                        createSwitchNode(local, endNode, endNode, endNode),
                        endNode
                    ),
                    listOf(
                        localInitNode,
                        createSwitchNode(local, endNode, switchTargetANode, endNode),
                        switchTargetANode,
                        switchTargetAGoto,
                        endNode
                    ),
                    listOf(
                        localInitNode,
                        createSwitchNode(local, endNode, endNode, switchTargetBNode),
                        switchTargetBNode,
                        switchTargetBGoto,
                        endNode
                    )
                )
            }
        }
    }
})

fun createIfNode(condition: Value, ifBranch: JimpleNode, elseBranch: JimpleNode) =
    JimpleNode(
        Jimple.v().newIfStmt(condition, elseBranch.statement),
        mutableListOf(ifBranch, elseBranch)
    )

fun createSwitchNode(switch: Value, defaultTarget: JimpleNode, vararg targets: JimpleNode) =
    JimpleNode(
        Jimple.v().newTableSwitchStmt(
            switch,
            0, targets.size - 1,
            targets.map { it.statement },
            defaultTarget.statement
        ),
        targets.toMutableList<Node>().also { it.add(defaultTarget) }
    )

fun createAssignNode(target: Local, value: Int, vararg successors: JimpleNode) =
    JimpleNode(Jimple.v().newAssignStmt(target, IntConstant.v(value)), successors.toMutableList())

fun createGotoNode(target: JimpleNode) =
    JimpleNode(Jimple.v().newGotoStmt(target.statement), mutableListOf(target))

fun createReturnNode(value: Value) =
    JimpleNode(Jimple.v().newReturnStmt(value))

fun assertThatEnumeratorCreatesPaths(entryNode: JimpleNode, vararg paths: List<JimpleNode>) {
    assertThat(JimplePathEnumerator(entryNode, 999).enumerate())
        .usingElementComparator(NodeComparator())
        .containsExactlyInAnyOrderElementsOf(paths.toList())
}

private class NodeComparator : Comparator<List<JimpleNode>> {
    override fun compare(o1: List<JimpleNode>, o2: List<JimpleNode>): Int {
        if (o1 === o2) return 0
        if (o1.size != o2.size) return -1
        return if ((0 until o1.size).all { o1[it].equivTo(o2[it]) }) 0 else -1
    }
}
