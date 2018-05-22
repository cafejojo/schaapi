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
import soot.jimple.GotoStmt
import soot.jimple.ReturnStmt
import soot.jimple.ReturnVoidStmt

/**
 * Unit tests for [GeneralizedNodeComparator.structuresAreEqual].
 */
internal class GeneralizedNodeComparatorStructureTest : Spek({
    lateinit var comparator: GeneralizedNodeComparator

    beforeEachTest {
        comparator = GeneralizedNodeComparator()
    }

    describe("bad weather cases") {
        it("throws an exception if a non-JimpleNode template is given") {
            val template = mock<Node> {}
            val instance = JimpleNode(mockStmt())

            assertThatThrownBy { comparator.generalizedValuesAreEqual(template, instance) }
                .isExactlyInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Jimple GeneralizedNodeComparator cannot handle non-Jimple nodes.")
        }

        it("throws an exception if a non-JimpleNode instance is given") {
            val template = JimpleNode(mockStmt())
            val instance = mock<Node> {}

            assertThatThrownBy { comparator.generalizedValuesAreEqual(template, instance) }
                .isExactlyInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Jimple GeneralizedNodeComparator cannot handle non-Jimple nodes.")
        }
    }

    describe("structural comparison of statements") {
        context("throw statements") {
            it("equals itself") {
                val stmt = JimpleNode(mockThrowStmt(SimpleValue("value")))

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("equals statements with the same values") {
                val value = SimpleValue("value")
                val template = JimpleNode(mockThrowStmt(value))
                val instance = JimpleNode(mockThrowStmt(value))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("equals statements with the same structure") {
                val template = JimpleNode(mockThrowStmt(SimpleValue("value")))
                val instance = JimpleNode(mockThrowStmt(SimpleValue("value")))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("does not equal statements with a different value type") {
                val template = JimpleNode(mockThrowStmt(SimpleValue("template")))
                val instance = JimpleNode(mockThrowStmt(SimpleValue("instance")))

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }

            it("does not equal statements of a different class") {
                val template = JimpleNode(mockThrowStmt(SimpleValue("value")))
                val instance = JimpleNode(mockStmt())

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }

        context("definition statements") {
            it("equals itself") {
                val stmt = JimpleNode(mockDefinitionStmt(SimpleValue("left"), SimpleValue("right")))

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("equals statements with the same values") {
                val leftValue = SimpleValue("left-shared")
                val rightValue = SimpleValue("right-shared")
                val template = JimpleNode(mockDefinitionStmt(leftValue, rightValue))
                val instance = JimpleNode(mockDefinitionStmt(leftValue, rightValue))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("equals statements with the same structure") {
                val template = JimpleNode(mockDefinitionStmt(SimpleValue("shared-left"), SimpleValue("shared-right")))
                val instance = JimpleNode(mockDefinitionStmt(SimpleValue("shared-left"), SimpleValue("shared-right")))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("does not equal statements with a different value type") {
                val template =
                    JimpleNode(mockDefinitionStmt(SimpleValue("template-left"), SimpleValue("template-right")))
                val instance =
                    JimpleNode(mockDefinitionStmt(SimpleValue("instance-left"), SimpleValue("instance-right")))

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }

            it("does not equal statements of a different class") {
                val template = JimpleNode(mockDefinitionStmt(SimpleValue("left"), SimpleValue("right")))
                val instance = JimpleNode(mockStmt())

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }

        context("if statements") {
            it("equals itself") {
                val stmt = JimpleNode(mockIfStmt(SimpleValue("value")))

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("equals statements with the same values") {
                val value = SimpleValue("shared")
                val template = JimpleNode(mockIfStmt(value))
                val instance = JimpleNode(mockIfStmt(value))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("equals statements with the same structure") {
                val template = JimpleNode(mockIfStmt(SimpleValue("shared")))
                val instance = JimpleNode(mockIfStmt(SimpleValue("shared")))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("does not equal statements with a different value type") {
                val template = JimpleNode(mockIfStmt(SimpleValue("template")))
                val instance = JimpleNode(mockIfStmt(SimpleValue("instance")))

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }

            it("does not equal statements of a different class") {
                val template = JimpleNode(mockIfStmt(SimpleValue("value")))
                val instance = JimpleNode(mockStmt())

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }

        context("switch statements") {
            it("equals itself") {
                val stmt = JimpleNode(mockSwitchStmt(SimpleValue("value")))

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("equals statements with the same values") {
                val value = SimpleValue("shared")
                val template = JimpleNode(mockSwitchStmt(value))
                val instance = JimpleNode(mockSwitchStmt(value))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("equals statements with the same structure") {
                val template = JimpleNode(mockSwitchStmt(SimpleValue("shared")))
                val instance = JimpleNode(mockSwitchStmt(SimpleValue("shared")))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("does not equal statements with a different value type") {
                val template = JimpleNode(mockSwitchStmt(SimpleValue("template")))
                val instance = JimpleNode(mockSwitchStmt(SimpleValue("instance")))

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }

            it("does not equal statements of a different class") {
                val template = JimpleNode(mockSwitchStmt(SimpleValue("value")))
                val instance = JimpleNode(mockStmt())

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }

        context("invoke statements") {
            it("equals itself") {
                val stmt = JimpleNode(mockInvokeStmt(SimpleInvokeExpr("value")))

                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("equals statements with the same values") {
                val value = SimpleInvokeExpr("shared")
                val template = JimpleNode(mockInvokeStmt(value))
                val instance = JimpleNode(mockInvokeStmt(value))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("equals statements with the same structure") {
                val template = JimpleNode(mockInvokeStmt(SimpleInvokeExpr("shared")))
                val instance = JimpleNode(mockInvokeStmt(SimpleInvokeExpr("shared")))

                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("does not equal statements with a different value type") {
                val template = JimpleNode(mockInvokeStmt(SimpleInvokeExpr("template")))
                val instance = JimpleNode(mockInvokeStmt(SimpleInvokeExpr("instance")))

                assertThat(comparator.satisfies(template, instance)).isFalse()
            }

            it("does not equal statements of a different class") {
                val template = JimpleNode(mockInvokeStmt(SimpleInvokeExpr("value")))
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
})
