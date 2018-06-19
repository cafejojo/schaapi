package org.cafejojo.schaapi.models.libraryusagegraph.jimple

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.Immediate
import soot.IntType
import soot.Modifier
import soot.RefType
import soot.SootClass
import soot.SootField
import soot.SootMethod
import soot.Value
import soot.jimple.AnyNewExpr
import soot.jimple.Expr
import soot.jimple.FieldRef
import soot.jimple.IntConstant
import soot.jimple.InvokeExpr
import soot.jimple.Jimple
import soot.jimple.Ref

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

        context("Expr") {
            it("returns the value in an unknown Expr") {
                val value = mock<Expr> {}

                assertThat(visitor.visit(value)).containsExactly(value)
            }

            it("returns the only value of an UnopExpr") {
                val const = IntConstant.v(1)
                val value = Jimple.v().newNegExpr(const)

                assertThat(visitor.visit(value)).containsExactly(value, const)
            }

            it("returns both values of a BinopExpr") {
                val constA = IntConstant.v(49)
                val constB = IntConstant.v(53)
                val value = Jimple.v().newSubExpr(constA, constB)

                assertThat(visitor.visit(value)).containsExactly(value, constA, constB)
            }

            it("returns the value of an InstanceOfExpr") {
                val local = Jimple.v().newLocal("local", RefType.v("SomeClass"))
                val value = Jimple.v().newInstanceOfExpr(local, RefType.v("SomeOtherClass"))

                assertThat(visitor.visit(value)).containsExactly(value, local)
            }

            it("returns the value of a CastExpr") {
                val local = Jimple.v().newLocal("local", RefType.v("SomeClass"))
                val value = Jimple.v().newCastExpr(local, RefType.v("SomeOtherClass"))

                assertThat(visitor.visit(value)).containsExactly(value, local)
            }

            context("AnyNewExpr") {
                it("returns the value of an unknown AnyNewExpr") {
                    val value = mock<AnyNewExpr> { }

                    assertThat(visitor.visit(value)).containsExactly(value)
                }

                it("returns the value of a NewExpr") {
                    val value = Jimple.v().newNewExpr(RefType.v("SomeClass"))

                    assertThat(visitor.visit(value)).containsExactly(value)
                }

                it("returns the value and size of a NewArrayExpr") {
                    val sizeValue = Jimple.v().newLocal("size", IntType.v())
                    val value = Jimple.v().newNewArrayExpr(RefType.v("SomeClass"), sizeValue)

                    assertThat(visitor.visit(value)).containsExactly(value, sizeValue)
                }

                it("returns the value and sizes of a NewMultiArrayExpr") {
                    val sizeValues = Array(3) { i -> Jimple.v().newLocal("size$i", IntType.v()) }
                    val value =
                        Jimple.v().newNewMultiArrayExpr(RefType.v("SomeClass").arrayType, sizeValues.toMutableList())

                    @Suppress("SpreadOperator") // Easier to write
                    assertThat(visitor.visit(value)).containsExactly(value, *sizeValues)
                }
            }

            context("InvokeExpr") {
                it("returns the value of an unknown InvokeExpr") {
                    val value = mock<InvokeExpr> {}

                    assertThat(visitor.visit(value)).containsExactly(value)
                }

                it("returns the value, base, and arguments of an InstanceInvokeExpr") {
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
            }
        }

        context("Ref") {
            it("returns the value of an unknown Ref") {
                val value = mock<Ref> {}

                assertThat(visitor.visit(value)).containsExactly(value)
            }

            it("returns the value of an IdentityRef") {
                val value = Jimple.v().newThisRef(RefType.v("SomeClass"))

                assertThat(visitor.visit(value)).containsExactly(value)
            }

            context("FieldRef") {
                it("returns the value of an unknown FieldRef") {
                    val value = mock<FieldRef> {}

                    assertThat(visitor.visit(value)).containsExactly(value)
                }

                it("returns the value and base of an InstanceFieldRef") {
                    val base = Jimple.v().newLocal("base", RefType.v("SomeClass"))
                    val field = SootField("field", IntType.v(), Modifier.PUBLIC)
                        .also { it.declaringClass = mock {} }
                    val value = Jimple.v().newInstanceFieldRef(base, field.makeRef())

                    assertThat(visitor.visit(value)).containsExactly(value, base)
                }

                it("returns the value of a StaticFieldRef") {
                    val field = SootField("field", IntType.v(), Modifier.PUBLIC + Modifier.STATIC)
                        .also { it.declaringClass = mock {} }
                    val value = Jimple.v().newStaticFieldRef(field.makeRef())

                    assertThat(visitor.visit(value)).containsExactly(value)
                }
            }

            it("returns the value, base, and index of an ArrayRef") {
                val base = Jimple.v().newLocal("base", RefType.v("SomeClass").arrayType)
                val index = Jimple.v().newLocal("offset", IntType.v())
                val value = Jimple.v().newArrayRef(base, index)

                assertThat(visitor.visit(value)).containsExactly(value, base, index)
            }
        }

        context("Immediate") {
            it("returns the value of an unknown Immediate") {
                val value = mock<Immediate> {}

                assertThat(visitor.visit(value)).containsExactly(value)
            }

            it("returns the value of a Local") {
                val value = Jimple.v().newLocal("local", RefType.v("SomeClass"))

                assertThat(visitor.visit(value)).containsExactly(value)
            }

            it("returns the value of a Constant") {
                val value = IntConstant.v(384)

                assertThat(visitor.visit(value)).containsExactly(value)
            }
        }
    }
})
