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
import soot.jimple.DefinitionStmt
import soot.jimple.IfStmt
import soot.jimple.ReturnStmt
import soot.jimple.SwitchStmt
import soot.jimple.ThrowStmt

/**
 * Unit tests for [GeneralizedNodeComparator.generalizedValuesAreEqual].
 */
internal class GeneralizedNodeComparatorValueTest : Spek({
    lateinit var comparator: GeneralizedNodeComparator

    beforeEachTest {
        comparator = GeneralizedNodeComparator()
    }

    describe("generalized value comparison of statements") {
        context("bad weather cases") {
            it("throws an exception if a non-JimpleNode template is given") {
                val template = mock<Node> {}
                val instance = JimpleNode(mock {})

                assertThatThrownBy { comparator.generalizedValuesAreEqual(template, instance) }
                    .isExactlyInstanceOf(IllegalArgumentException::class.java)
                    .hasMessage("Jimple GeneralizedNodeComparator cannot handle non-Jimple nodes.")
            }

            it("throws an exception if a non-JimpleNode instance is given") {
                val template = JimpleNode(mock {})
                val instance = mock<Node> {}

                assertThatThrownBy { comparator.generalizedValuesAreEqual(template, instance) }
                    .isExactlyInstanceOf(IllegalArgumentException::class.java)
                    .hasMessage("Jimple GeneralizedNodeComparator cannot handle non-Jimple nodes.")
            }
        }

        context("(in)equality depends on the template") {
            it("copies tags to two instances") {
                val template = JimpleNode(mock<ThrowStmt> {
                    on { it.op } doReturn SimpleValue("shared")
                })

                val instanceA = JimpleNode(mock<ThrowStmt> {
                    on { it.op } doReturn SimpleValue("shared")
                })
                val instanceB = JimpleNode(mock<ThrowStmt> {
                    on { it.op } doReturn SimpleValue("shared")
                })

                assertThat(comparator.generalizedValuesAreEqual(template, instanceA)).isTrue()
                assertThat(comparator.generalizedValuesAreEqual(template, instanceB)).isTrue()
            }

            it("copies tags from non-finalised values") {
                val templateValue = SimpleValue("shared")
                val templateA = JimpleNode(mock<ThrowStmt> {
                    on { it.op } doReturn templateValue
                })
                val templateB = JimpleNode(mock<ThrowStmt> {
                    on { it.op } doReturn templateValue
                })

                val instanceValue = SimpleValue("shared")
                val instanceA = JimpleNode(mock<ThrowStmt> {
                    on { it.op } doReturn instanceValue
                })
                val instanceB = JimpleNode(mock<ThrowStmt> {
                    on { it.op } doReturn instanceValue
                })

                comparator.generalizedValuesAreEqual(templateA, instanceA)
                assertThat(comparator.generalizedValuesAreEqual(templateB, instanceB)).isTrue()
            }

            it("does not copy tags from finalised values") {
                val templateValue = SimpleValue("shared")
                val templateA = JimpleNode(mock<ThrowStmt> {
                    on { it.op } doReturn templateValue
                })
                val templateB = JimpleNode(mock<ThrowStmt> {
                    on { it.op } doReturn templateValue
                })

                val instanceA = JimpleNode(mock<ThrowStmt> {
                    on { it.op } doReturn SimpleValue("shared")
                })
                val instanceB = JimpleNode(mock<ThrowStmt> {
                    on { it.op } doReturn SimpleValue("shared")
                })

                comparator.generalizedValuesAreEqual(templateA, instanceA)
                assertThat(comparator.generalizedValuesAreEqual(templateB, instanceB)).isFalse()
            }

            it("is not transitive") {
                val template = JimpleNode(mock<ThrowStmt> {
                    on { it.op } doReturn SimpleValue("shared")
                })

                val instanceA = JimpleNode(mock<ThrowStmt> {
                    on { it.op } doReturn SimpleValue("shared")
                })
                val instanceB = JimpleNode(mock<ThrowStmt> {
                    on { it.op } doReturn SimpleValue("shared")
                })

                comparator.generalizedValuesAreEqual(template, instanceA)
                assertThat(comparator.generalizedValuesAreEqual(instanceA, instanceB)).isFalse()
            }

            it("copies tags for one value even if the other is already assigned") {
                val templateSharedValue = SimpleValue("shared")
                val templateA = JimpleNode(mock<IfStmt> {
                    on { it.condition } doReturn templateSharedValue
                })
                val templateB = JimpleNode(mock<DefinitionStmt> {
                    on { it.leftOp } doReturn templateSharedValue
                    on { it.rightOp } doReturn SimpleValue("shared")
                })

                val instanceSharedValue = SimpleValue("shared")
                val instanceA = JimpleNode(mock<IfStmt> {
                    on { it.condition } doReturn instanceSharedValue
                })
                val instanceB = JimpleNode(mock<DefinitionStmt> {
                    on { it.leftOp } doReturn instanceSharedValue
                    on { it.rightOp } doReturn SimpleValue("shared")
                })

                comparator.generalizedValuesAreEqual(templateA, instanceA)
                assertThat(comparator.generalizedValuesAreEqual(templateB, instanceB)).isTrue()
            }

            it("supports switching template side between statements") {
                val templateSharedValue = SimpleValue("shared")
                val templateA = JimpleNode(mock<SwitchStmt> {
                    on { it.key } doReturn templateSharedValue
                })
                val templateB = JimpleNode(mock<DefinitionStmt> {
                    on { it.leftOp } doReturn templateSharedValue
                    on { it.rightOp } doReturn SimpleValue("shared")
                })

                val instanceSharedValue = SimpleValue("shared")
                val instanceA = JimpleNode(mock<IfStmt> {
                    on { it.condition } doReturn instanceSharedValue
                })
                val instanceB = JimpleNode(mock<DefinitionStmt> {
                    on { it.leftOp } doReturn instanceSharedValue
                    on { it.rightOp } doReturn SimpleValue("shared")
                })

                comparator.generalizedValuesAreEqual(templateA, instanceA)
                assertThat(comparator.generalizedValuesAreEqual(instanceB, templateB)).isTrue()
            }

            it("rejects instances with tags where the template has none") {
                val fakeTemplate = JimpleNode(mock<ReturnStmt> {
                    on { it.op } doReturn SimpleValue("shared")
                })
                val realTemplate = JimpleNode(mock<ThrowStmt> {
                    on { it.op } doReturn SimpleValue("shared")
                })

                val instanceSharedValue = SimpleValue("shared")
                val instanceA = JimpleNode(mock<ReturnStmt> {
                    on { it.op } doReturn instanceSharedValue
                })
                val instanceB = JimpleNode(mock<ThrowStmt> {
                    on { it.op } doReturn instanceSharedValue
                })

                comparator.generalizedValuesAreEqual(fakeTemplate, instanceA)
                assertThat(comparator.generalizedValuesAreEqual(realTemplate, instanceB)).isFalse()
            }

            it("rejects instances with incorrect tags if the template is finalised") {
                val fakeTemplate = JimpleNode(mock<ReturnStmt> {
                    on { it.op } doReturn SimpleValue("shared")
                })

                val fakeInstance = JimpleNode(mock<ReturnStmt> {
                    on { it.op } doReturn SimpleValue("shared")
                })

                comparator.generalizedValuesAreEqual(fakeTemplate, fakeInstance)

                val realTemplateValue = SimpleValue("shared")
                val realUnfinalizedTemplate = JimpleNode(mock<ReturnStmt> {
                    on { it.op } doReturn realTemplateValue
                })
                val realFinalizedTemplate = JimpleNode(mock<ReturnStmt> {
                    on { it.op } doReturn realTemplateValue
                })

                val realInstanceValue = SimpleValue("shared")
                val realInstance = JimpleNode(mock<ReturnStmt> {
                    on { it.op } doReturn realInstanceValue
                })

                comparator.generalizedValuesAreEqual(realUnfinalizedTemplate, realInstance)

                assertThat(comparator.generalizedValuesAreEqual(realFinalizedTemplate, fakeInstance)).isFalse()
            }

            it("rejects instances with incorrect tags if the template is non-finalised") {
                val fakeTemplate = JimpleNode(mock<ReturnStmt> {
                    on { it.op } doReturn SimpleValue("shared")
                })
                val fakeInstance = JimpleNode(mock<ReturnStmt> {
                    on { it.op } doReturn SimpleValue("shared")
                })

                comparator.generalizedValuesAreEqual(fakeTemplate, fakeInstance)

                val realTemplate = JimpleNode(mock<ReturnStmt> {
                    on { it.op } doReturn SimpleValue("shared")
                })
                val realInstance = JimpleNode(mock<ReturnStmt> {
                    on { it.op } doReturn SimpleValue("shared")
                })

                comparator.generalizedValuesAreEqual(realTemplate, realInstance)

                assertThat(comparator.generalizedValuesAreEqual(realTemplate, fakeInstance)).isFalse()
            }
        }
    }
})
