package org.cafejojo.schaapi.usagegraphgenerator.filters

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.jupiter.api.assertThrows
import soot.Local
import soot.SootClass
import soot.SootField
import soot.SootMethod
import soot.Type
import soot.Value
import soot.jimple.ArrayRef
import soot.jimple.BinopExpr
import soot.jimple.CastExpr
import soot.jimple.Constant
import soot.jimple.FieldRef
import soot.jimple.IdentityRef
import soot.jimple.InvokeExpr
import soot.jimple.NewArrayExpr
import soot.jimple.NewExpr
import soot.jimple.NewMultiArrayExpr
import soot.jimple.UnopExpr
import soot.jimple.internal.AbstractBinopExpr
import soot.jimple.toolkits.infoflow.AbstractDataSource
import soot.jimple.toolkits.thread.synchronization.NewStaticLock
import soot.shimple.PhiExpr

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
            itRetains(libraryInvokeExpr)
            itDoesNotRetain(nonLibraryInvokeExpr)
        }

        it("filters unary operation expression") {
            itRetains(mock<UnopExpr> {
                on { op } doReturn libraryInvokeExpr
            })
            itDoesNotRetain(mock<UnopExpr> {
                on { op } doReturn nonLibraryInvokeExpr
            })
        }

        it("filters binary operation expressions") {
            itRetains(mock<BinopExpr> {
                on { op1 } doReturn libraryInvokeExpr
                on { op2 } doReturn libraryInvokeExpr
            })
            itRetains(mock<BinopExpr> {
                on { op1 } doReturn libraryInvokeExpr
                on { op2 } doReturn nonLibraryInvokeExpr
            })
            itRetains(mock<BinopExpr> {
                on { op1 } doReturn nonLibraryInvokeExpr
                on { op2 } doReturn libraryInvokeExpr
            })
            itDoesNotRetain(mock<BinopExpr> {
                on { op1 } doReturn nonLibraryInvokeExpr
                on { op2 } doReturn nonLibraryInvokeExpr
            })
        }

        it("filters abstract binary operation expressions") {
            itRetains(mock<AbstractBinopExpr> {
                on { op1 } doReturn libraryInvokeExpr
                on { op2 } doReturn libraryInvokeExpr
            })
            itRetains(mock<AbstractBinopExpr> {
                on { op1 } doReturn libraryInvokeExpr
                on { op2 } doReturn nonLibraryInvokeExpr
            })
            itRetains(mock<AbstractBinopExpr> {
                on { op1 } doReturn nonLibraryInvokeExpr
                on { op2 } doReturn libraryInvokeExpr
            })
            itDoesNotRetain(mock<AbstractBinopExpr> {
                on { op1 } doReturn nonLibraryInvokeExpr
                on { op2 } doReturn nonLibraryInvokeExpr
            })
        }

        it("filters phi expressions") {
            itRetains(mock<PhiExpr> {
                on { values } doReturn listOf(libraryInvokeExpr, nonLibraryInvokeExpr)
            })
            itDoesNotRetain(mock<PhiExpr> {
                on { values } doReturn listOf(nonLibraryInvokeExpr)
            })
        }

        it("filters new expressions") {
            itRetains(mock<NewExpr> {
                on { type } doReturn libraryType
            })
            itDoesNotRetain(mock<NewExpr> {
                on { type } doReturn nonLibraryType
            })
        }

        it("filters new array expressions") {
            itDoesNotRetain(mock<NewArrayExpr>())
        }

        it("filters new multi array expressions") {
            itDoesNotRetain(mock<NewMultiArrayExpr>())
        }

        it("filters cast expressions") {
            itRetains(mock<CastExpr> {
                on { op } doReturn libraryInvokeExpr
            })
            itDoesNotRetain(mock<CastExpr> {
                on { op } doReturn nonLibraryInvokeExpr
            })
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
            itDoesNotRetain(mock<IdentityRef>())
        }

        it("filters field refs") {
            itRetains(mock<FieldRef> {
                on { field } doReturn libraryField
            })
            itDoesNotRetain(mock<FieldRef> {
                on { field } doReturn nonLibraryField
            })
        }

        it("filters array refs") {
            itRetains(mock<ArrayRef> {
                on { base } doReturn libraryInvokeExpr
            })
            itDoesNotRetain(mock<ArrayRef> {
                on { base } doReturn nonLibraryInvokeExpr
            })
        }
    }

    describe("filtering of immediate values based on library usage") {
        it("filters local immediates") {
            itDoesNotRetain(mock<Local>())
        }
        it("filters constant immediates") {
            itDoesNotRetain(mock<Constant>())
        }
    }

    describe("filtering of static lock values based on library usage") {
        it("filters new static locks") {
            itDoesNotRetain(mock<NewStaticLock>())
        }
    }

    describe("filtering of data sources based on library usage") {
        it("filters data sources") {
            itDoesNotRetain(mock<AbstractDataSource>())
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

private fun itDoesNotRecognize(value: Value) = assertThrows<UnsupportedValueException> { ValueFilter.retain(value) }
private fun itRetains(value: Value) = assertThat(ValueFilter.retain(value)).isTrue()
private fun itDoesNotRetain(value: Value) = assertThat(ValueFilter.retain(value)).isFalse()
