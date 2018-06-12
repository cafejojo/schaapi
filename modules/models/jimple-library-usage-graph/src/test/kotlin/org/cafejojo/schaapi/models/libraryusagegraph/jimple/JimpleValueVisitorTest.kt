package org.cafejojo.schaapi.models.libraryusagegraph.jimple

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.IntType
import soot.Modifier
import soot.RefType
import soot.SootClass
import soot.SootMethod
import soot.Value
import soot.jimple.IntConstant
import soot.jimple.Jimple

/**
 * Unit tests for [JimpleValueVisitor].
 */
internal object JimpleValueVisitorTest : Spek({
    describe("Jimple value visitor") {
        lateinit var visitor: JimpleValueAccumulator

        beforeEachTest {
            visitor = JimpleValueAccumulator()
        }

        it("returns the given element itself") {
            val value = mock<Value> {}

            assertThat(visitor.visit(value)).containsExactly(value)
        }

        it("returns the only value in an UnopExpr") {
            val const = IntConstant.v(1)
            val value = Jimple.v().newNegExpr(const)

            assertThat(visitor.visit(value)).containsExactly(value, const)
        }

        it("returns both values in a BinopExpr") {
            val constA = IntConstant.v(49)
            val constB = IntConstant.v(53)
            val value = Jimple.v().newSubExpr(constA, constB)

            assertThat(visitor.visit(value)).containsExactly(value, constA, constB)
        }

        it("returns the value of an AnyNewExpr") {
            val value = Jimple.v().newNewExpr(RefType.v("SomeClass"))

            assertThat(visitor.visit(value)).containsExactly(value)
        }

        it("returns the base and arguments of an InstanceInvokeExpr") {
            val base = Jimple.v().newLocal("base", RefType.v("BaseClass"))
            val method = SootMethod("method", listOf(RefType.v("ArgTypeA"), RefType.v("ArgTypeB")), IntType.v())
                .also { SootClass("SomeClass").addMethod(it) }
            val argA = Jimple.v().newLocal("argA", RefType.v("ArgTypeA"))
            val argB = Jimple.v().newLocal("argB", RefType.v("ArgTypeB"))
            val value = Jimple.v().newSpecialInvokeExpr(base, method.makeRef(), listOf(argA, argB))

            assertThat(visitor.visit(value)).containsExactly(value, base, argA, argB)
        }

        it("returns the arguments of a StaticInvokeExpr") {
            val method = SootMethod(
                "method",
                listOf(RefType.v("ArgTypeA"), RefType.v("ArgTypeB")),
                IntType.v(),
                Modifier.STATIC
            ).also { SootClass("SomeClass").addMethod(it) }
            val argA = Jimple.v().newLocal("argA", RefType.v("ArgTypeA"))
            val argB = Jimple.v().newLocal("argB", RefType.v("ArgTypeB"))
            val value = Jimple.v().newStaticInvokeExpr(method.makeRef(), listOf(argA, argB))

            assertThat(visitor.visit(value)).containsExactly(value, argA, argB)
        }

        it("returns the arguments of a DynamicInvokeExpr") {
            val method = SootMethod("method", listOf(RefType.v("ArgTypeA"), RefType.v("ArgTypeB")), IntType.v())
                .also { SootClass(SootClass.INVOKEDYNAMIC_DUMMY_CLASS_NAME).addMethod(it) }
            val bsArgA = Jimple.v().newLocal("bsArgA", RefType.v("ArgTypeA"))
            val bsArgB = Jimple.v().newLocal("bsArgB", RefType.v("ArgTypeB"))
            val argA = Jimple.v().newLocal("argA", RefType.v("ArgTypeA"))
            val argB = Jimple.v().newLocal("argB", RefType.v("ArgTypeB"))
            val value = Jimple.v().newDynamicInvokeExpr(
                method.makeRef(), listOf(bsArgA, bsArgB),
                method.makeRef(), listOf(argA, argB)
            )

            assertThat(visitor.visit(value)).containsExactly(value, bsArgA, bsArgB, argA, argB)
        }

        it("returns the value of a Ref") {
            val value = Jimple.v().newThisRef(RefType.v("SomeClass"))

            assertThat(visitor.visit(value)).containsExactly(value)
        }

        it("returns the value of an Immediate") {
            val value = Jimple.v().newLocal("local", RefType.v("SomeClass"))

            assertThat(visitor.visit(value)).containsExactly(value)
        }

        it("returns the value of an InstanceOf") {
            val local = Jimple.v().newLocal("local", RefType.v("SomeClass"))
            val value = Jimple.v().newInstanceOfExpr(local, RefType.v("SomeOtherClass"))

            assertThat(visitor.visit(value)).containsExactly(value, local)
        }

        it("returns the value of a CastExpr") {
            val local = Jimple.v().newLocal("local", RefType.v("SomeClass"))
            val value = Jimple.v().newCastExpr(local, RefType.v("SomeOtherClass"))

            assertThat(visitor.visit(value)).containsExactly(value, local)
        }
    }
})
