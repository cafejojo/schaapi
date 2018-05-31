package org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.filters

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.libraryProject
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.Unit
import soot.jimple.DefinitionStmt
import soot.jimple.GotoStmt
import soot.jimple.IfStmt
import soot.jimple.InvokeStmt
import soot.jimple.ReturnStmt
import soot.jimple.ReturnVoidStmt
import soot.jimple.SwitchStmt
import soot.jimple.ThrowStmt

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
            assertThatItRetains(mock<IfStmt>())
        }

        it("filters switch statements") {
            assertThatItRetains(mock<SwitchStmt>())
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

private fun assertThatItRetains(unit: Unit) =
    assertThat(StatementFilter(libraryProject).retain(unit)).isTrue()

private fun assertThatItDoesNotRetain(unit: Unit) =
    assertThat(StatementFilter(libraryProject).retain(unit)).isFalse()
