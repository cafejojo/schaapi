package org.cafejojo.schaapi.usagegraphgenerator

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.Type
import soot.jimple.IntConstant
import soot.jimple.Jimple
import soot.jimple.StringConstant

internal class SootNodeTest : Spek({
    describe("when checking whether two sootnodes are equal") {
        it("should be equal if the values are in the same order and of the same type") {
            val type = mock<Type> {}
            val local1 = Jimple.v().newLocal("local1", type)
            val local2 = Jimple.v().newLocal("local2", type)

            val node1 = SootNode(
                Jimple.v().newAssignStmt(
                    local1,
                    Jimple.v().newAddExpr(IntConstant.v(10), IntConstant.v(20))
                )
            )

            val node2 = SootNode(
                Jimple.v().newAssignStmt(
                    local2,
                    Jimple.v().newAddExpr(IntConstant.v(10), IntConstant.v(20))
                )
            )

            assertThat(node1).isEqualTo(node2)
        }

        it("should have the same hashcode if they are equal") {
            val type = mock<Type> {}
            val local1 = Jimple.v().newLocal("local1", type)
            val local2 = Jimple.v().newLocal("local2", type)

            val node1 = SootNode(
                Jimple.v().newAssignStmt(
                    local1,
                    Jimple.v().newAddExpr(IntConstant.v(10), IntConstant.v(20))
                )
            )

            val node2 = SootNode(
                Jimple.v().newAssignStmt(
                    local2,
                    Jimple.v().newAddExpr(IntConstant.v(10), IntConstant.v(20))
                )
            )

            assertThat(node1.hashCode()).isEqualTo(node2.hashCode())
        }

        it("should not be equal if the values are not the same type") {
            val type = mock<Type> {}
            val local1 = Jimple.v().newLocal("local1", type)
            val local2 = Jimple.v().newLocal("local2", type)

            val node1 = SootNode(
                Jimple.v().newAssignStmt(
                    local1,
                    Jimple.v().newAddExpr(IntConstant.v(10), IntConstant.v(20))
                )
            )

            val node2 = SootNode(
                Jimple.v().newAssignStmt(
                    local2,
                    Jimple.v().newAddExpr(IntConstant.v(10), StringConstant.v("asdf"))
                )
            )

            assertThat(node1).isNotEqualTo(node2)
            assertThat(node1.hashCode()).isNotEqualTo(node2.hashCode())
        }

        it("should not have the same hashcode if they are not equal") {
            val type = mock<Type> {}
            val local1 = Jimple.v().newLocal("local1", type)
            val local2 = Jimple.v().newLocal("local2", type)

            val node1 = SootNode(
                Jimple.v().newAssignStmt(
                    local1,
                    Jimple.v().newAddExpr(IntConstant.v(10), IntConstant.v(20))
                )
            )

            val node2 = SootNode(
                Jimple.v().newAssignStmt(
                    local2,
                    Jimple.v().newAddExpr(
                        IntConstant.v(10), StringConstant.v("asdf")
                    )
                )
            )

            assertThat(node1.hashCode()).isNotEqualTo(node2.hashCode())
        }
    }
})
