package org.cafejojo.schaapi.usagegraphgenerator.compare

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.jimple.DefinitionStmt
import soot.jimple.IfStmt
import soot.jimple.ReturnStmt
import soot.jimple.Stmt
import soot.jimple.SwitchStmt
import soot.jimple.ThrowStmt

internal class GeneralizedStmtComparatorValueTest : Spek({
    lateinit var comparator: GeneralizedStmtComparator

    beforeEachTest {
        comparator = GeneralizedStmtComparator()
    }

    describe("generalized value comparison of statements") {
        context("(in)equality does not change for the same check") {
            it("finds equality when comparing reflexively") {
                val value = mockValue()
                val stmt = mock<ReturnStmt> {
                    on { it.op } doReturn value
                }

                comparator.satisfies(stmt, stmt)
                assertThat(comparator.satisfies(stmt, stmt)).isTrue()
            }

            it("finds equality for the same structure and value") {
                val value = mockValue()
                val template = mock<IfStmt> {
                    on { it.condition } doReturn value
                }
                val instance = mock<IfStmt> {
                    on { it.condition } doReturn value
                }

                comparator.satisfies(template, instance)
                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("finds equality for the same structure and *kind* of value") {
                val templateValue = mockValue()
                val template = mock<SwitchStmt> {
                    on { it.key } doReturn templateValue
                }

                val instanceValue = mockValue()
                val instance = mock<SwitchStmt> {
                    on { it.key } doReturn instanceValue
                }

                comparator.satisfies(template, instance)
                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("finds inequality for the same structure but a different kind of value") {
                val templateValue = mockTypedValue()
                val template = mock<ThrowStmt> {
                    on { it.op } doReturn templateValue
                }

                val instanceValue = mockTypedValue()
                val instance = mock<ThrowStmt> {
                    on { it.op } doReturn instanceValue
                }

                comparator.satisfies(template, instance)
                assertThat(comparator.satisfies(template, instance)).isFalse()
            }

            it("finds inequality for different structures") {
                val value = mockValue()
                val template = mock<ReturnStmt> {
                    on { it.op } doReturn value
                }

                val instance = mock<Stmt> {}

                comparator.satisfies(template, instance)
                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }

        context("(in)equality depends on the template") {
            it("copies tags to two instances") {
                val templateValue = mockValue()
                val template = mock<ThrowStmt> {
                    on { it.op } doReturn templateValue
                }

                val instanceAValue = mockValue()
                val instanceA = mock<ThrowStmt> {
                    on { it.op } doReturn instanceAValue
                }

                val instanceBValue = mockValue()
                val instanceB = mock<ThrowStmt> {
                    on { it.op } doReturn instanceBValue
                }

                assertThat(comparator.satisfies(template, instanceA)).isTrue()
                assertThat(comparator.satisfies(template, instanceB)).isTrue()
            }

            it("copies tags from non-finalised values") {
                val templateValue = mockValue()
                val templateA = mock<ThrowStmt> {
                    on { it.op } doReturn templateValue
                }
                val templateB = mock<ThrowStmt> {
                    on { it.op } doReturn templateValue
                }

                val instanceValue = mockValue()
                val instanceA = mock<ThrowStmt> {
                    on { it.op } doReturn instanceValue
                }
                val instanceB = mock<ThrowStmt> {
                    on { it.op } doReturn instanceValue
                }

                comparator.satisfies(templateA, instanceA)
                assertThat(comparator.satisfies(templateB, instanceB)).isTrue()
            }

            it("does not copy tags from finalised values") {
                val templateValue = mockValue()
                val templateA = mock<ThrowStmt> {
                    on { it.op } doReturn templateValue
                }
                val templateB = mock<ThrowStmt> {
                    on { it.op } doReturn templateValue
                }

                val instanceAValue = mockValue()
                val instanceBValue = mockValue()
                val instanceA = mock<ThrowStmt> {
                    on { it.op } doReturn instanceAValue
                }
                val instanceB = mock<ThrowStmt> {
                    on { it.op } doReturn instanceBValue
                }

                comparator.satisfies(templateA, instanceA)
                assertThat(comparator.satisfies(templateB, instanceB)).isFalse()
            }

            it("is not transitive") {
                val templateValue = mockValue()
                val template = mock<ThrowStmt> {
                    on { it.op } doReturn templateValue
                }

                val instanceAValue = mockValue()
                val instanceBValue = mockValue()
                val instanceA = mock<ThrowStmt> {
                    on { it.op } doReturn instanceAValue
                }
                val instanceB = mock<ThrowStmt> {
                    on { it.op } doReturn instanceBValue
                }

                comparator.satisfies(template, instanceA)
                assertThat(comparator.satisfies(instanceA, instanceB)).isFalse()
            }

            it("copies tags for one value even if the other is already assigned") {
                val templateLeftValue = mockValue()
                val templateRightValue = mockValue()
                val templateA = mock<IfStmt> {
                    on { it.condition } doReturn templateLeftValue
                }
                val templateB = mock<DefinitionStmt> {
                    on { it.leftOp } doReturn templateLeftValue
                    on { it.rightOp } doReturn templateRightValue
                }

                val instanceLeftValue = mockValue()
                val instanceRightValue = mockValue()
                val instanceA = mock<IfStmt> {
                    on { it.condition } doReturn instanceLeftValue
                }
                val instanceB = mock<DefinitionStmt> {
                    on { it.leftOp } doReturn instanceLeftValue
                    on { it.rightOp } doReturn instanceRightValue
                }

                comparator.satisfies(templateA, instanceA)
                assertThat(comparator.satisfies(templateB, instanceB)).isTrue()
            }

            it("supports switching template side between statements") {
                val templateLeftValue = mockValue()
                val templateRightValue = mockValue()
                val templateA = mock<SwitchStmt> {
                    on { it.key } doReturn templateLeftValue
                }
                val templateB = mock<DefinitionStmt> {
                    on { it.leftOp } doReturn templateLeftValue
                    on { it.rightOp } doReturn templateRightValue
                }

                val instanceLeftValue = mockValue()
                val instanceRightValue = mockValue()
                val instanceA = mock<IfStmt> {
                    on { it.condition } doReturn instanceLeftValue
                }
                val instanceB = mock<DefinitionStmt> {
                    on { it.leftOp } doReturn instanceLeftValue
                    on { it.rightOp } doReturn instanceRightValue
                }

                comparator.satisfies(templateA, instanceA)
                assertThat(comparator.satisfies(instanceB, templateB)).isTrue()
            }

            it("rejects instances with tags where the template has none") {
                val fakeTemplateValue = mockValue()
                val fakeTemplate = mock<ReturnStmt> {
                    on { it.op } doReturn fakeTemplateValue
                }
                val realTemplateValue = mockValue()
                val realTemplate = mock<ThrowStmt> {
                    on { it.op } doReturn realTemplateValue
                }

                val instanceValue = mockValue()
                val instanceA = mock<ReturnStmt> {
                    on { it.op } doReturn instanceValue
                }
                val instanceB = mock<ThrowStmt> {
                    on { it.op } doReturn instanceValue
                }

                comparator.satisfies(fakeTemplate, instanceA)
                assertThat(comparator.satisfies(realTemplate, instanceB)).isFalse()
            }

            it("rejects instances with incorrect tags if the template is finalised") {
                val fakeTemplateValue = mockValue()
                val fakeTemplate = mock<ReturnStmt> {
                    on { it.op } doReturn fakeTemplateValue
                }

                val fakeInstanceValue = mockValue()
                val fakeInstance = mock<ReturnStmt> {
                    on { it.op } doReturn fakeInstanceValue
                }

                comparator.satisfies(fakeTemplate, fakeInstance)

                val realTemplateValue = mockValue()
                val realUnfinalizedTemplate = mock<ReturnStmt> {
                    on { it.op } doReturn realTemplateValue
                }
                val realFinalizedTemplate = mock<ReturnStmt> {
                    on { it.op } doReturn realTemplateValue
                }

                val realInstanceValue = mockValue()
                val realInstance = mock<ReturnStmt> {
                    on { it.op } doReturn realInstanceValue
                }

                comparator.satisfies(realUnfinalizedTemplate, realInstance)

                assertThat(comparator.satisfies(realFinalizedTemplate, fakeInstance)).isFalse()
            }

            it("rejects instances with incorrect tags if the template is non-finalised") {
                val fakeTemplateValue = mockValue()
                val fakeTemplate = mock<ReturnStmt> {
                    on { it.op } doReturn fakeTemplateValue
                }

                val fakeInstanceValue = mockValue()
                val fakeInstance = mock<ReturnStmt> {
                    on { it.op } doReturn fakeInstanceValue
                }

                comparator.satisfies(fakeTemplate, fakeInstance)

                val realTemplateValue = mockValue()
                val realTemplate = mock<ReturnStmt> {
                    on { it.op } doReturn realTemplateValue
                }

                val realInstanceValue = mockValue()
                val realInstance = mock<ReturnStmt> {
                    on { it.op } doReturn realInstanceValue
                }

                comparator.satisfies(realTemplate, realInstance)

                assertThat(comparator.satisfies(realTemplate, fakeInstance)).isFalse()
            }
        }
    }
})
