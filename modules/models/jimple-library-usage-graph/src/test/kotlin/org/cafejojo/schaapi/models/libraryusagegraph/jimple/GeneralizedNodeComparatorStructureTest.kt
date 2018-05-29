package org.cafejojo.schaapi.models.libraryusagegraph.jimple

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.Value
import soot.jimple.ReturnStmt

/**
 * Unit tests for [GeneralizedNodeComparator.structuresAreEqual].
 */
internal class GeneralizedNodeComparatorStructureTest : Spek({
    lateinit var comparator: GeneralizedNodeComparator

    beforeEachTest {
        comparator = GeneralizedNodeComparator()
    }

    describe("structural comparison of statements") {
        context("throw statements") {
            it("equals itself") {
                val stmt = JimpleNode(mockThrowStmt(mockValue("value")))

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("equals statements with the same values") {
                val value = mockValue("value")
                val template = JimpleNode(mockThrowStmt(value))
                val instance = JimpleNode(mockThrowStmt(value))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("equals statements with the same structure") {
                val template = JimpleNode(mockThrowStmt(mockValue("value")))
                val instance = JimpleNode(mockThrowStmt(mockValue("value")))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("does not equal statements with a different value type") {
                val template = JimpleNode(mockThrowStmt(mockValue("template")))
                val instance = JimpleNode(mockThrowStmt(mockValue("instance")))

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }

            it("does not equal statements of a different class") {
                val template = JimpleNode(mockThrowStmt(mockValue("value")))
                val instance = JimpleNode(mockStmt())

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }

        context("definition statements") {
            it("equals itself") {
                val stmt = JimpleNode(mockDefinitionStmt(mockValue("left"), mockValue("right")))

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("equals statements with the same values") {
                val leftValue = mockValue("shared-left")
                val rightValue = mockValue("shared-right")
                val template = JimpleNode(mockDefinitionStmt(leftValue, rightValue))
                val instance = JimpleNode(mockDefinitionStmt(leftValue, rightValue))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("equals statements with the same structure") {
                val template = JimpleNode(mockDefinitionStmt(mockValue("shared-left"), mockValue("shared-right")))
                val instance = JimpleNode(mockDefinitionStmt(mockValue("shared-left"), mockValue("shared-right")))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("does not equal statements with a different value type") {
                val template =
                    JimpleNode(mockDefinitionStmt(mockValue("template-left"), mockValue("template-right")))
                val instance =
                    JimpleNode(mockDefinitionStmt(mockValue("instance-left"), mockValue("instance-right")))

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }

            it("does not equal statements of a different class") {
                val template = JimpleNode(mockDefinitionStmt(mockValue("left"), mockValue("right")))
                val instance = JimpleNode(mockStmt())

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }

        context("if statements") {
            it("equals itself") {
                val stmt = JimpleNode(mockIfStmt(mockValue("value")))

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("equals statements with the same values") {
                val value = mockValue("shared")
                val template = JimpleNode(mockIfStmt(value))
                val instance = JimpleNode(mockIfStmt(value))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("equals statements with the same structure") {
                val template = JimpleNode(mockIfStmt(mockValue("shared")))
                val instance = JimpleNode(mockIfStmt(mockValue("shared")))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("does not equal statements with a different value type") {
                val template = JimpleNode(mockIfStmt(mockValue("template")))
                val instance = JimpleNode(mockIfStmt(mockValue("instance")))

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }

            it("does not equal statements of a different class") {
                val template = JimpleNode(mockIfStmt(mockValue("value")))
                val instance = JimpleNode(mockStmt())

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }

        context("switch statements") {
            it("equals itself") {
                val stmt = JimpleNode(mockSwitchStmt(mockValue("value")))

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("equals statements with the same values") {
                val value = mockValue("shared")
                val template = JimpleNode(mockSwitchStmt(value))
                val instance = JimpleNode(mockSwitchStmt(value))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("equals statements with the same structure") {
                val template = JimpleNode(mockSwitchStmt(mockValue("shared")))
                val instance = JimpleNode(mockSwitchStmt(mockValue("shared")))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("does not equal statements with a different value type") {
                val template = JimpleNode(mockSwitchStmt(mockValue("template")))
                val instance = JimpleNode(mockSwitchStmt(mockValue("instance")))

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }

            it("does not equal statements of a different class") {
                val template = JimpleNode(mockSwitchStmt(mockValue("value")))
                val instance = JimpleNode(mockStmt())

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }

        context("invoke statements") {
            it("equals itself") {
                val stmt = JimpleNode(mockInvokeStmt(mockInvokeExpr("value")))

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("equals statements with the same values") {
                val value = mockInvokeExpr("shared")
                val template = JimpleNode(mockInvokeStmt(value))
                val instance = JimpleNode(mockInvokeStmt(value))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("equals statements with the same structure") {
                val template = JimpleNode(mockInvokeStmt(mockInvokeExpr("shared")))
                val instance = JimpleNode(mockInvokeStmt(mockInvokeExpr("shared")))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("does not equal statements with a different value type") {
                val template = JimpleNode(mockInvokeStmt(mockInvokeExpr("template")))
                val instance = JimpleNode(mockInvokeStmt(mockInvokeExpr("instance")))

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }

            it("does not equal statements of a different class") {
                val template = JimpleNode(mockInvokeStmt(mockInvokeExpr("value")))
                val instance = JimpleNode(mockStmt())

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }

        context("return statements") {
            fun mockReturnStmt(value: Value) =
                JimpleNode(mock<ReturnStmt> {
                    on { it.op } doReturn value
                })

            it("equals itself") {
                val stmt = mockReturnStmt(mockValue("value"))

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("equals statements with the same values") {
                val value = mockValue("shared")
                val template = mockReturnStmt(value)
                val instance = mockReturnStmt(value)

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("equals statements with the same structure") {
                val template = mockReturnStmt(mockValue("shared"))
                val instance = mockReturnStmt(mockValue("shared"))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("does not equal statements with a different value type") {
                val template = mockReturnStmt(mockValue("template"))
                val instance = mockReturnStmt(mockValue("instance"))

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }

            it("does not equal statements of a different class") {
                val template = mockReturnStmt(mockValue("value"))
                val instance = JimpleNode(mockStmt())

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }

        context("goto statements") {
            it("equals itself") {
                val stmt = JimpleNode(mockGotoStmt())

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("equals any other goto") {
                val template = JimpleNode(mockGotoStmt())
                val instance = JimpleNode(mockGotoStmt())

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("does not equal statements of a different class") {
                val template = JimpleNode(mockGotoStmt())
                val instance = JimpleNode(mockStmt())

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }

        context("return-void statements") {
            it("equals itself") {
                val stmt = JimpleNode(mockReturnVoidStmt())

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("equals any other return void") {
                val template = JimpleNode(mockReturnVoidStmt())
                val instance = JimpleNode(mockReturnVoidStmt())

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("does not equal statements of a different class") {
                val template = JimpleNode(mockReturnVoidStmt())
                val instance = JimpleNode(mockStmt())

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }
    }

    describe("recursive structural comparison of statements") {
        context("UnopExpr") {
            it("finds self-equality at one level of nesting") {
                val stmt = JimpleNode(mockIfStmt(SimpleUnopExpr("value")))

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("finds self-equality at two levels of nesting") {
                val stmt = JimpleNode(mockIfStmt(SimpleUnopExpr(SimpleUnopExpr("value"))))

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("finds equality for two equivalent nodes with one level of nesting") {
                val template = JimpleNode(mockIfStmt(SimpleUnopExpr("shared")))
                val instance = JimpleNode(mockIfStmt(SimpleUnopExpr("shared")))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("finds equality for two equivalent nodes with two levels of nesting") {
                val template = JimpleNode(mockIfStmt(SimpleUnopExpr(SimpleUnopExpr("shared"))))
                val instance = JimpleNode(mockIfStmt(SimpleUnopExpr(SimpleUnopExpr("shared"))))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("finds inequality for two nodes with different types at one level of nesting") {
                val template = JimpleNode(mockIfStmt(SimpleUnopExpr("template")))
                val instance = JimpleNode(mockIfStmt(SimpleUnopExpr("instance")))

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }

            it("finds inequality for two nodes with different types at two levels of nesting") {
                val template = JimpleNode(mockIfStmt(SimpleUnopExpr(SimpleUnopExpr("template"))))
                val instance = JimpleNode(mockIfStmt(SimpleUnopExpr(SimpleUnopExpr("instance"))))

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }

            it("finds inequality for two nodes with different levels of nesting") {
                val template = JimpleNode(mockIfStmt(SimpleUnopExpr("shared")))
                val instance = JimpleNode(mockIfStmt(SimpleUnopExpr(SimpleUnopExpr("shared"))))

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }

        context("BinopExpr") {
            it("finds self-equality at one level of nesting") {
                val stmt = JimpleNode(mockIfStmt(SimpleBinopExpr("left", "right")))

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("finds self-equality at two levels of nesting") {
                val stmt = JimpleNode(
                    mockIfStmt(
                        SimpleBinopExpr(
                            SimpleBinopExpr("left-left", "left-right"),
                            SimpleBinopExpr("right-left", "right-right")
                        )
                    )
                )

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("finds equality for two equivalent nodes with one level of nesting") {
                val template = JimpleNode(mockIfStmt(SimpleBinopExpr("shared-left", "shared-right")))
                val instance = JimpleNode(mockIfStmt(SimpleBinopExpr("shared-left", "shared-right")))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("finds equality for two equivalent nodes with two levels of nesting") {
                val template = JimpleNode(
                    mockIfStmt(
                        SimpleBinopExpr(
                            SimpleBinopExpr("shared-left-left", "shared-left-right"),
                            SimpleBinopExpr("shared-right-left", "shared-right-right")
                        )
                    )
                )
                val instance = JimpleNode(
                    mockIfStmt(
                        SimpleBinopExpr(
                            SimpleBinopExpr("shared-left-left", "shared-left-right"),
                            SimpleBinopExpr("shared-right-left", "shared-right-right")
                        )
                    )
                )

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("finds inequality for two nodes with different types at one level of nesting") {
                val template = JimpleNode(mockIfStmt(SimpleBinopExpr("left", "template-right")))
                val instance = JimpleNode(mockIfStmt(SimpleBinopExpr("left", "instance-right")))

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }

            it("finds inequality for two nodes with different types at two levels of nesting") {
                val template = JimpleNode(
                    mockIfStmt(
                        SimpleBinopExpr(
                            SimpleBinopExpr("left-left", "template-left-right"),
                            SimpleBinopExpr("right-left", "right-right")
                        )
                    )
                )
                val instance = JimpleNode(
                    mockIfStmt(
                        SimpleBinopExpr(
                            SimpleBinopExpr("left-left", "instance-left-right"),
                            SimpleBinopExpr("right-left", "right-right")
                        )
                    )
                )

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }

            it("finds inequality for two nodes with different levels of nesting") {
                val template = JimpleNode(mockIfStmt(SimpleBinopExpr("shared-left", "shared-right")))
                val instance = JimpleNode(mockIfStmt(SimpleBinopExpr(SimpleUnopExpr("shared-left"), "shared-right")))

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }

        context("InvokeExpr") {
            it("finds self-equality at one level of nesting") {
                val method = SimpleSootMethod("method", listOf("arg1", "arg2"), "output")
                val base = SimpleInvokeExpr("base", method, "arg1", "arg2")
                val node = JimpleNode(mockIfStmt(base))

                assertThat(comparator.structuresAreEqual(node, node)).isTrue()
            }

            it("finds self-equality at two levels of nesting in the base") {
                val innerMethod = SimpleSootMethod("inner-method", listOf("inner-arg1", "inner-arg2"), "inner-output")
                val innerBase = SimpleInvokeExpr("inner-base", innerMethod, "inner-arg1", "inner-arg2")

                val outerMethod = SimpleSootMethod("outer-method", listOf("outer-arg1", "outer-arg2"), "outer-output")
                val outerBase = SimpleInvokeExpr(innerBase, outerMethod, "outer-arg1", "outer-arg2")

                val node = JimpleNode(mockIfStmt(outerBase))

                assertThat(comparator.structuresAreEqual(node, node)).isTrue()
            }

            it("finds self-equality at two levels of nesting in an argument") {
                val innerMethod = SimpleSootMethod("inner-method", listOf("inner-arg1", "inner-arg2"), "inner-out")
                val innerBase = SimpleInvokeExpr("inner-base", innerMethod, "inner-arg1", "inner-arg2")

                val outerMethod = SimpleSootMethod("outer-method", listOf("outer-arg1", "outer-arg2"), "outer-out")
                val outerBase = SimpleInvokeExpr("base", outerMethod, mockValue("outer-arg1"), innerBase)

                val node = JimpleNode(mockIfStmt(outerBase))

                assertThat(comparator.structuresAreEqual(node, node)).isTrue()
            }

            it("finds equality for two equivalent nodes with one level of nesting") {
                val method = SimpleSootMethod("method", listOf("arg1", "arg2"), "output")

                val template = JimpleNode(mockIfStmt(SimpleInvokeExpr("base", method, "arg1", "arg2")))
                val instance = JimpleNode(mockIfStmt(SimpleInvokeExpr("base", method, "arg1", "arg2")))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("finds equality for two equivalent nodes with two levels of nesting in the base") {
                val innerMethod = SimpleSootMethod("inner-method", listOf("inner-arg1", "inner-arg2"), "inner-output")
                val outerMethod = SimpleSootMethod("outer-method", listOf("outer-arg1", "outer-arg2"), "outer-output")

                val templateInnerBase = SimpleInvokeExpr("inner-base", innerMethod, "inner-arg1", "inner-arg2")
                val templateOuterBase = SimpleInvokeExpr(templateInnerBase, outerMethod, "arg1", "arg2")
                val templateNode = JimpleNode(mockIfStmt(templateOuterBase))

                val instanceInnerBase = SimpleInvokeExpr("inner-base", innerMethod, "inner-arg1", "inner-arg2")
                val instanceOuterBase = SimpleInvokeExpr(instanceInnerBase, outerMethod, "arg1", "arg2")
                val instanceNode = JimpleNode(mockIfStmt(instanceOuterBase))

                assertThat(comparator.satisfies(templateNode, instanceNode)).isTrue()
            }

            it("finds equality for two equivalent nodes with two levels of nesting in an argument") {
                val innerMethod = SimpleSootMethod("inner-method", listOf("inner-arg1", "inner-arg2"), "inner-output")
                val outerMethod = SimpleSootMethod("outer-method", listOf("outer-arg1", "outer-arg2"), "outer-output")

                val templateArgument = SimpleInvokeExpr("inner-base", innerMethod, "inner-arg1", "inner-arg2")
                val templateInvoke = SimpleInvokeExpr(
                    "outer-base",
                    outerMethod,
                    mockValue("outer-arg1"), templateArgument
                )
                val templateNode = JimpleNode(mockIfStmt(templateInvoke))

                val instanceArgument = SimpleInvokeExpr("inner-base", innerMethod, "inner-arg1", "inner-arg2")
                val instanceInvoke = SimpleInvokeExpr(
                    "outer-base",
                    outerMethod,
                    mockValue("outer-arg1"), instanceArgument
                )
                val instanceNode = JimpleNode(mockIfStmt(instanceInvoke))

                assertThat(comparator.satisfies(templateNode, instanceNode)).isTrue()
            }

            it("finds inequality for two nodes with different types at one level of nesting") {
                val method = SimpleSootMethod("method", listOf("arg1", "arg2"), "output")

                val template = JimpleNode(mockIfStmt(SimpleInvokeExpr("base", method, "template-arg1", "arg2")))
                val instance = JimpleNode(mockIfStmt(SimpleInvokeExpr("base", method, "instance-arg1", "arg2")))

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }

            it("finds inequality for two nodes with different types at two levels of nesting in the base") {
                val innerMethod = SimpleSootMethod("inner-method", listOf("inner-arg1", "inner-arg2"), "inner-output")
                val outerMethod = SimpleSootMethod("outer-method", listOf("outer-arg1", "outer-arg2"), "outer-output")

                val templateInnerBase = SimpleInvokeExpr("inner-base", innerMethod, "template-inner-arg1", "inner-arg2")
                val templateOuterBase = SimpleInvokeExpr(templateInnerBase, outerMethod, "arg1", "arg2")
                val templateNode = JimpleNode(mockIfStmt(templateOuterBase))

                val instanceInnerBase = SimpleInvokeExpr("inner-base", innerMethod, "instance-inner-arg1", "inner-arg2")
                val instanceOuterBase = SimpleInvokeExpr(instanceInnerBase, outerMethod, "arg1", "arg2")
                val instanceNode = JimpleNode(mockIfStmt(instanceOuterBase))

                assertThat(comparator.satisfies(templateNode, instanceNode)).isFalse()
            }

            it("finds inequality for two nodes with different types at two levels of nesting in an argument") {
                val innerMethod = SimpleSootMethod("inner-method", listOf("inner-arg1", "inner-arg2"), "inner-output")
                val outerMethod = SimpleSootMethod("outer-method", listOf("outer-arg1", "outer-arg2"), "outer-output")

                val templateArgument = SimpleInvokeExpr("inner-base", innerMethod, "template-inner-arg1", "inner-arg2")
                val templateInvoke = SimpleInvokeExpr(
                    "outer-base",
                    outerMethod,
                    mockValue("outer-arg1"), templateArgument
                )
                val templateNode = JimpleNode(mockIfStmt(templateInvoke))

                val instanceArgument = SimpleInvokeExpr("inner-base", innerMethod, "instance-inner-arg1", "inner-arg2")
                val instanceInvoke = SimpleInvokeExpr(
                    "outer-base",
                    outerMethod,
                    mockValue("outer-arg1"), instanceArgument
                )
                val instanceNode = JimpleNode(mockIfStmt(instanceInvoke))

                assertThat(comparator.satisfies(templateNode, instanceNode)).isFalse()
            }

            it("finds inequality for two equivalent nodes if the called method is not exactly equal") {
                val templateMethod = SimpleSootMethod("method", listOf("arg1", "arg2"), "output")
                val templateInvoke = SimpleInvokeExpr("base", templateMethod, "arg1", "arg2")
                val templateNode = JimpleNode(mockIfStmt(templateInvoke))

                val instanceMethod = SimpleSootMethod("method", listOf("arg1", "arg2"), "output")
                val instanceInvoke = SimpleInvokeExpr("base", instanceMethod, "arg1", "arg2")
                val instanceNode = JimpleNode(mockIfStmt(instanceInvoke))

                assertThat(comparator.satisfies(templateNode, instanceNode)).isFalse()
            }
        }
    }
})
