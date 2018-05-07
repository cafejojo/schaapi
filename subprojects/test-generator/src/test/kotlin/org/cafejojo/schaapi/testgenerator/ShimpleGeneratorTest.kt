package org.cafejojo.schaapi.testgenerator

import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.usagegraphgenerator.SootNode
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.Modifier
import soot.SootClass
import soot.jimple.IntConstant
import soot.jimple.Jimple
import soot.jimple.toolkits.typing.fast.Integer1Type

internal class ShimpleGeneratorTest : Spek({
    describe("When passed a list of nodes") {
        it("should generate a method with no parameters if all variables are bound") {
            val a = Jimple.v().newLocal("a", Integer1Type.v())
            val b = Jimple.v().newLocal("b", Integer1Type.v())
            val c = Jimple.v().newLocal("c", Integer1Type.v())

            val assignA = Jimple.v().newAssignStmt(a, IntConstant.v(10))
            val assignB = Jimple.v().newAssignStmt(b, IntConstant.v(20))
            val assignC = Jimple.v().newAssignStmt(c, Jimple.v().newAddExpr(a, b))

            val node3 = SootNode(assignC, mutableListOf())
            val node2 = SootNode(assignB, mutableListOf(node3))
            val node1 = SootNode(assignA, mutableListOf(node2))

            val shimpleMethod = ShimpleGenerator(
                SootClass("asdf", Modifier.PUBLIC),
                listOf(node1, node2, node3)
            ).generateShimple()

            assertThat(shimpleMethod.parameterCount).isZero()
        }

        it("should generate a method with the unbound variable as parameter") {
            val a = Jimple.v().newLocal("a", Integer1Type.v())
            val b = Jimple.v().newLocal("b", Integer1Type.v())
            val c = Jimple.v().newLocal("c", Integer1Type.v())

            val assignC = Jimple.v().newAssignStmt(c, Jimple.v().newAddExpr(a, b))

            val node = SootNode(assignC, mutableListOf())

            val shimpleMethod = ShimpleGenerator(
                SootClass("asdf", Modifier.PUBLIC),
                listOf(node)
            ).generateShimple()

            assertThat(shimpleMethod.parameterCount).isEqualTo(2)
            assertThat(shimpleMethod.parameterTypes).isEqualTo(listOf(Integer1Type.v(), Integer1Type.v()))
        }
    }
})
