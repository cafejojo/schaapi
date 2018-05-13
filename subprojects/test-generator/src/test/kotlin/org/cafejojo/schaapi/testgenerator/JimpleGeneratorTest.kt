package org.cafejojo.schaapi.testgenerator

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.BooleanType
import soot.CharType
import soot.IntType
import soot.RefType
import soot.SootClass
import soot.VoidType
import soot.jimple.IntConstant
import soot.jimple.Jimple
import soot.jimple.StringConstant

internal class JimpleGeneratorTest : Spek({
    describe("generation of a method based on a list of nodes") {
        it("should not create parameters if all variables are bound") {
            val a = Jimple.v().newLocal("a", CharType.v())
            val b = Jimple.v().newLocal("b", CharType.v())
            val c = Jimple.v().newLocal("c", CharType.v())

            val assignA = Jimple.v().newAssignStmt(a, StringConstant.v("hello"))
            val assignB = Jimple.v().newAssignStmt(b, StringConstant.v("world"))
            val assignC = Jimple.v().newAssignStmt(c, Jimple.v().newAddExpr(a, b))

            val sClass = SootClass("asdf")
            val jimpleMethod = SootClassGenerator(sClass)
                .generateMethod("method", listOf(assignA, assignB, assignC))

            assertThat(jimpleMethod.parameterCount).isZero()
        }

        it("should generate parameters for all unbound variables") {
            val a = Jimple.v().newLocal("a", IntType.v())
            val b = Jimple.v().newLocal("b", IntType.v())
            val c = Jimple.v().newLocal("c", IntType.v())

            val assignC = Jimple.v().newAssignStmt(c, Jimple.v().newAddExpr(a, b))

            val sClass = SootClass("class")
            val jimpleMethod = SootClassGenerator(sClass)
                .generateMethod("method", listOf(assignC))

            assertThat(jimpleMethod.parameterTypes).containsExactly(IntType.v(), IntType.v())
            assertThat(jimpleMethod.activeBody.parameterLocals.map { it.name }).containsExactly(a.name, b.name)
        }

        it("can generate multiple methods") {
            val a = Jimple.v().newLocal("a", IntType.v())
            val b = Jimple.v().newLocal("b", IntType.v())
            val c = Jimple.v().newLocal("c", IntType.v())
            val d = Jimple.v().newLocal("d", IntType.v())

            val assignC = Jimple.v().newAssignStmt(c, Jimple.v().newAddExpr(a, b))
            val assignD = Jimple.v().newAssignStmt(d, IntConstant.v(23))

            val sClass = SootClass("class")
            val generator = SootClassGenerator(sClass)

            val method1 = generator.generateMethod("method1", listOf(assignC))
            val method2 = generator.generateMethod("method2", listOf(assignD))

            assertThat(method1.parameterCount).isEqualTo(2)
            assertThat(method2.parameterCount).isZero()
            assertThat(sClass.methodCount).isEqualTo(2)
        }

        it("should generate a valid body") {
            val a = Jimple.v().newLocal("a", IntType.v())
            val b = Jimple.v().newLocal("b", IntType.v())
            val c = Jimple.v().newLocal("c", IntType.v())

            val assignC = Jimple.v().newAssignStmt(c, Jimple.v().newAddExpr(a, b))

            val sClass = SootClass("class")
            val jimpleMethod = SootClassGenerator(sClass)
                .generateMethod("method", listOf(assignC))

            val throwable = catchThrowable { jimpleMethod.activeBody.validate() }
            assertThat(throwable).isNull()
        }

        it("should generate parameters for variables only bound after their use") {
            val a = Jimple.v().newLocal("a", IntType.v())
            val b = Jimple.v().newLocal("b", IntType.v())
            val c = Jimple.v().newLocal("c", IntType.v())

            val assignA = Jimple.v().newAssignStmt(a, IntConstant.v(10))
            val assignC = Jimple.v().newAssignStmt(c, Jimple.v().newAddExpr(a, b))
            val assignB = Jimple.v().newAssignStmt(b, IntConstant.v(20))

            val sClass = SootClass("class")
            val jimpleMethod = SootClassGenerator(sClass)
                .generateMethod("method", listOf(assignA, assignC, assignB))

            assertThat(jimpleMethod.parameterTypes).containsExactly(IntType.v())
            assertThat(jimpleMethod.activeBody.parameterLocals.map { it.name }).containsExactly(b.name)
            assertThat(jimpleMethod.activeBody.locals.map { it.name })
                .contains(a.name, b.name, c.name)
        }

        it("should generate a method with all the locals used") {
            val a = Jimple.v().newLocal("a", BooleanType.v())
            val b = Jimple.v().newLocal("b", BooleanType.v())
            val c = Jimple.v().newLocal("c", BooleanType.v())

            val assignC = Jimple.v().newAssignStmt(c, Jimple.v().newAndExpr(a, b))

            val sClass = SootClass("class")
            val jimpleMethod = SootClassGenerator(sClass)
                .generateMethod("method", listOf(assignC))

            assertThat(jimpleMethod.activeBody.locals.map { it.name }).contains(a.name, b.name, c.name)
        }

        it("should generate a method with return type void if no return is present") {
            val c = Jimple.v().newLocal("c", BooleanType.v())

            val assignC = Jimple.v().newAssignStmt(c, IntConstant.v(10))

            val sClass = SootClass("class")
            val jimpleMethod = SootClassGenerator(sClass)
                .generateMethod("method", listOf(assignC))

            assertThat(jimpleMethod.returnType).isEqualTo(VoidType.v())
        }

        it("should generate a method with return type boolean of last statement is return boolean") {
            val c = Jimple.v().newLocal("c", BooleanType.v())

            val assignC = Jimple.v().newAssignStmt(c, IntConstant.v(10))
            val returnC = Jimple.v().newReturnStmt(c)

            val sClass = SootClass("class")
            val jimpleMethod = SootClassGenerator(sClass)
                .generateMethod("method", listOf(assignC, returnC))

            assertThat(jimpleMethod.returnType).isEqualTo(c.type)
        }

        it("should generate a method with custom return type if last statement is custom return type") {
            val c = Jimple.v().newLocal("c", RefType.v("myClass"))

            val assignC = Jimple.v().newAssignStmt(c, IntConstant.v(10))
            val returnC = Jimple.v().newReturnStmt(c)

            val sClass = SootClass("class")
            val jimpleMethod = SootClassGenerator(sClass)
                .generateMethod("method", listOf(assignC, returnC))

            assertThat(jimpleMethod.returnType).isEqualTo(c.type)
        }

        it("should generate a method with only statements before return") {
            val c = Jimple.v().newLocal("c", RefType.v("myClass"))

            val assignC = Jimple.v().newAssignStmt(c, IntConstant.v(10))
            val returnC = Jimple.v().newReturnStmt(c)
            val assignCAgain = Jimple.v().newAssignStmt(c, IntConstant.v(20))

            val sClass = SootClass("class")
            val jimpleMethod = SootClassGenerator(sClass)
                .generateMethod("method", listOf(assignC, returnC, assignCAgain))

            assertThat(jimpleMethod.activeBody.units).hasSize(3)
        }
    }
})
