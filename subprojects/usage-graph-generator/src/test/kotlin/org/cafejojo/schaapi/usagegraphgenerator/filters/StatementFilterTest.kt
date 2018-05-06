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

private const val NON_LIBRARY_CLASS = "java.lang.String"
private const val LIBRARY_CLASS = "org.cafejojo.schaapi.usagegraphgenerator.testclasses.library"

internal class StatementFilterTest : Spek({
    describe("filters statements based on library usage") {
        val libraryValue = constructInvokeExprMock(LIBRARY_CLASS)
        val nonLibraryValue = constructInvokeExprMock(NON_LIBRARY_CLASS)

        it("filters throw statements") {
            itRetains(mock<ThrowStmt> {
                on { op } doReturn libraryValue
            })
            itDoesNotRetain(mock<ThrowStmt> {
                on { op } doReturn nonLibraryValue
            })
        }

        it("filters definition statements") {
            itRetains(mock<DefinitionStmt> {
                on { rightOp } doReturn libraryValue
            })
            itDoesNotRetain(mock<DefinitionStmt> {
                on { rightOp } doReturn nonLibraryValue
            })
        }

        it("filters if statements") {
        }

        it("filters switch statements") {
        }

        it("filters invoke statements") {
            itRetains(mock<InvokeStmt> {
                on { invokeExpr } doReturn libraryValue
            })
            itDoesNotRetain(mock<InvokeStmt> {
                on { invokeExpr } doReturn nonLibraryValue
            })
        }

        it("filters return statements") {
            itRetains(mock<ReturnStmt> {
                on { op } doReturn libraryValue
            })
            itDoesNotRetain(mock<ReturnStmt> {
                on { op } doReturn nonLibraryValue
            })
        }

        it("filters goto statements") {
            itRetains(mock<GotoStmt>())
        }

        it("filters return void statements") {
            itRetains(mock<ReturnVoidStmt>())
        }

        it("filters unknown statements") {
            itDoesNotRetain(mock<Unit>())
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

private fun itRetains(unit: Unit) = assertThat(StatementFilter.retain(unit)).isTrue()
private fun itDoesNotRetain(unit: Unit) = assertThat(StatementFilter.retain(unit)).isFalse()
