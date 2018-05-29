package org.cafejojo.schaapi.models.libraryusagegraph.jimple

import org.assertj.core.api.Assertions.assertThat
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

        context("(in)equality in recursive nodes") {
            it("allows reusing values that were nested before") {
                val templateLeft = mockValue("left")
                val templateRight = mockValue("right")
                val instanceLeft = mockValue("left")
                val instanceRight = mockValue("right")

                val templateA = JimpleNode(mockIfStmt(SimpleBinopExpr(templateLeft, templateRight)))
                val instanceA = JimpleNode(mockIfStmt(SimpleBinopExpr(instanceLeft, instanceRight)))
                comparator.generalizedValuesAreEqual(templateA, instanceA)

                val templateB = JimpleNode(mockIfStmt(SimpleUnopExpr(templateLeft)))
                val instanceB = JimpleNode(mockIfStmt(SimpleUnopExpr(instanceLeft)))
                assertThat(comparator.generalizedValuesAreEqual(templateB, instanceB)).isTrue()
            }

            it("allows reusing values that contain nested values") {
                val templateLeft = mockValue("left")
                val templateRight = mockValue("right")
                val templateExpr = SimpleBinopExpr(templateLeft, templateRight)
                val instanceLeft = mockValue("left")
                val instanceRight = mockValue("right")
                val instanceExpr = SimpleBinopExpr(instanceLeft, instanceRight)

                val templateA = JimpleNode(mockIfStmt(templateExpr))
                val instanceA = JimpleNode(mockIfStmt(instanceExpr))
                comparator.generalizedValuesAreEqual(templateA, instanceA)

                val templateB = JimpleNode(mockDefinitionStmt(templateExpr, templateLeft))
                val instanceB = JimpleNode(mockDefinitionStmt(instanceExpr, instanceLeft))
                assertThat(comparator.generalizedValuesAreEqual(templateB, instanceB)).isTrue()
            }

            it("rejects wrongly used nested values") {
                val templateLeft = mockValue("left")
                val templateRight = mockValue("right")
                val templateExpr = SimpleBinopExpr(templateLeft, templateRight)
                val instanceLeft = mockValue("left")
                val instanceRight = mockValue("right")
                val instanceExpr = SimpleBinopExpr(instanceLeft, instanceRight)

                val templateA = JimpleNode(mockIfStmt(templateExpr))
                val instanceA = JimpleNode(mockIfStmt(instanceExpr))
                comparator.generalizedValuesAreEqual(templateA, instanceA)

                val templateB = JimpleNode(mockDefinitionStmt(templateExpr, templateLeft))
                val instanceB = JimpleNode(mockDefinitionStmt(instanceExpr, instanceRight))
                assertThat(comparator.generalizedValuesAreEqual(templateB, instanceB)).isFalse()
            }

            it("allows method instances to be reused across statements") {
                val method = SimpleSootMethod("base", listOf("arg1", "arg2"), "output")

                val expr1A = SimpleInvokeExpr("base", method, "arg1", "arg2")
                val expr1B = SimpleInvokeExpr("base", method, "arg1", "arg2")
                val node1A = JimpleNode(mockInvokeStmt(expr1A))
                val node1B = JimpleNode(mockInvokeStmt(expr1B))
                comparator.generalizedValuesAreEqual(node1A, node1B)

                val expr2A = SimpleInvokeExpr("base", method, "arg1", "arg2")
                val expr2B = SimpleInvokeExpr("base", method, "arg1", "arg2")
                val node2A = JimpleNode(mockInvokeStmt(expr2A))
                val node2B = JimpleNode(mockInvokeStmt(expr2B))
                assertThat(comparator.generalizedValuesAreEqual(node2A, node2B)).isTrue()
            }
        }
    }
})
