package org.cafejojo.schaapi.usagegraphgenerator.filters

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.Body
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

private const val NON_LIBRARY_CLASS = "java.lang.String"
private const val LIBRARY_CLASS = "org.cafejojo.schaapi.usagegraphgenerator.testclasses.library"

internal class StatementFilterTest : Spek({
    describe("filters statements based on library usage") {
        val libraryValue = constructInvokeExprMock(LIBRARY_CLASS)
        val nonLibraryValue = constructInvokeExprMock(NON_LIBRARY_CLASS)

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
            assertThatItRetains(mock<ReturnStmt>())
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

private fun assertThatItRetains(unit: Unit, body: Body = mock()) =
    assertThat(StatementFilter(body).retain(unit)).isTrue()

private fun assertThatItDoesNotRetain(unit: Unit, body: Body = mock()) =
    assertThat(StatementFilter(body).retain(unit)).isFalse()
