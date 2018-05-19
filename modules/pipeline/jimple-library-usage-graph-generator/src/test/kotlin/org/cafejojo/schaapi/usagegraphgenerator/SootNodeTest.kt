package org.cafejojo.schaapi.usagegraphgenerator

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.Type
import soot.Value
import soot.jimple.DefinitionStmt

internal class SootNodeTest : Spek({
    describe("when checking whether two Soot nodes are equal") {
        fun mockDefinitionStmt(leftType: Type, rightType: Type): SootNode {
            val leftOp = mock<Value> { on { it.type } doReturn leftType }
            val rightOp = mock<Value> { on { it.type } doReturn rightType }

            return SootNode(mock<DefinitionStmt> {
                on { it.leftOp } doReturn leftOp
                on { it.rightOp } doReturn rightOp
            })
        }

        it("should be equal if the values are in the same order and of the same type") {
            val type1 = mock<Type> {}
            val type2 = mock<Type> {}

            val node1 = mockDefinitionStmt(type1, type2)
            val node2 = mockDefinitionStmt(type1, type2)

            assertThat(node1).isEqualTo(node2)
        }

        it("should have the same hashcode if they are equal") {
            val type1 = mock<Type> {}
            val type2 = mock<Type> {}

            val node1 = mockDefinitionStmt(type1, type2)
            val node2 = mockDefinitionStmt(type1, type2)

            assertThat(node1.hashCode()).isEqualTo(node2.hashCode())
        }

        it("should not be equal if the values are not the same type") {
            val type1 = mock<Type> {}
            val type2 = mock<Type> {}
            val type3 = mock<Type> {}

            val node1 = mockDefinitionStmt(type1, type2)
            val node2 = mockDefinitionStmt(type1, type3)

            assertThat(node1).isNotEqualTo(node2)
        }

        it("should not have the same hashcode if they are not equal") {
            val type1 = mock<Type> {}
            val type2 = mock<Type> {}
            val type3 = mock<Type> {}

            val node1 = mockDefinitionStmt(type1, type2)
            val node2 = mockDefinitionStmt(type1, type3)

            assertThat(node1.hashCode()).isNotEqualTo(node2.hashCode())
        }

        it("should not have the same hashcode if they have the same value types but in a different order") {
            val type1 = mock<Type> {}
            val type2 = mock<Type> {}

            val node1 = mockDefinitionStmt(type1, type2)
            val node2 = mockDefinitionStmt(type2, type1)

            assertThat(node1.hashCode()).isNotEqualTo(node2.hashCode())
        }
    }
})
