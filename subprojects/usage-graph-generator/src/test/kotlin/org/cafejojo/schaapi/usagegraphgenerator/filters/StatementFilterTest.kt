package org.cafejojo.schaapi.usagegraphgenerator.filters

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.SootClass
import soot.SootMethod
import soot.Unit
import soot.jimple.DefinitionStmt
import soot.jimple.GotoStmt
import soot.jimple.InvokeExpr
import soot.jimple.InvokeStmt
import soot.jimple.ReturnStmt
import soot.jimple.ReturnVoidStmt
import soot.jimple.ThrowStmt

internal class StatementFilterTest : Spek({
    describe("filters statements based on library usage") {
        val libraryValue = constructInvokeExprMock("testclasses.library")
        val nonLibraryValue = constructInvokeExprMock("org.cafejojo.schaapi")

        it("filters throw statements") {
            assertThatItRetains(mock<ThrowStmt> {
                on { op } doReturn libraryValue
            })
            assertThatItDoesNotRetain(mock<ThrowStmt> {
                on { op } doReturn nonLibraryValue
            })
        }

        it("filters definition statements") {
            assertThatItRetains(mock<DefinitionStmt> {
                on { rightOp } doReturn libraryValue
            })
            assertThatItDoesNotRetain(mock<DefinitionStmt> {
                on { rightOp } doReturn nonLibraryValue
            })
        }

        it("filters if statements") {
            // todo
        }

        it("filters switch statements") {
            // todo
        }

        it("filters invoke statements") {
            assertThatItRetains(mock<InvokeStmt> {
                on { invokeExpr } doReturn libraryValue
            })
            assertThatItDoesNotRetain(mock<InvokeStmt> {
                on { invokeExpr } doReturn nonLibraryValue
            })
        }

        it("filters return statements") {
            assertThatItRetains(mock<ReturnStmt> {
                on { op } doReturn libraryValue
            })
            assertThatItDoesNotRetain(mock<ReturnStmt> {
                on { op } doReturn nonLibraryValue
            })
        }

        it("filters goto statements") {
            assertThatItRetains(mock<GotoStmt>())
        }

        it("filters return void statements") {
            assertThatItRetains(mock<ReturnVoidStmt>())
        }

        it("filters unknown statements") {
            assertThatItDoesNotRetain(mock<Unit>())
        }
    }
})

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

private fun assertThatItRetains(unit: Unit) = assertThat(StatementFilter.retain(unit)).isTrue()
private fun assertThatItDoesNotRetain(unit: Unit) = assertThat(StatementFilter.retain(unit)).isFalse()
