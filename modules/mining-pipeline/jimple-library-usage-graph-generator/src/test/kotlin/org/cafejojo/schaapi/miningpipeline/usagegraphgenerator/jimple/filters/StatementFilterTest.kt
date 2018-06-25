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

internal object StatementFilterTest : Spek({
    describe("filters statements based on library usage") {
        val libraryValue = constructInvokeExprMock(LIBRARY_CLASS)
        val userValue = constructInvokeExprMock(USER_CLASS)
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
                on { leftOp } doReturn libraryValue
                on { rightOp } doReturn libraryValue
            })
            assertThatItRetains(mock<DefinitionStmt> {
                on { leftOp } doReturn nonLibraryValue
                on { rightOp } doReturn libraryValue
            })
            assertThatItRetains(mock<DefinitionStmt> {
                on { leftOp } doReturn libraryValue
                on { rightOp } doReturn nonLibraryValue
            })
            assertThatItDoesNotRetain(mock<DefinitionStmt> {
                on { leftOp } doReturn nonLibraryValue
                on { rightOp } doReturn nonLibraryValue
            })
        }

        it("filters if statements") {
            assertThatItRetains(mock<IfStmt> {
                on { it.condition } doReturn libraryValue
            })
            assertThatItRetains(mock<IfStmt> {
                on { it.condition } doReturn nonLibraryValue
            })
            assertThatItRetains(mock<IfStmt> {
                on { it.condition } doReturn userValue
            })
        }

        it("filters switch statements") {
            assertThatItRetains(mock<SwitchStmt> {
                on { it.key } doReturn libraryValue
            })
            assertThatItRetains(mock<SwitchStmt> {
                on { it.key } doReturn nonLibraryValue
            })
            assertThatItRetains(mock<SwitchStmt> {
                on { it.key } doReturn userValue
            })
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
            assertThatItRetains(mock<ReturnStmt> {
                on { op } doReturn nonLibraryValue
            })
            assertThatItDoesNotRetain(mock<ReturnStmt> {
                on { op } doReturn userValue
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

private fun assertThatItRetains(unit: Unit) =
    assertThat(StatementFilter(libraryProject).retain(unit)).isTrue()

private fun assertThatItDoesNotRetain(unit: Unit) =
    assertThat(StatementFilter(libraryProject).retain(unit)).isFalse()
