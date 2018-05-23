package org.cafejojo.schaapi.models.libraryusagegraph.jimple

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.cafejojo.schaapi.models.Node
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

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

        context("(in)equality depends on the template") {
            it("copies tags to two instances") {
                val template = JimpleNode(mockThrowStmt(mockValue("shared")))
                val instanceA = JimpleNode(mockThrowStmt(mockValue("shared")))
                val instanceB = JimpleNode(mockThrowStmt(mockValue("shared")))

                assertThat(comparator.generalizedValuesAreEqual(template, instanceA)).isTrue()
                assertThat(comparator.generalizedValuesAreEqual(template, instanceB)).isTrue()
            }

            it("copies tags from non-finalised values") {
                val templateValue = mockValue("shared")
                val templateA = JimpleNode(mockThrowStmt(templateValue))
                val templateB = JimpleNode(mockThrowStmt(templateValue))

                val instanceValue = mockValue("shared")
                val instanceA = JimpleNode(mockThrowStmt(instanceValue))
                val instanceB = JimpleNode(mockThrowStmt(instanceValue))

                comparator.generalizedValuesAreEqual(templateA, instanceA)
                assertThat(comparator.generalizedValuesAreEqual(templateB, instanceB)).isTrue()
            }

            it("does not copy tags from finalised values") {
                val templateValue = mockValue("shared")
                val templateA = JimpleNode(mockThrowStmt(templateValue))
                val templateB = JimpleNode(mockThrowStmt(templateValue))

                val instanceA = JimpleNode(mockThrowStmt(mockValue("shared")))
                val instanceB = JimpleNode(mockThrowStmt(mockValue("shared")))

                comparator.generalizedValuesAreEqual(templateA, instanceA)
                assertThat(comparator.generalizedValuesAreEqual(templateB, instanceB)).isFalse()
            }

            it("is not transitive") {
                val template = JimpleNode(mockThrowStmt(mockValue("shared")))

                val instanceA = JimpleNode(mockThrowStmt(mockValue("shared")))
                val instanceB = JimpleNode(mockThrowStmt(mockValue("shared")))

                comparator.generalizedValuesAreEqual(template, instanceA)
                assertThat(comparator.generalizedValuesAreEqual(instanceA, instanceB)).isFalse()
            }

            it("copies tags for one value even if the other is already assigned") {
                val templateSharedValue = mockValue("shared")
                val templateA = JimpleNode(mockIfStmt(templateSharedValue))
                val templateB = JimpleNode(mockDefinitionStmt(templateSharedValue, mockValue("shared")))

                val instanceSharedValue = mockValue("shared")
                val instanceA = JimpleNode(mockIfStmt(instanceSharedValue))
                val instanceB = JimpleNode(mockDefinitionStmt(instanceSharedValue, mockValue("shared")))

                comparator.generalizedValuesAreEqual(templateA, instanceA)
                assertThat(comparator.generalizedValuesAreEqual(templateB, instanceB)).isTrue()
            }

            it("supports switching template side between statements") {
                val templateSharedValue = mockValue("shared")
                val templateA = JimpleNode(mockSwitchStmt(templateSharedValue))
                val templateB = JimpleNode(mockDefinitionStmt(templateSharedValue, mockValue("shared")))

                val instanceSharedValue = mockValue("shared")
                val instanceA = JimpleNode(mockIfStmt(instanceSharedValue))
                val instanceB = JimpleNode(mockDefinitionStmt(instanceSharedValue, mockValue("shared")))

                comparator.generalizedValuesAreEqual(templateA, instanceA)
                assertThat(comparator.generalizedValuesAreEqual(instanceB, templateB)).isTrue()
            }

            it("rejects instances with tags where the template has none") {
                val fakeTemplate = JimpleNode(mockReturnStmt(mockValue("shared")))
                val realTemplate = JimpleNode(mockThrowStmt(mockValue("shared")))

                val instanceSharedValue = mockValue("shared")
                val instanceA = JimpleNode(mockReturnStmt(instanceSharedValue))
                val instanceB = JimpleNode(mockThrowStmt(instanceSharedValue))

                comparator.generalizedValuesAreEqual(fakeTemplate, instanceA)
                assertThat(comparator.generalizedValuesAreEqual(realTemplate, instanceB)).isFalse()
            }

            it("rejects instances with incorrect tags if the template is finalised") {
                val fakeTemplate = JimpleNode(mockReturnStmt(mockValue("shared")))
                val fakeInstance = JimpleNode(mockReturnStmt(mockValue("shared")))

                comparator.generalizedValuesAreEqual(fakeTemplate, fakeInstance)

                val realTemplateValue = mockValue("shared")
                val realUnfinalizedTemplate = JimpleNode(mockReturnStmt(realTemplateValue))
                val realFinalizedTemplate = JimpleNode(mockReturnStmt(realTemplateValue))

                val realInstanceValue = mockValue("shared")
                val realInstance = JimpleNode(mockReturnStmt(realInstanceValue))

                comparator.generalizedValuesAreEqual(realUnfinalizedTemplate, realInstance)

                assertThat(comparator.generalizedValuesAreEqual(realFinalizedTemplate, fakeInstance)).isFalse()
            }

            it("rejects instances with incorrect tags if the template is non-finalised") {
                val fakeTemplate = JimpleNode(mockReturnStmt(mockValue("shared")))
                val fakeInstance = JimpleNode(mockReturnStmt(mockValue("shared")))

                comparator.generalizedValuesAreEqual(fakeTemplate, fakeInstance)

                val realTemplate = JimpleNode(mockReturnStmt(mockValue("shared")))
                val realInstance = JimpleNode(mockReturnStmt(mockValue("shared")))

                comparator.generalizedValuesAreEqual(realTemplate, realInstance)

                assertThat(comparator.generalizedValuesAreEqual(realTemplate, fakeInstance)).isFalse()
            }
        }
    }
})
