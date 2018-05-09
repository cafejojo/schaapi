package org.cafejojo.schaapi.usagegraphgenerator.compare

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.RefType
import soot.Type
import soot.Value
import soot.jimple.DefinitionStmt
import soot.jimple.GotoStmt
import soot.jimple.IfStmt
import soot.jimple.InvokeExpr
import soot.jimple.InvokeStmt
import soot.jimple.ReturnStmt
import soot.jimple.ReturnVoidStmt
import soot.jimple.Stmt
import soot.jimple.SwitchStmt
import soot.jimple.ThrowStmt

internal class GeneralizedStmtComparatorStructureTest : Spek({
    lateinit var comparator: GeneralizedStmtComparator

    beforeEachTest {
        comparator = GeneralizedStmtComparator()
    }

    describe("type comparison of statements") {
        it("finds equality for the same type") {
            val templateValue = mock<Value> {
                on { it.type } doReturn RefType.v("MyClass")
            }
            val template = mock<IfStmt> {
                on { it.condition } doReturn templateValue
            }

            val instanceValue = mock<Value> {
                on { it.type } doReturn RefType.v("MyClass")
            }
            val instance = mock<IfStmt> {
                on { it.condition } doReturn instanceValue
            }

            assertThat(comparator.satisfies(template, instance)).isTrue()
        }

        it("finds equality if instance has subclass of template") {
            val templateValue = mock<Value> {
                on { it.type } doReturn RefType.v("java.lang.Object")
            }
            val template = mock<IfStmt> {
                on { it.condition } doReturn templateValue
            }

            val instanceValue = mock<Value> {
                on { it.type } doReturn RefType.v("java.lang.String")
            }
            val instance = mock<IfStmt> {
                on { it.condition } doReturn instanceValue
            }

            assertThat(comparator.satisfies(template, instance)).isTrue()
        }

        it("finds equality if template has subclass of instance") {
            val templateValue = mock<Value> {
                on { it.type } doReturn RefType.v("java.lang.String")
            }
            val template = mock<IfStmt> {
                on { it.condition } doReturn templateValue
            }

            val instanceValue = mock<Value> {
                on { it.type } doReturn RefType.v("java.lang.Object")
            }
            val instance = mock<IfStmt> {
                on { it.condition } doReturn instanceValue
            }

            assertThat(comparator.satisfies(template, instance)).isTrue()
        }

        it("finds inequality for different classes") {
            val templateValue = mock<Value> {
                on { it.type } doReturn RefType.v("MyClass")
            }
            val template = mock<IfStmt> {
                on { it.condition } doReturn templateValue
            }

            val instanceValue = mock<Value> {
                on { it.type } doReturn RefType.v("NotMyClass")
            }
            val instance = mock<IfStmt> {
                on { it.condition } doReturn instanceValue
            }

            assertThat(comparator.satisfies(template, instance)).isFalse()
        }

        it("finds inequality on dinges") {
            @SuppressWarnings("EqualsWithHashCodeExist")
            class NullType : Type() {
                override fun toString() = "null"

                @SuppressWarnings("EqualsAlwaysReturnsTrueOrFalse")
                override fun equals(other: Any?) = true
            }

            val templateValue = mock<Value> {
                on { it.type } doReturn NullType()
            }
            val template = mock<IfStmt> {
                on { it.condition } doReturn templateValue
            }

            val instanceValue = mock<Value> {
                on { it.type } doReturn NullType()
            }
            val instance = mock<IfStmt> {
                on { it.condition } doReturn instanceValue
            }

            assertThat(comparator.satisfies(template, instance)).isFalse()
        }
    }

    describe("structural comparison of statements") {
        context("throw statements") {
            fun mockThrowStmt(value: Value) =
                mock<ThrowStmt> {
                    on { it.op } doReturn value
                }

            it("equals itself") {
                val value = mockValue()
                val stmt = mockThrowStmt(value)

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("equals statements with the same values") {
                val value = mockValue()
                val template = mockThrowStmt(value)
                val instance = mockThrowStmt(value)

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("equals statements with the same structure") {
                val templateValue = mockValue()
                val template = mockThrowStmt(templateValue)

                val instanceValue = mockValue()
                val instance = mockThrowStmt(instanceValue)

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("does not equal statements with a different value type") {
                val templateValue = mockTypedValue()
                val template = mockThrowStmt(templateValue)

                val instanceValue = mockTypedValue()
                val instance = mockThrowStmt(instanceValue)

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }

            it("does not equal statements of a different class") {
                val value = mockValue()
                val template = mockThrowStmt(value)

                val instance = mock<Stmt> {}

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }

        context("definition statements") {
            fun mockDefinitionStmt(leftValue: Value, rightValue: Value) =
                mock<DefinitionStmt> {
                    on { it.leftOp } doReturn leftValue
                    on { it.rightOp } doReturn rightValue
                }

            it("equals itself") {
                val leftValue = mockValue()
                val rightValue = mockValue()
                val stmt = mockDefinitionStmt(leftValue, rightValue)

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("equals statements with the same values") {
                val leftValue = mockValue()
                val rightValue = mockValue()
                val template = mockDefinitionStmt(leftValue, rightValue)
                val instance = mockDefinitionStmt(leftValue, rightValue)

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("equals statements with the same structure") {
                val leftTemplateValue = mockValue()
                val rightTemplateValue = mockValue()
                val template = mockDefinitionStmt(leftTemplateValue, rightTemplateValue)

                val leftInstanceValue = mockValue()
                val rightInstanceValue = mockValue()
                val instance = mockDefinitionStmt(leftInstanceValue, rightInstanceValue)

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("does not equal statements with a different value type") {
                val leftTemplateValue = mockTypedValue()
                val rightTemplateValue = mockTypedValue()
                val template = mockDefinitionStmt(leftTemplateValue, rightTemplateValue)

                val leftInstanceValue = mockTypedValue()
                val rightInstanceValue = mockTypedValue()
                val instance = mockDefinitionStmt(leftInstanceValue, rightInstanceValue)

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }

            it("does not equal statements of a different class") {
                val leftValue = mockValue()
                val rightValue = mockValue()
                val template = mockDefinitionStmt(leftValue, rightValue)

                val instance = mock<Stmt> {}

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }

        context("if statements") {
            fun mockIfStmt(value: Value) =
                mock<IfStmt> {
                    on { it.condition } doReturn value
                }

            it("equals itself") {
                val value = mockValue()
                val stmt = mockIfStmt(value)

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("equals statements with the same values") {
                val value = mockValue()
                val template = mockIfStmt(value)
                val instance = mockIfStmt(value)

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("equals statements with the same structure") {
                val templateValue = mockValue()
                val template = mockIfStmt(templateValue)

                val instanceValue = mockValue()
                val instance = mockIfStmt(instanceValue)

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("does not equal statements with a different value type") {
                val templateValue = mockTypedValue()
                val template = mockIfStmt(templateValue)

                val instanceValue = mockTypedValue()
                val instance = mockIfStmt(instanceValue)

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }

            it("does not equal statements of a different class") {
                val value = mockValue()
                val template = mockIfStmt(value)

                val instance = mock<Stmt> {}

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }

        context("switch statements") {
            fun mockSwitchStmt(value: Value) =
                mock<SwitchStmt> {
                    on { it.key } doReturn value
                }

            it("equals itself") {
                val value = mockValue()
                val stmt = mockSwitchStmt(value)

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("equals statements with the same values") {
                val value = mockValue()
                val template = mockSwitchStmt(value)
                val instance = mockSwitchStmt(value)

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("equals statements with the same structure") {
                val templateValue = mockValue()
                val template = mockSwitchStmt(templateValue)

                val instanceValue = mockValue()
                val instance = mockSwitchStmt(instanceValue)

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("does not equal statements with a different value type") {
                val templateValue = mockTypedValue()
                val template = mockSwitchStmt(templateValue)

                val instanceValue = mockTypedValue()
                val instance = mockSwitchStmt(instanceValue)

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }

            it("does not equal statements of a different class") {
                val value = mockValue()
                val template = mockSwitchStmt(value)

                val instance = mock<Stmt> {}

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }

        context("invoke statements") {
            fun mockInvokeExpr() =
                mock<InvokeExpr> {
                    on { it.type } doReturn RefType.v("java.lang.Object")
                }

            fun mockTypedInvokeExpr() =
                mock<InvokeExpr> {
                    on { it.type } doReturn mock<Type> {}
                }

            fun mockInvokeStmt(invokeExpr: InvokeExpr) =
                mock<InvokeStmt> {
                    on { it.invokeExpr } doReturn invokeExpr
                }

            it("equals itself") {
                val value = mockInvokeExpr()
                val stmt = mockInvokeStmt(value)

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("equals statements with the same values") {
                val value = mockInvokeExpr()
                val template = mockInvokeStmt(value)
                val instance = mockInvokeStmt(value)

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("equals statements with the same structure") {
                val templateValue = mockInvokeExpr()
                val template = mockInvokeStmt(templateValue)

                val instanceValue = mockInvokeExpr()
                val instance = mockInvokeStmt(instanceValue)

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("does not equal statements with a different value type") {
                val templateValue = mockTypedInvokeExpr()
                val template = mockInvokeStmt(templateValue)

                val instanceValue = mockTypedInvokeExpr()
                val instance = mockInvokeStmt(instanceValue)

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }

            it("does not equal statements of a different class") {
                val value = mockInvokeExpr()
                val template = mockInvokeStmt(value)

                val instance = mock<Stmt> {}

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }

        context("return statements") {
            fun mockReturnStmt(value: Value) =
                mock<ReturnStmt> {
                    on { it.op } doReturn value
                }

            it("equals itself") {
                val value = mockValue()
                val stmt = mockReturnStmt(value)

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("equals statements with the same values") {
                val value = mockValue()
                val template = mockReturnStmt(value)
                val instance = mockReturnStmt(value)

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("equals statements with the same structure") {
                val templateValue = mockValue()
                val template = mockReturnStmt(templateValue)

                val instanceValue = mockValue()
                val instance = mockReturnStmt(instanceValue)

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("does not equal statements with a different value type") {
                val templateValue = mockTypedValue()
                val template = mockReturnStmt(templateValue)

                val instanceValue = mockTypedValue()
                val instance = mockReturnStmt(instanceValue)

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }

            it("does not equal statements of a different class") {
                val value = mockValue()
                val template = mockReturnStmt(value)

                val instance = mock<Stmt> {}

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }

        context("goto statements") {
            fun mockGotoStmt() =
                mock<GotoStmt> {}

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

                val instance = mock<Stmt> {}

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }

        context("return-void statements") {
            fun mockReturnVoidStmt() =
                mock<ReturnVoidStmt> {}

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

                val instance = mock<Stmt> {}

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }
    }
})
