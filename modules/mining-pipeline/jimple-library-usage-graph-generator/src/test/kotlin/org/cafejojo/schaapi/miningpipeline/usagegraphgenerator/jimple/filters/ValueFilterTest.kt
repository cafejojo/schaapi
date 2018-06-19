package org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.filters

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.libraryProject
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.jupiter.api.assertThrows
import soot.Immediate
import soot.IntType
import soot.Local
import soot.RefType
import soot.SootClass
import soot.SootField
import soot.Value
import soot.jimple.AnyNewExpr
import soot.jimple.ArrayRef
import soot.jimple.BinopExpr
import soot.jimple.CastExpr
import soot.jimple.ConcreteRef
import soot.jimple.Constant
import soot.jimple.Expr
import soot.jimple.IdentityRef
import soot.jimple.NewArrayExpr
import soot.jimple.NewExpr
import soot.jimple.NewMultiArrayExpr
import soot.jimple.Ref
import soot.jimple.StaticFieldRef
import soot.jimple.UnopExpr
import soot.jimple.toolkits.infoflow.AbstractDataSource
import soot.jimple.toolkits.thread.synchronization.NewStaticLock

internal object ValueFilterTest : Spek({
    val libraryInvokeExpr = constructInvokeExprMock(LIBRARY_CLASS)
    val nonLibraryInvokeExpr = constructInvokeExprMock(NON_LIBRARY_CLASS)

    val libraryType = mock<RefType> {
        on { toString() } doReturn LIBRARY_CLASS
    }
    val nonLibraryType = mock<RefType> {
        on { toString() } doReturn NON_LIBRARY_CLASS
    }

    describe("filtering of expression values based on library usage") {
        it("filters invoke expressions") {
            assertThatItRetains(libraryInvokeExpr)
            assertThatItDoesNotRetain(nonLibraryInvokeExpr)
        }

        it("filters unary operation expression") {
            assertThatItRetains(mock<UnopExpr> {
                on { op } doReturn libraryInvokeExpr
                on { type } doReturn libraryType
            })
            assertThatItRetains(mock<UnopExpr> {
                on { op } doReturn libraryInvokeExpr
                on { type } doReturn nonLibraryType
            })
            assertThatItRetains(mock<UnopExpr> {
                on { op } doReturn nonLibraryInvokeExpr
                on { type } doReturn libraryType
            })
            assertThatItDoesNotRetain(mock<UnopExpr> {
                on { op } doReturn nonLibraryInvokeExpr
                on { type } doReturn nonLibraryType
            })
        }

        it("filters binary operation expressions") {
            assertThatItRetains(mock<BinopExpr> {
                on { type } doReturn libraryType
                on { op1 } doReturn libraryInvokeExpr
                on { op2 } doReturn libraryInvokeExpr
            })
            assertThatItRetains(mock<BinopExpr> {
                on { type } doReturn libraryType
                on { op1 } doReturn libraryInvokeExpr
                on { op2 } doReturn nonLibraryInvokeExpr
            })
            assertThatItRetains(mock<BinopExpr> {
                on { type } doReturn libraryType
                on { op1 } doReturn nonLibraryInvokeExpr
                on { op2 } doReturn libraryInvokeExpr
            })
            assertThatItRetains(mock<BinopExpr> {
                on { type } doReturn libraryType
                on { op1 } doReturn nonLibraryInvokeExpr
                on { op2 } doReturn nonLibraryInvokeExpr
            })
            assertThatItRetains(mock<BinopExpr> {
                on { type } doReturn nonLibraryType
                on { op1 } doReturn libraryInvokeExpr
                on { op2 } doReturn libraryInvokeExpr
            })
            assertThatItRetains(mock<BinopExpr> {
                on { type } doReturn nonLibraryType
                on { op1 } doReturn libraryInvokeExpr
                on { op2 } doReturn nonLibraryInvokeExpr
            })
            assertThatItRetains(mock<BinopExpr> {
                on { type } doReturn nonLibraryType
                on { op1 } doReturn nonLibraryInvokeExpr
                on { op2 } doReturn libraryInvokeExpr
            })
            assertThatItDoesNotRetain(mock<BinopExpr> {
                on { type } doReturn nonLibraryType
                on { op1 } doReturn nonLibraryInvokeExpr
                on { op2 } doReturn nonLibraryInvokeExpr
            })
        }

        it("filters new expressions") {
            assertThatItRetains(mock<NewExpr> {
                on { baseType } doReturn libraryType
            })
            assertThatItDoesNotRetain(mock<NewExpr> {
                on { baseType } doReturn nonLibraryType
            })
        }

        it("filters new array expressions") {
            val sizeLocal = mock<Local> {
                on { type } doReturn IntType.v()
            }

            assertThatItDoesNotRetain(mock<NewArrayExpr> {
                on { size } doReturn sizeLocal
                on { baseType } doReturn libraryType
            })
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
                on { castType } doReturn libraryType
            })
            assertThatItRetains(mock<CastExpr> {
                on { op } doReturn libraryInvokeExpr
                on { castType } doReturn nonLibraryType
            })
            assertThatItRetains(mock<CastExpr> {
                on { op } doReturn nonLibraryInvokeExpr
                on { castType } doReturn libraryType
            })
            assertThatItDoesNotRetain(mock<CastExpr> {
                on { op } doReturn nonLibraryInvokeExpr
                on { castType } doReturn nonLibraryType
            })
        }

        it("does not recognize unknown expressions") {
            assertThatItDoesNotRecognize(mock<Expr>())
        }
    }

    describe("filtering of ref values based on library usage") {
        val libraryClass = constructDeclaringClass(LIBRARY_CLASS)
        val nonLibraryClass = constructDeclaringClass(NON_LIBRARY_CLASS)

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
            assertThatItRetains(mock<StaticFieldRef> {
                on { field } doReturn libraryField
            })
            assertThatItDoesNotRetain(mock<StaticFieldRef> {
                on { field } doReturn nonLibraryField
            })
        }

        it("filters array refs") {
            val indexLocal = mock<Local> {
                on { type } doReturn IntType.v()
            }

            assertThatItRetains(mock<ArrayRef> {
                on { type } doReturn libraryType
                on { base } doReturn libraryInvokeExpr
                on { index } doReturn indexLocal
            })
            assertThatItDoesNotRetain(mock<ArrayRef> {
                on { type } doReturn nonLibraryType
                on { base } doReturn nonLibraryInvokeExpr
                on { index } doReturn indexLocal
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
            assertThatItRetains(mock<Local> {
                on { type } doReturn libraryType
            })

            assertThatItDoesNotRetain(mock<Local> {
                on { type } doReturn nonLibraryType
            })
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
            assertThatItDoesNotRecognize(mock {})
        }
    }

    describe("filtering multiple values at once") {
        it("retains both values if they are both valid") {
            assertThatItRetains(listOf<Value>(
                mock<UnopExpr> {
                    on { type } doReturn libraryType
                    on { op } doReturn libraryInvokeExpr
                },
                mock<UnopExpr> {
                    on { type } doReturn libraryType
                    on { op } doReturn libraryInvokeExpr
                }
            ))
        }

        it("filters out both values if either has an undesired class") {
            assertThatItDoesNotRetain(listOf(
                mock<UnopExpr> {
                    on { type } doReturn libraryType
                    on { op } doReturn libraryInvokeExpr
                },
                mock<NewStaticLock> {
                    on { type } doReturn libraryType
                }
            ))
            assertThatItDoesNotRetain(listOf(
                mock<NewStaticLock> {
                    on { type } doReturn libraryType
                },
                mock<UnopExpr> {
                    on { type } doReturn libraryType
                    on { op } doReturn libraryInvokeExpr
                }
            ))
            assertThatItDoesNotRetain(listOf<Value>(
                mock<NewStaticLock> {
                    on { type } doReturn libraryType
                },
                mock<NewStaticLock> {
                    on { type } doReturn libraryType
                }
            ))
        }

        it("filters out both values if neither has a library usage") {
            assertThatItRetains(listOf<Value>(
                mock<UnopExpr> {
                    on { type } doReturn libraryType
                    on { op } doReturn libraryInvokeExpr
                },
                mock<UnopExpr> {
                    on { type } doReturn nonLibraryType
                    on { op } doReturn nonLibraryInvokeExpr
                }
            ))
            assertThatItRetains(listOf<Value>(
                mock<UnopExpr> {
                    on { type } doReturn nonLibraryType
                    on { op } doReturn nonLibraryInvokeExpr
                },
                mock<UnopExpr> {
                    on { type } doReturn libraryType
                    on { op } doReturn libraryInvokeExpr
                }
            ))
            assertThatItDoesNotRetain(listOf<Value>(
                mock<UnopExpr> {
                    on { type } doReturn nonLibraryType
                    on { op } doReturn nonLibraryInvokeExpr
                },
                mock<UnopExpr> {
                    on { type } doReturn nonLibraryType
                    on { op } doReturn nonLibraryInvokeExpr
                }
            ))
        }
    }
})

private fun constructDeclaringClass(declaringClassName: String) = mock<SootClass> {
    on { name } doReturn declaringClassName
}

private fun assertThatItDoesNotRecognize(value: Value) =
    assertThrows<UnsupportedValueException> { ValueFilter(libraryProject).retain(value) }

private fun assertThatItRetains(value: Value) =
    assertThat(ValueFilter(libraryProject).retain(value)).isTrue()

private fun assertThatItRetains(values: Iterable<Value>) =
    assertThat(ValueFilter(libraryProject).retain(values)).isTrue()

private fun assertThatItDoesNotRetain(value: Value) =
    assertThat(ValueFilter(libraryProject).retain(value)).isFalse()

private fun assertThatItDoesNotRetain(values: Iterable<Value>) =
    assertThat(ValueFilter(libraryProject).retain(values)).isFalse()
