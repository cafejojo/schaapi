package org.cafejojo.schaapi.models.libraryusagegraph.jimple

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.jimple.DefinitionStmt

internal class JimpleNodeTest : Spek({
    describe("top-level values") {
        it("returns the operator of a throw statement") {
            val value = mockValue("value")
            val node = JimpleNode(mockIfStmt(value))

            assertThat(node.getTopLevelValues()).containsExactly(value)
        }

        it("returns both operators of a definition statement") {
            val leftValue = mockValue("left")
            val rightValue = mockValue("right")
            val node = JimpleNode(mockDefinitionStmt(leftValue, rightValue))

            assertThat(node.getTopLevelValues()).containsExactly(leftValue, rightValue)
        }

        it("returns the condition of an if statement") {
            val value = mockValue("value")
            val node = JimpleNode(mockIfStmt(value))

            assertThat(node.getTopLevelValues()).containsExactly(value)
        }

        it("returns the key of a switch statement") {
            val value = mockValue("value")
            val node = JimpleNode(mockSwitchStmt(value))

            assertThat(node.getTopLevelValues()).containsExactly(value)
        }

        it("returns the key of a switch statement") {
            val invokeExpr = mockInvokeExpr("value")
            val node = JimpleNode(mockInvokeStmt(invokeExpr))

            assertThat(node.getTopLevelValues()).containsExactly(invokeExpr)
        }

        it("returns the operator of a return statement") {
            val value = mockValue("value")
            val node = JimpleNode(mockReturnStmt(value))

            assertThat(node.getTopLevelValues()).containsExactly(value)
        }

        it("returns nothing for a goto statement") {
            val node = JimpleNode(mockGotoStmt())

            assertThat(node.getTopLevelValues()).isEmpty()
        }

        it("returns nothing for a return void statement") {
            val node = JimpleNode(mockReturnVoidStmt())

            assertThat(node.getTopLevelValues()).isEmpty()
        }

        it("returns nothing for an unrecognised statement") {
            val node = JimpleNode(mockStmt())

            assertThat(node.getTopLevelValues()).isEmpty()
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

        it("should be equal if the values are in the same order and of the same type") {
            val node1 = mockDefinitionStmt("left", "right")
            val node2 = mockDefinitionStmt("left", "right")

            assertThat(node1).isEqualTo(node2)
        }

        it("should have the same hashcode if they are equal") {
            val node1 = mockDefinitionStmt("left", "right")
            val node2 = mockDefinitionStmt("left", "right")

            assertThat(node1.hashCode()).isEqualTo(node2.hashCode())
        }

        it("should not be equal if the values are not the same type") {
            val node1 = mockDefinitionStmt("left", "right-a")
            val node2 = mockDefinitionStmt("left", "right-b")

            assertThat(node1).isNotEqualTo(node2)
        }

        it("should not have the same hashcode if they are not equal") {
            val node1 = mockDefinitionStmt("left", "right-a")
            val node2 = mockDefinitionStmt("left", "right-b")

            assertThat(node1.hashCode()).isNotEqualTo(node2.hashCode())
        }

        it("should not have the same hashcode if they have the same value types but in a different order") {
            val node1 = mockDefinitionStmt("left", "right")
            val node2 = mockDefinitionStmt("right", "left")

            assertThat(node1.hashCode()).isNotEqualTo(node2.hashCode())
        }
    }
})
