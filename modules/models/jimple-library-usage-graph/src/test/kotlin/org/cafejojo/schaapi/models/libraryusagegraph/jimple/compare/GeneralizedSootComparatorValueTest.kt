package org.cafejojo.schaapi.models.libraryusagegraph.jimple.compare

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.cafejojo.schaapi.common.Node
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.SootNode
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

internal class GeneralizedSootComparatorValueTest : Spek({
    lateinit var comparator: GeneralizedSootComparator

    beforeEachTest {
        comparator = GeneralizedSootComparator()
    }

    describe("generalized value comparison of statements") {
        context("bad weather cases") {
            it("throws an exception if a non-SootNode template is given") {
                val template = mock<Node> {}
                val instance = SootNode(mock<Stmt> {})

                assertThatThrownBy { comparator.generalizedValuesAreEqual(template, instance) }
                    .isExactlyInstanceOf(IllegalArgumentException::class.java)
                    .hasMessage("GeneralizedSootComparator cannot handle non-SootNodes.")
            }

            it("throws an exception if a non-SootNode instance is given") {
                val template = SootNode(mock<Stmt> {})
                val instance = mock<Node> {}

                assertThatThrownBy { comparator.generalizedValuesAreEqual(template, instance) }
                    .isExactlyInstanceOf(IllegalArgumentException::class.java)
                    .hasMessage("GeneralizedSootComparator cannot handle non-SootNodes.")
            }
        }

        context("(in)equality does not change for the same check") {
            it("finds equality when comparing reflexively") {
                val value = mockValue()
                val node = SootNode(mock<ReturnStmt> {
                    on { it.op } doReturn value
                })

                comparator.satisfies(node, node)
                assertThat(comparator.satisfies(node, node)).isTrue()
            }

            it("finds equality for the same structure and value") {
                val value = mockValue()
                val template = SootNode(mock<IfStmt> {
                    on { it.condition } doReturn value
                })
                val instance = SootNode(mock<IfStmt> {
                    on { it.condition } doReturn value
                })

                comparator.satisfies(template, instance)
                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("finds equality for the same structure and *kind* of value") {
                val templateValue = mockValue()
                val template = SootNode(mock<SwitchStmt> {
                    on { it.key } doReturn templateValue
                })

                val instanceValue = mockValue()
                val instance = SootNode(mock<SwitchStmt> {
                    on { it.key } doReturn instanceValue
                })

                comparator.satisfies(template, instance)
                assertThat(comparator.satisfies(template, instance)).isTrue()
            }

            it("finds inequality for the same structure but a different kind of value") {
                val templateValue = mockTypedValue()
                val template = SootNode(mock<ThrowStmt> {
                    on { it.op } doReturn templateValue
                })

                val instanceValue = mockTypedValue()
                val instance = SootNode(mock<ThrowStmt> {
                    on { it.op } doReturn instanceValue
                })

                comparator.satisfies(template, instance)
                assertThat(comparator.satisfies(template, instance)).isFalse()
            }

            it("finds inequality for different structures") {
                val value = mockValue()
                val template = SootNode(mock<ReturnStmt> {
                    on { it.op } doReturn value
                })

                val instance = SootNode(mock<Stmt> {})

                comparator.satisfies(template, instance)
                assertThat(comparator.satisfies(template, instance)).isFalse()
            }
        }

        context("(in)equality depends on the template") {
            it("copies tags to two instances") {
                val templateValue = mockValue()
                val template = SootNode(mock<ThrowStmt> {
                    on { it.op } doReturn templateValue
                })

                val instanceAValue = mockValue()
                val instanceA = SootNode(mock<ThrowStmt> {
                    on { it.op } doReturn instanceAValue
                })

                val instanceBValue = mockValue()
                val instanceB = SootNode(mock<ThrowStmt> {
                    on { it.op } doReturn instanceBValue
                })

                assertThat(comparator.satisfies(template, instanceA)).isTrue()
                assertThat(comparator.satisfies(template, instanceB)).isTrue()
            }

            it("copies tags from non-finalised values") {
                val templateValue = mockValue()
                val templateA = SootNode(mock<ThrowStmt> {
                    on { it.op } doReturn templateValue
                })
                val templateB = SootNode(mock<ThrowStmt> {
                    on { it.op } doReturn templateValue
                })

                val instanceValue = mockValue()
                val instanceA = SootNode(mock<ThrowStmt> {
                    on { it.op } doReturn instanceValue
                })
                val instanceB = SootNode(mock<ThrowStmt> {
                    on { it.op } doReturn instanceValue
                })

                comparator.satisfies(templateA, instanceA)
                assertThat(comparator.satisfies(templateB, instanceB)).isTrue()
            }

            it("does not copy tags from finalised values") {
                val templateValue = mockValue()
                val templateA = SootNode(mock<ThrowStmt> {
                    on { it.op } doReturn templateValue
                })
                val templateB = SootNode(mock<ThrowStmt> {
                    on { it.op } doReturn templateValue
                })

                val instanceAValue = mockValue()
                val instanceBValue = mockValue()
                val instanceA = SootNode(mock<ThrowStmt> {
                    on { it.op } doReturn instanceAValue
                })
                val instanceB = SootNode(mock<ThrowStmt> {
                    on { it.op } doReturn instanceBValue
                })

                comparator.satisfies(templateA, instanceA)
                assertThat(comparator.satisfies(templateB, instanceB)).isFalse()
            }

            it("is not transitive") {
                val templateValue = mockValue()
                val template = SootNode(mock<ThrowStmt> {
                    on { it.op } doReturn templateValue
                })

                val instanceAValue = mockValue()
                val instanceBValue = mockValue()
                val instanceA = SootNode(mock<ThrowStmt> {
                    on { it.op } doReturn instanceAValue
                })
                val instanceB = SootNode(mock<ThrowStmt> {
                    on { it.op } doReturn instanceBValue
                })

                comparator.satisfies(template, instanceA)
                assertThat(comparator.satisfies(instanceA, instanceB)).isFalse()
            }

            it("copies tags for one value even if the other is already assigned") {
                val templateLeftValue = mockValue()
                val templateRightValue = mockValue()
                val templateA = SootNode(mock<IfStmt> {
                    on { it.condition } doReturn templateLeftValue
                })
                val templateB = SootNode(mock<DefinitionStmt> {
                    on { it.leftOp } doReturn templateLeftValue
                    on { it.rightOp } doReturn templateRightValue
                })

                val instanceLeftValue = mockValue()
                val instanceRightValue = mockValue()
                val instanceA = SootNode(mock<IfStmt> {
                    on { it.condition } doReturn instanceLeftValue
                })
                val instanceB = SootNode(mock<DefinitionStmt> {
                    on { it.leftOp } doReturn instanceLeftValue
                    on { it.rightOp } doReturn instanceRightValue
                })

                comparator.satisfies(templateA, instanceA)
                assertThat(comparator.satisfies(templateB, instanceB)).isTrue()
            }

            it("supports switching template side between statements") {
                val templateLeftValue = mockValue()
                val templateRightValue = mockValue()
                val templateA = SootNode(mock<SwitchStmt> {
                    on { it.key } doReturn templateLeftValue
                })
                val templateB = SootNode(mock<DefinitionStmt> {
                    on { it.leftOp } doReturn templateLeftValue
                    on { it.rightOp } doReturn templateRightValue
                })

                val instanceLeftValue = mockValue()
                val instanceRightValue = mockValue()
                val instanceA = SootNode(mock<IfStmt> {
                    on { it.condition } doReturn instanceLeftValue
                })
                val instanceB = SootNode(mock<DefinitionStmt> {
                    on { it.leftOp } doReturn instanceLeftValue
                    on { it.rightOp } doReturn instanceRightValue
                })

                comparator.satisfies(templateA, instanceA)
                assertThat(comparator.satisfies(instanceB, templateB)).isTrue()
            }

            it("rejects instances with tags where the template has none") {
                val fakeTemplateValue = mockValue()
                val fakeTemplate = SootNode(mock<ReturnStmt> {
                    on { it.op } doReturn fakeTemplateValue
                })
                val realTemplateValue = mockValue()
                val realTemplate = SootNode(mock<ThrowStmt> {
                    on { it.op } doReturn realTemplateValue
                })

                val instanceValue = mockValue()
                val instanceA = SootNode(mock<ReturnStmt> {
                    on { it.op } doReturn instanceValue
                })
                val instanceB = SootNode(mock<ThrowStmt> {
                    on { it.op } doReturn instanceValue
                })

                comparator.satisfies(fakeTemplate, instanceA)
                assertThat(comparator.satisfies(realTemplate, instanceB)).isFalse()
            }

            it("rejects instances with incorrect tags if the template is finalised") {
                val fakeTemplateValue = mockValue()
                val fakeTemplate = SootNode(mock<ReturnStmt> {
                    on { it.op } doReturn fakeTemplateValue
                })

                val fakeInstanceValue = mockValue()
                val fakeInstance = SootNode(mock<ReturnStmt> {
                    on { it.op } doReturn fakeInstanceValue
                })

                comparator.satisfies(fakeTemplate, fakeInstance)

                val realTemplateValue = mockValue()
                val realUnfinalizedTemplate = SootNode(mock<ReturnStmt> {
                    on { it.op } doReturn realTemplateValue
                })
                val realFinalizedTemplate = SootNode(mock<ReturnStmt> {
                    on { it.op } doReturn realTemplateValue
                })

                val realInstanceValue = mockValue()
                val realInstance = SootNode(mock<ReturnStmt> {
                    on { it.op } doReturn realInstanceValue
                })

                comparator.satisfies(realUnfinalizedTemplate, realInstance)

                assertThat(comparator.satisfies(realFinalizedTemplate, fakeInstance)).isFalse()
            }

            it("rejects instances with incorrect tags if the template is non-finalised") {
                val fakeTemplateValue = mockValue()
                val fakeTemplate = SootNode(mock<ReturnStmt> {
                    on { it.op } doReturn fakeTemplateValue
                })

                val fakeInstanceValue = mockValue()
                val fakeInstance = SootNode(mock<ReturnStmt> {
                    on { it.op } doReturn fakeInstanceValue
                })

                comparator.satisfies(fakeTemplate, fakeInstance)

                val realTemplateValue = mockValue()
                val realTemplate = SootNode(mock<ReturnStmt> {
                    on { it.op } doReturn realTemplateValue
                })

                val realInstanceValue = mockValue()
                val realInstance = SootNode(mock<ReturnStmt> {
                    on { it.op } doReturn realInstanceValue
                })

                comparator.satisfies(realTemplate, realInstance)

                assertThat(comparator.satisfies(realTemplate, fakeInstance)).isFalse()
            }
        }
    }
})
