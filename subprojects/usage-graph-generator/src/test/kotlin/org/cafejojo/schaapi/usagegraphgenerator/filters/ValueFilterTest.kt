package org.cafejojo.schaapi.usagegraphgenerator.filters

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.jupiter.api.assertThrows
import soot.Immediate
import soot.Local
import soot.SootClass
import soot.SootField
import soot.SootMethod
import soot.Type
import soot.Value
import soot.jimple.AnyNewExpr
import soot.jimple.ArrayRef
import soot.jimple.BinopExpr
import soot.jimple.CastExpr
import soot.jimple.ConcreteRef
import soot.jimple.Constant
import soot.jimple.Expr
import soot.jimple.FieldRef
import soot.jimple.IdentityRef
import soot.jimple.InvokeExpr
import soot.jimple.NewArrayExpr
import soot.jimple.NewExpr
import soot.jimple.NewMultiArrayExpr
import soot.jimple.Ref
import soot.jimple.UnopExpr
import soot.jimple.internal.AbstractBinopExpr
import soot.jimple.toolkits.infoflow.AbstractDataSource
import soot.jimple.toolkits.thread.synchronization.NewStaticLock
import soot.shimple.PhiExpr
import soot.shimple.ShimpleExpr

internal class ValueFilterTest : Spek({
    val libraryInvokeExpr = constructInvokeExprMock("testclasses.library")
    val nonLibraryInvokeExpr = constructInvokeExprMock("org.cafejojo.schaapi")

    val libraryType = mock<Type> {
        on { toString() } doReturn "testclasses.library"
    }
    val nonLibraryType = mock<Type> {
        on { toString() } doReturn "org.cafejojo.schaapi"
    }

    describe("filtering of expression values based on library usage") {
        it("filters invoke expressions") {
            assertThatItRetains(libraryInvokeExpr)
            assertThatItDoesNotRetain(nonLibraryInvokeExpr)
        }

        it("filters unary operation expression") {
            assertThatItRetains(mock<UnopExpr> {
                on { op } doReturn libraryInvokeExpr
            })
            assertThatItDoesNotRetain(mock<UnopExpr> {

                on { op } doReturn nonLibraryInvokeExpr
            })
        }

        it("filters binary operation expressions") {
            assertThatItRetains(mock<BinopExpr> {
                on { op1 } doReturn libraryInvokeExpr
                on { op2 } doReturn libraryInvokeExpr
            })
            assertThatItRetains(mock<BinopExpr> {
                on { op1 } doReturn libraryInvokeExpr
                on { op2 } doReturn nonLibraryInvokeExpr
            })
            assertThatItRetains(mock<BinopExpr> {
                on { op1 } doReturn nonLibraryInvokeExpr
                on { op2 } doReturn libraryInvokeExpr
            })
            assertThatItDoesNotRetain(mock<BinopExpr> {
                on { op1 } doReturn nonLibraryInvokeExpr
                on { op2 } doReturn nonLibraryInvokeExpr
            })
        }

        it("filters abstract binary operation expressions") {
            assertThatItRetains(mock<AbstractBinopExpr> {
                on { op1 } doReturn libraryInvokeExpr
                on { op2 } doReturn libraryInvokeExpr
            })
            assertThatItRetains(mock<AbstractBinopExpr> {
                on { op1 } doReturn libraryInvokeExpr
                on { op2 } doReturn nonLibraryInvokeExpr
            })
            assertThatItRetains(mock<AbstractBinopExpr> {
                on { op1 } doReturn nonLibraryInvokeExpr
                on { op2 } doReturn libraryInvokeExpr
            })
            assertThatItDoesNotRetain(mock<AbstractBinopExpr> {
                on { op1 } doReturn nonLibraryInvokeExpr
                on { op2 } doReturn nonLibraryInvokeExpr
            })
        }

        it("filters phi expressions") {
            assertThatItRetains(mock<PhiExpr> {
                on { values } doReturn listOf(libraryInvokeExpr, nonLibraryInvokeExpr)
            })
            assertThatItDoesNotRetain(mock<PhiExpr> {
                on { values } doReturn listOf(nonLibraryInvokeExpr)
            })
        }

        it("does not recognize unknown shimple expressions") {
            assertThatItDoesNotRecognize(mock<ShimpleExpr>())
        }

        it("filters new expressions") {
            assertThatItRetains(mock<NewExpr> {
                on { type } doReturn libraryType
            })
            assertThatItDoesNotRetain(mock<NewExpr> {
                on { type } doReturn nonLibraryType
            })
        }

        it("filters new array expressions") {
            assertThatItDoesNotRetain(mock<NewArrayExpr>())
        }

        it("filters new multi array expressions") {
            assertThatItDoesNotRetain(mock<NewMultiArrayExpr>())
        }

        it("does not recognize unknown new expressions") {
            assertThatItDoesNotRecognize(mock<AnyNewExpr>())
        }

        it("filters cast expressions") {
            assertThatItRetains(mock<CastExpr> {
                on { op } doReturn libraryInvokeExpr
            })
            assertThatItDoesNotRetain(mock<CastExpr> {
                on { op } doReturn nonLibraryInvokeExpr
            })
        }

        it("does not recognize unknown expressions") {
            assertThatItDoesNotRecognize(mock<Expr>())
        }
    }

    describe("filtering of ref values based on library usage") {
        val libraryClass = constructDeclaringClass("testclasses.library")
        val nonLibraryClass = constructDeclaringClass("org.cafejojo.schaapi")

        val libraryField = mock<SootField> {
            on { declaringClass } doReturn libraryClass
        }
        val nonLibraryField = mock<SootField> {
            on { declaringClass } doReturn nonLibraryClass
        }

        it("filters identity refs") {
            assertThatItDoesNotRetain(mock<IdentityRef>())
        }

        it("filters field refs") {
            assertThatItRetains(mock<FieldRef> {
                on { field } doReturn libraryField
            })
            assertThatItDoesNotRetain(mock<FieldRef> {
                on { field } doReturn nonLibraryField
            })
        }

        it("filters array refs") {
            assertThatItRetains(mock<ArrayRef> {
                on { base } doReturn libraryInvokeExpr
            })
            assertThatItDoesNotRetain(mock<ArrayRef> {
                on { base } doReturn nonLibraryInvokeExpr
            })
        }

        it("does not recognize unknown refs") {
            assertThatItDoesNotRecognize(mock<Ref>())
        }

        it("does not recognize unknown concrete refs") {
            assertThatItDoesNotRecognize(mock<ConcreteRef>())
        }
    }

    describe("filtering of immediate values based on library usage") {
        it("filters local immediates") {
            assertThatItDoesNotRetain(mock<Local>())
        }

        it("filters constant immediates") {
            assertThatItDoesNotRetain(mock<Constant>())
        }

        it("does not recognize unknown immediates") {
            assertThatItDoesNotRecognize(mock<Immediate>())
        }
    }

    describe("filtering of static lock values based on library usage") {
        it("filters new static locks") {
            assertThatItDoesNotRetain(mock<NewStaticLock>())
        }
    }

    describe("filtering of data sources based on library usage") {
        it("filters data sources") {
            assertThatItDoesNotRetain(mock<AbstractDataSource>())
        }
    }

    describe("filtering of unrecognized values based on library usage") {
        it("does not recognize unknown values") {
            assertThatItDoesNotRecognize(mock<Value>())
        }
    }
})

private fun constructDeclaringClass(declaringClassName: String) = mock<SootClass> {
    on { name } doReturn declaringClassName
}

private fun constructInvokeExprMock(declaringClassName: String): InvokeExpr {
    val clazz = mock<SootClass> {
        on { name } doReturn declaringClassName
    }
    val method = mock<SootMethod> {
        on { declaringClass } doReturn clazz
    }
    return mock {
        on { getMethod() } doReturn method
    }
}

private fun assertThatItDoesNotRecognize(value: Value) =
    assertThrows<UnsupportedValueException> { ValueFilter.retain(value) }

private fun assertThatItRetains(value: Value) =
    assertThat(ValueFilter.retain(value)).isTrue()

private fun assertThatItDoesNotRetain(value: Value) =
    assertThat(ValueFilter.retain(value)).isFalse()