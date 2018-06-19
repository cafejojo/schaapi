package org.cafejojo.schaapi.models.libraryusagegraph.jimple

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.BooleanType
import soot.IntType
import soot.jimple.DefinitionStmt
import soot.jimple.Jimple

internal object JimpleNodeTest : Spek({
    beforeGroup {
        SootNameEquivalenceChanger.activate()
    }

    describe("contained values") {
        context("non-recursive statements") {
            it("returns the operator of a throw statement") {
                val value = mockValue("value")
                val node = JimpleNode(mockThrowStmt(value))

                assertThat(node.getTopLevelValues()).containsExactly(value)
                assertThat(node.getValues()).containsExactly(value)
            }

            it("returns both operators of a definition statement") {
                val leftValue = mockValue("left")
                val rightValue = mockValue("right")
                val node = JimpleNode(mockDefinitionStmt(leftValue, rightValue))

                assertThat(node.getTopLevelValues()).containsExactly(leftValue, rightValue)
                assertThat(node.getValues()).containsExactly(leftValue, rightValue)
            }

            it("returns the condition of an if statement") {
                val value = mockValue("value")
                val node = JimpleNode(mockIfStmt(value))

                assertThat(node.getTopLevelValues()).containsExactly(value)
                assertThat(node.getValues()).containsExactly(value)
            }

            it("returns the key of a switch statement") {
                val value = mockValue("value")
                val node = JimpleNode(mockSwitchStmt(value))

                assertThat(node.getTopLevelValues()).containsExactly(value)
                assertThat(node.getValues()).containsExactly(value)
            }

            it("returns the invocation expression of an invoke statement") {
                val invokeExpr = mockInvokeExpr("value")
                val node = JimpleNode(mockInvokeStmt(invokeExpr))

                assertThat(node.getTopLevelValues()).containsExactly(invokeExpr)
                assertThat(node.getValues()).containsExactly(invokeExpr)
            }

            it("returns the operator of a return statement") {
                val value = mockValue("value")
                val node = JimpleNode(mockReturnStmt(value))

                assertThat(node.getTopLevelValues()).containsExactly(value)
                assertThat(node.getValues()).containsExactly(value)
            }

            it("returns nothing for a goto statement") {
                val node = JimpleNode(mockGotoStmt())

                assertThat(node.getTopLevelValues()).isEmpty()
                assertThat(node.getValues()).isEmpty()
            }

            it("returns nothing for a return void statement") {
                val node = JimpleNode(mockReturnVoidStmt())

                assertThat(node.getTopLevelValues()).isEmpty()
                assertThat(node.getValues()).isEmpty()
            }

            it("returns nothing for an unrecognised statement") {
                val node = JimpleNode(mockStmt())

                assertThat(node.getTopLevelValues()).isEmpty()
                assertThat(node.getValues()).isEmpty()
            }
        }

        context("recursive statements") {
            context("unop/binop") {
                it("finds the value in an expression") {
                    val value = mockValue("value")
                    val expr = SimpleUnopExpr(value)
                    val node = JimpleNode(mockIfStmt(expr))

                    assertThat(node.getTopLevelValues()).containsExactly(expr)
                    assertThat(node.getValues()).containsExactly(expr, value)
                }

                it("finds the value in an expression in an expression") {
                    val value = mockValue("value")
                    val innerExpr = SimpleUnopExpr(value)
                    val outerExpr = SimpleUnopExpr(innerExpr)
                    val node = JimpleNode(mockIfStmt(outerExpr))

                    assertThat(node.getTopLevelValues()).containsExactly(outerExpr)
                    assertThat(node.getValues()).containsExactly(outerExpr, value, innerExpr)
                }

                it("finds the values in an expression") {
                    val leftValue = mockValue("left")
                    val rightValue = mockValue("right")
                    val expr = SimpleBinopExpr(leftValue, rightValue)
                    val node = JimpleNode(mockIfStmt(expr))

                    assertThat(node.getTopLevelValues()).containsExactly(expr)
                    assertThat(node.getValues()).containsExactly(expr, leftValue, rightValue)
                }

                it("finds the values in an expression") {
                    val leftValue = mockValue("left")
                    val rightValue = mockValue("right")
                    val expr = SimpleBinopExpr(leftValue, rightValue)
                    val node = JimpleNode(mockIfStmt(expr))

                    assertThat(node.getTopLevelValues()).containsExactly(expr)
                    assertThat(node.getValues()).containsExactly(expr, leftValue, rightValue)
                }

                it("finds the values in two expressions") {
                    val leftValue = mockValue("left")
                    val leftExpr = SimpleUnopExpr(leftValue)

                    val rightValue = mockValue("right")
                    val rightExpr = SimpleUnopExpr(rightValue)

                    val node = JimpleNode(mockDefinitionStmt(leftExpr, rightExpr))

                    assertThat(node.getTopLevelValues()).containsExactly(leftExpr, rightExpr)
                    assertThat(node.getValues()).containsExactly(leftExpr, rightExpr, leftValue, rightValue)
                }

                it("finds the values in the expressions in two expressions") {
                    val leftLeftValue = mockValue("left-left")
                    val leftRightValue = mockValue("left-right")
                    val leftExpr = SimpleBinopExpr(leftLeftValue, leftRightValue)

                    val rightLeftValue = mockValue("right-left")
                    val rightRightValue = mockValue("right-right")
                    val rightExpr = SimpleBinopExpr(rightLeftValue, rightRightValue)

                    val node = JimpleNode(mockDefinitionStmt(leftExpr, rightExpr))

                    assertThat(node.getTopLevelValues()).containsExactly(leftExpr, rightExpr)
                    assertThat(node.getValues()).containsExactly(
                        leftExpr, rightExpr,
                        leftLeftValue, leftRightValue,
                        rightLeftValue, rightRightValue
                    )
                }

                it("finds the values in an invocation without arguments") {
                    val method = SimpleSootMethod("method", emptyList(), "output")
                    val base = mockValue("base")
                    @Suppress("SpreadOperator") // Cannot be avoided because of generic
                    val expr = SimpleInvokeExpr(base, method, *emptyArray<String>())
                    val node = JimpleNode(mockIfStmt(expr))

                    assertThat(node.getTopLevelValues()).containsExactly(expr)
                    assertThat(node.getValues()).containsExactly(expr, base)
                }
            }

            context("invocation") {
                it("finds the values in an invocation") {
                    val method = SimpleSootMethod("method", listOf("arg1", "arg2", "arg3"), "output")
                    val base = mockValue("base")
                    val arg1 = mockValue("arg1")
                    val arg2 = mockValue("arg2")
                    val arg3 = mockValue("arg3")
                    val expr = SimpleInvokeExpr(base, method, arg1, arg2, arg3)
                    val node = JimpleNode(mockIfStmt(expr))

                    assertThat(node.getTopLevelValues()).containsExactly(expr)
                    assertThat(node.getValues()).containsExactly(expr, arg1, arg2, arg3, base)
                }

                it("finds the values in the base of an invocation") {
                    val method = SimpleSootMethod("method", listOf("arg1", "arg2"), "output")
                    val baseValue = mockValue("base-value")
                    val baseExpr = SimpleUnopExpr(baseValue)
                    val arg1 = mockValue("arg1")
                    val arg2 = mockValue("arg2")
                    val expr = SimpleInvokeExpr(baseExpr, method, arg1, arg2)
                    val node = JimpleNode(mockIfStmt(expr))

                    assertThat(node.getTopLevelValues()).containsExactly(expr)
                    assertThat(node.getValues()).containsExactly(expr, arg1, arg2, baseValue, baseExpr)
                }

                it("finds the values in the argument of an invocation") {
                    val method = SimpleSootMethod("method", listOf("arg1", "arg2"), "output")
                    val base = mockValue("base")
                    val arg1 = mockValue("arg1")
                    val arg2Value = mockValue("arg2-value")
                    val arg2Expr = SimpleUnopExpr(arg2Value)
                    val expr = SimpleInvokeExpr(base, method, arg1, arg2Expr)
                    val node = JimpleNode(mockIfStmt(expr))

                    assertThat(node.getTopLevelValues()).containsExactly(expr)
                    assertThat(node.getValues()).containsExactly(expr, arg1, arg2Expr, arg2Value, base)
                }
            }
        }
    }

    describe("when checking whether two Jimple nodes are equal") {
        fun mockDefinitionStmt(leftType: String, rightType: String): JimpleNode {
            val leftOp = mockValue(leftType)
            val rightOp = mockValue(rightType)

            return JimpleNode(mock<DefinitionStmt> {
                on { it.leftOp } doReturn leftOp
                on { it.rightOp } doReturn rightOp
            })
        }

        it("should be equal if it contains a local") {
            val local1 = Jimple.v().newLocal("local1", IntType.v())
            val local2 = Jimple.v().newLocal("local2", IntType.v())
            val node1 = JimpleNode(Jimple.v().newReturnStmt(local1))
            val node2 = JimpleNode(Jimple.v().newReturnStmt(local2))

            assertThat(node1.equivTo(node2)).isTrue()
        }

        it("should have the same hash code if it contains a local") {
            val local1 = Jimple.v().newLocal("local1", IntType.v())
            val local2 = Jimple.v().newLocal("local2", IntType.v())
            val node1 = JimpleNode(Jimple.v().newReturnStmt(local1))
            val node2 = JimpleNode(Jimple.v().newReturnStmt(local2))

            assertThat(node1.equivHashCode()).isEqualTo(node2.equivHashCode())
        }

        it("should not equal if it contains a local with a different type") {
            val local1 = Jimple.v().newLocal("local", IntType.v())
            val local2 = Jimple.v().newLocal("local", BooleanType.v())
            val node1 = JimpleNode(Jimple.v().newReturnStmt(local1))
            val node2 = JimpleNode(Jimple.v().newReturnStmt(local2))

            assertThat(node1.equivTo(node2)).isFalse()
        }

        it("should have the same hash code if it contains a local") {
            val local1 = Jimple.v().newLocal("local", IntType.v())
            val local2 = Jimple.v().newLocal("local", BooleanType.v())
            val node1 = JimpleNode(Jimple.v().newReturnStmt(local1))
            val node2 = JimpleNode(Jimple.v().newReturnStmt(local2))

            assertThat(node1.equivHashCode()).isNotEqualTo(node2.equivHashCode())
        }

        it("should be equal if the values are in the same order and of the same type") {
            val node1 = mockDefinitionStmt("left", "right")
            val node2 = mockDefinitionStmt("left", "right")

            assertThat(node1.equivTo(node2)).isTrue()
        }

        it("should have the same hashcode if they are equal") {
            val node1 = mockDefinitionStmt("left", "right")
            val node2 = mockDefinitionStmt("left", "right")

            assertThat(node1.equivHashCode()).isEqualTo(node2.equivHashCode())
        }

        it("should not be equal if the values are not the same type") {
            val node1 = mockDefinitionStmt("left", "right-a")
            val node2 = mockDefinitionStmt("left", "right-b")

            assertThat(node1.equivTo(node2)).isFalse()
        }

        it("should not have the same hashcode if they are not equal") {
            val node1 = mockDefinitionStmt("left", "right-a")
            val node2 = mockDefinitionStmt("left", "right-b")

            assertThat(node1.equivHashCode()).isNotEqualTo(node2.equivHashCode())
        }

        it("should not have the same hashcode if they have the same value types but in a different order") {
            val node1 = mockDefinitionStmt("left", "right")
            val node2 = mockDefinitionStmt("right", "left")

            assertThat(node1.equivHashCode()).isNotEqualTo(node2.equivHashCode())
        }
    }

    describe("Jimple node copy") {
        it("does not equal the old node") {
            val stmt = Jimple.v().newBreakpointStmt()
            val node = JimpleNode(stmt, mutableListOf())
            val copy = node.copy()

            assertThat(copy).isNotSameAs(stmt)
            assertThat(copy).isNotEqualTo(stmt)
        }

        it("is equivalent to the old node") {
            val stmt = Jimple.v().newBreakpointStmt()
            val node = JimpleNode(stmt, mutableListOf())
            val copy = node.copy()

            assertThat(copy.equivTo(node))
            assertThat(copy.equivHashCode()).isEqualTo(node.equivHashCode())
        }

        it("uses a different list for the successors") {
            val stmt = Jimple.v().newBreakpointStmt()
            val successors = Array(3, { JimpleNode(Jimple.v().newBreakpointStmt()) })
            val node = JimpleNode(stmt, successors.toMutableList())
            val copy = node.copy()

            node.successors.removeAt(0)

            assertThat(node.successors).hasSize(2)
            assertThat(copy.successors).hasSize(3)
        }
    }
})
