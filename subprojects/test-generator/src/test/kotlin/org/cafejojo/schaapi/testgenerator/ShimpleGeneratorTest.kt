package org.cafejojo.schaapi.testgenerator

import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.usagegraphgenerator.SootNode
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.BooleanType
import soot.CharType
import soot.IntType
import soot.Modifier
import soot.Scene
import soot.SootClass
import soot.jimple.IntConstant
import soot.jimple.Jimple
import soot.jimple.StringConstant

internal class ShimpleGeneratorTest : Spek({
    describe("When passed a list of nodes") {
        it("should generate a method with no parameters if all variables are bound") {
            val a = Jimple.v().newLocal("a", CharType.v())
            val b = Jimple.v().newLocal("b", CharType.v())
            val c = Jimple.v().newLocal("c", CharType.v())

            val assignA = Jimple.v().newAssignStmt(a, StringConstant.v("hello"))
            val assignB = Jimple.v().newAssignStmt(b, StringConstant.v("world"))
            val assignC = Jimple.v().newAssignStmt(c, Jimple.v().newAddExpr(a, b))

            val node3 = SootNode(assignC, mutableListOf())
            val node2 = SootNode(assignB, mutableListOf(node3))
            val node1 = SootNode(assignA, mutableListOf(node2))

            val sClass = SootClass("asdf", Modifier.PUBLIC)
            Scene.v().addClass(sClass)
            val shimpleMethod = ShimpleGenerator(sClass)
                .generateShimpleMethod("method", listOf(node1, node2, node3))

            assertThat(shimpleMethod.parameterCount).isZero()
        }

        it("should generate a method with variable that is bound only later as parameter") {
            val a = Jimple.v().newLocal("a", IntType.v())
            val b = Jimple.v().newLocal("b", IntType.v())
            val c = Jimple.v().newLocal("c", IntType.v())

            val assignA = Jimple.v().newAssignStmt(a, IntConstant.v(10))
            val assignC = Jimple.v().newAssignStmt(c, Jimple.v().newAddExpr(a, b))
            val assignB = Jimple.v().newAssignStmt(b, IntConstant.v(20))

            val node3 = SootNode(assignC, mutableListOf())
            val node1 = SootNode(assignA, mutableListOf(node3))
            val node2 = SootNode(assignB, mutableListOf(node1))

            val sClass = SootClass("class", Modifier.PUBLIC)
            Scene.v().addClass(sClass)
            val shimpleMethod = ShimpleGenerator(sClass)
                .generateShimpleMethod("method", listOf(node1, node3, node2))

            assertThat(shimpleMethod.parameterCount).isOne()
        }

        it("should generate a method with the unbound variable as parameter") {
            val a = Jimple.v().newLocal("a", IntType.v())
            val b = Jimple.v().newLocal("b", IntType.v())
            val c = Jimple.v().newLocal("c", IntType.v())

            val assignC = Jimple.v().newAssignStmt(c, Jimple.v().newAddExpr(a, b))

            val node = SootNode(assignC, mutableListOf())

            val sClass = SootClass("class", Modifier.PUBLIC)
            val shimpleMethod = ShimpleGenerator(sClass)
                .generateShimpleMethod("method", listOf(node))

            assertThat(shimpleMethod.parameterCount).isEqualTo(2)
            assertThat(shimpleMethod.parameterTypes).isEqualTo(listOf(IntType.v(), IntType.v()))
        }

        it("should generate a method with the unbound variable as parameter") {
            val a = Jimple.v().newLocal("a", IntType.v())
            val b = Jimple.v().newLocal("b", IntType.v())
            val c = Jimple.v().newLocal("c", IntType.v())

            val assignC = Jimple.v().newAssignStmt(c, Jimple.v().newAddExpr(a, b))

            val node = SootNode(assignC, mutableListOf())

            val sClass = SootClass("class", Modifier.PUBLIC)
            val shimpleMethod = ShimpleGenerator(sClass)
                .generateShimpleMethod("method", listOf(node))

            assertThat(shimpleMethod.parameterCount).isEqualTo(2)
            assertThat(shimpleMethod.parameterTypes).isEqualTo(listOf(IntType.v(), IntType.v()))
        }

        it("should generate a method with the locals for the parameters") {
            val a = Jimple.v().newLocal("a", BooleanType.v())
            val b = Jimple.v().newLocal("b", BooleanType.v())
            val c = Jimple.v().newLocal("c", BooleanType.v())

            val assignC = Jimple.v().newAssignStmt(c, Jimple.v().newAndExpr(a, b))

            val node = SootNode(assignC, mutableListOf())

            val sClass = SootClass("class", Modifier.PUBLIC)
            val shimpleMethod = ShimpleGenerator(sClass)
                .generateShimpleMethod("method", listOf(node))

            assertThat(shimpleMethod.activeBody.parameterLocals.map { it.name }).isEqualTo(listOf(a.name, b.name))
        }

        it("should generate a method with the all locals") {
            val a = Jimple.v().newLocal("a", BooleanType.v())
            val b = Jimple.v().newLocal("b", BooleanType.v())
            val c = Jimple.v().newLocal("c", BooleanType.v())

            val assignC = Jimple.v().newAssignStmt(c, Jimple.v().newAndExpr(a, b))

            val node = SootNode(assignC, mutableListOf())

            val sClass = SootClass("class", Modifier.PUBLIC)
            val shimpleMethod = ShimpleGenerator(sClass)
                .generateShimpleMethod("method", listOf(node))

            assertThat(shimpleMethod.activeBody.locals.map { it.name }).containsExactly(a.name, b.name, c.name)
        }
    }
})
