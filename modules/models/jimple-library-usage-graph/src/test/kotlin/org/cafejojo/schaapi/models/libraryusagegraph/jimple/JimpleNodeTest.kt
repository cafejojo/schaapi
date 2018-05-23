package org.cafejojo.schaapi.models.libraryusagegraph.jimple

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.jimple.DefinitionStmt

internal class JimpleNodeTest : Spek({
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
