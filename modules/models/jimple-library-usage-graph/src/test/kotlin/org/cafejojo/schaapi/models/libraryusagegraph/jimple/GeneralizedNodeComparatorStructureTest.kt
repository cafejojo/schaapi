package org.cafejojo.schaapi.models.libraryusagegraph.jimple

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.cafejojo.schaapi.models.Node
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.Value
import soot.jimple.DefinitionStmt
import soot.jimple.GotoStmt
import soot.jimple.IfStmt
import soot.jimple.ReturnStmt
import soot.jimple.ReturnVoidStmt
import soot.jimple.SwitchStmt
import soot.jimple.ThrowStmt

internal class GeneralizedNodeComparatorStructureTest : Spek({
    lateinit var comparator: GeneralizedNodeComparator

    beforeEachTest {
        comparator = GeneralizedNodeComparator()
    }

    describe("bad weather cases") {
        it("throws an exception if a non-JimpleNode template is given") {
            val template = mock<Node> {}
            val instance = JimpleNode(mock {})

            assertThatThrownBy { comparator.structuresAreEqual(template, instance) }
                .isExactlyInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Jimple GeneralizedNodeComparator cannot handle non-Jimple nodes.")
        }

        it("throws an exception if a non-JimpleNode instance is given") {
            val template = JimpleNode(mock {})
            val instance = mock<Node> {}

            assertThatThrownBy { comparator.structuresAreEqual(template, instance) }
                .isExactlyInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Jimple GeneralizedNodeComparator cannot handle non-Jimple nodes.")
        }
    }

    describe("structural comparison of statements") {
        context("throw statements") {
            fun mockThrowStmt(value: Value) =
                JimpleNode(mock<ThrowStmt> {
                    on { it.op } doReturn value
                })

            it("equals itself") {
                val stmt = mockThrowStmt(SimpleValue("value"))

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("equals statements with the same values") {
                val value = SimpleValue("value")
                val template = mockThrowStmt(value)
                val instance = mockThrowStmt(value)

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("equals statements with the same structure") {
                val template = mockThrowStmt(SimpleValue("value"))
                val instance = mockThrowStmt(SimpleValue("value"))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("does not equal statements with a different value type") {
                val template = mockThrowStmt(SimpleValue("template"))
                val instance = mockThrowStmt(SimpleValue("instance"))

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }

            it("does not equal statements of a different class") {
                val template = mockThrowStmt(SimpleValue("value"))
                val instance = JimpleNode(mock {})

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }

        context("definition statements") {
            fun mockDefinitionStmt(leftValue: Value, rightValue: Value) =
                JimpleNode(mock<DefinitionStmt> {
                    on { it.leftOp } doReturn leftValue
                    on { it.rightOp } doReturn rightValue
                })

            it("equals itself") {
                val stmt = mockDefinitionStmt(SimpleValue("left"), SimpleValue("right"))

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("equals statements with the same values") {
                val leftValue = SimpleValue("left-shared")
                val rightValue = SimpleValue("right-shared")
                val template = mockDefinitionStmt(leftValue, rightValue)
                val instance = mockDefinitionStmt(leftValue, rightValue)

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("equals statements with the same structure") {
                val template = mockDefinitionStmt(SimpleValue("shared-left"), SimpleValue("shared-right"))
                val instance = mockDefinitionStmt(SimpleValue("shared-left"), SimpleValue("shared-right"))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("does not equal statements with a different value type") {
                val template = mockDefinitionStmt(SimpleValue("template-left"), SimpleValue("template-right"))
                val instance = mockDefinitionStmt(SimpleValue("instance-left"), SimpleValue("instance-right"))

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }

            it("does not equal statements of a different class") {
                val template = mockDefinitionStmt(SimpleValue("left"), SimpleValue("right"))
                val instance = JimpleNode(mock {})

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }

        context("if statements") {
            fun mockIfStmt(value: Value) =
                JimpleNode(mock<IfStmt> {
                    on { it.condition } doReturn value
                })

            it("equals itself") {
                val stmt = mockIfStmt(SimpleValue("value"))

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("equals statements with the same values") {
                val value = SimpleValue("shared")
                val template = mockIfStmt(value)
                val instance = mockIfStmt(value)

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("equals statements with the same structure") {
                val template = mockIfStmt(SimpleValue("shared"))
                val instance = mockIfStmt(SimpleValue("shared"))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("does not equal statements with a different value type") {
                val template = mockIfStmt(SimpleValue("template"))
                val instance = mockIfStmt(SimpleValue("instance"))

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }

            it("does not equal statements of a different class") {
                val template = mockIfStmt(SimpleValue("value"))
                val instance = JimpleNode(mock {})

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }

        context("switch statements") {
            fun mockSwitchStmt(value: Value) =
                JimpleNode(mock<SwitchStmt> {
                    on { it.key } doReturn value
                })

            it("equals itself") {
                val stmt = mockSwitchStmt(SimpleValue("value"))

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("equals statements with the same values") {
                val value = SimpleValue("shared")
                val template = mockSwitchStmt(value)
                val instance = mockSwitchStmt(value)

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("equals statements with the same structure") {
                val template = mockSwitchStmt(SimpleValue("shared"))
                val instance = mockSwitchStmt(SimpleValue("shared"))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("does not equal statements with a different value type") {
                val template = mockSwitchStmt(SimpleValue("template"))
                val instance = mockSwitchStmt(SimpleValue("instance"))

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }

            it("does not equal statements of a different class") {
                val template = mockSwitchStmt(SimpleValue("value"))
                val instance = JimpleNode(mock {})

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }

//        context("invoke statements") {
//            fun mockInvokeExpr(type: String) =
//                mock<InvokeExpr> {
//                    on { it.type } doReturn RefType.v(type)
//                    on { it.equivTo(any()) } doReturn {}
//                }
//
//            fun mockTypedInvokeExpr() =
//                mock<InvokeExpr> {
//                    on { it.type } doReturn mock<Type> {}
//                }
//
//            fun mockInvokeStmt(invokeExpr: InvokeExpr) =
//                JimpleNode(mock<InvokeStmt> {
//                    on { it.invokeExpr } doReturn invokeExpr
//                })
//
//            it("equals itself") {
//                val value = mockInvokeExpr()
//                val stmt = mockInvokeStmt(value)
//
//                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
//            }
//
//            it("equals statements with the same values") {
//                val value = mockInvokeExpr()
//                val template = mockInvokeStmt(value)
//                val instance = mockInvokeStmt(value)
//
//                assertThat(comparator.satisfies(template, instance)).isTrue()
//            }
//
//            it("equals statements with the same structure") {
//                val templateValue = mockInvokeExpr()
//                val template = mockInvokeStmt(templateValue)
//
//                val instanceValue = mockInvokeExpr()
//                val instance = mockInvokeStmt(instanceValue)
//
//                assertThat(comparator.satisfies(template, instance)).isTrue()
//            }
//
//            it("does not equal statements with a different value type") {
//                val templateValue = mockTypedInvokeExpr()
//                val template = mockInvokeStmt(templateValue)
//
//                val instanceValue = mockTypedInvokeExpr()
//                val instance = mockInvokeStmt(instanceValue)
//
//                assertThat(comparator.satisfies(template, instance)).isFalse()
//            }
//
//            it("does not equal statements of a different class") {
//                val value = mockInvokeExpr()
//                val template = mockInvokeStmt(value)
//
//                val instance = JimpleNode(mock<Stmt> {})
//
//                assertThat(comparator.satisfies(template, instance)).isFalse()
//            }
//        }

        context("return statements") {
            fun mockReturnStmt(value: Value) =
                JimpleNode(mock<ReturnStmt> {
                    on { it.op } doReturn value
                })

            it("equals itself") {
                val stmt = mockReturnStmt(SimpleValue("value"))

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("equals statements with the same values") {
                val value = SimpleValue("shared")
                val template = mockReturnStmt(value)
                val instance = mockReturnStmt(value)

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("equals statements with the same structure") {
                val template = mockReturnStmt(SimpleValue("shared"))
                val instance = mockReturnStmt(SimpleValue("shared"))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("does not equal statements with a different value type") {
                val template = mockReturnStmt(SimpleValue("template"))
                val instance = mockReturnStmt(SimpleValue("instance"))

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }

            it("does not equal statements of a different class") {
                val template = mockReturnStmt(SimpleValue("value"))
                val instance = JimpleNode(mock {})

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }

        context("goto statements") {
            fun mockGotoStmt() = JimpleNode(mock<GotoStmt> {})

            it("equals itself") {
                val stmt = mockGotoStmt()

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("equals any other goto") {
                val template = mockGotoStmt()
                val instance = mockGotoStmt()

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("does not equal statements of a different class") {
                val template = mockGotoStmt()
                val instance = JimpleNode(mock {})

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }

        context("return-void statements") {
            fun mockReturnVoidStmt() = JimpleNode(mock<ReturnVoidStmt> {})

            it("equals itself") {
                val stmt = mockReturnVoidStmt()

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("equals any other return void") {
                val template = mockReturnVoidStmt()
                val instance = mockReturnVoidStmt()

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("does not equal statements of a different class") {
                val template = mockReturnVoidStmt()
                val instance = JimpleNode(mock {})

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }
    }
})
