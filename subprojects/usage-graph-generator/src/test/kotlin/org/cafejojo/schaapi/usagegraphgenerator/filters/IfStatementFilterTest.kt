package org.cafejojo.schaapi.usagegraphgenerator.filters

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.usagegraphgenerator.libraryProject
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.Body
import soot.PatchingChain
import soot.jimple.GotoStmt
import soot.jimple.IfStmt
import soot.jimple.InvokeStmt
import soot.jimple.ReturnVoidStmt
import soot.util.HashChain

internal class IfStatementFilterTest : Spek({
    describe("filtering of if statements where branches determine existence after filtering") {
        val libraryInvokeExpr = constructInvokeExprMock(LIBRARY_CLASS)
        val nonLibraryInvokeExpr = constructInvokeExprMock(NON_LIBRARY_CLASS)

        it("retains ifs with library usage in both branches") {
            val unitChain = PatchingChain(HashChain())
            val body = mock<Body> {
                on { units } doReturn unitChain
            }
            val ifStart = mock<IfStmt>().also(unitChain::addLast)
            val trueBranchBody = mock<InvokeStmt> {
                on { invokeExpr } doReturn libraryInvokeExpr
            }.also(unitChain::addLast)
            val goToTrueBranchEnd = mock<GotoStmt>().also(unitChain::addLast)
            val falseBranchStart = mock<InvokeStmt> {
                on { invokeExpr } doReturn libraryInvokeExpr
            }.also(unitChain::addLast)
            val ifEnd = mock<ReturnVoidStmt>().also(unitChain::addLast)

            whenever(ifStart.target).thenReturn(falseBranchStart)
            whenever(goToTrueBranchEnd.target).thenReturn(ifEnd)

            StatementFilter(libraryProject).apply(body)
            IfStatementFilter(libraryProject).apply(body)

            assertThat(unitChain).containsExactly(
                ifStart,
                trueBranchBody,
                goToTrueBranchEnd,
                falseBranchStart,
                ifEnd
            )
        }

        it("retains ifs with library usage in the true branch") {
            val unitChain = PatchingChain(HashChain())
            val body = mock<Body> {
                on { units } doReturn unitChain
            }
            val ifStart = mock<IfStmt>().also(unitChain::addLast)
            val trueBranchBody = mock<InvokeStmt> {
                on { invokeExpr } doReturn libraryInvokeExpr
            }.also(unitChain::addLast)
            val goToTrueBranchEnd = mock<GotoStmt>().also(unitChain::addLast)
            val falseBranchStart = mock<InvokeStmt> {
                on { invokeExpr } doReturn nonLibraryInvokeExpr
            }.also(unitChain::addLast)
            val ifEnd = mock<ReturnVoidStmt>().also(unitChain::addLast)

            whenever(ifStart.target).thenReturn(falseBranchStart)
            whenever(goToTrueBranchEnd.target).thenReturn(ifEnd)

            StatementFilter(libraryProject).apply(body)
            IfStatementFilter(libraryProject).apply(body)

            assertThat(unitChain).containsExactly(
                ifStart,
                trueBranchBody,
                goToTrueBranchEnd,
                ifEnd
            )
        }

        it("retains ifs with library usage in the false branch") {
            val unitChain = PatchingChain(HashChain())
            val body = mock<Body> {
                on { units } doReturn unitChain
            }
            val ifStart = mock<IfStmt>().also(unitChain::addLast)
            mock<InvokeStmt> {
                on { invokeExpr } doReturn nonLibraryInvokeExpr
            }.also(unitChain::addLast)
            val goToTrueBranchEnd = mock<GotoStmt>().also(unitChain::addLast)
            val falseBranchStart = mock<InvokeStmt> {
                on { invokeExpr } doReturn libraryInvokeExpr
            }.also(unitChain::addLast)
            val ifEnd = mock<ReturnVoidStmt>().also(unitChain::addLast)

            whenever(ifStart.target).thenReturn(falseBranchStart)
            whenever(goToTrueBranchEnd.target).thenReturn(ifEnd)

            StatementFilter(libraryProject).apply(body)
            IfStatementFilter(libraryProject).apply(body)

            assertThat(unitChain).containsExactly(
                ifStart,
                goToTrueBranchEnd,
                falseBranchStart,
                ifEnd
            )
        }

        it("filters ifs with no library usages in its branches") {
            val unitChain = PatchingChain(HashChain())
            val body = mock<Body> {
                on { units } doReturn unitChain
            }
            val ifStart = mock<IfStmt> {
                on { condition } doReturn nonLibraryInvokeExpr
            }.also(unitChain::addLast)
            mock<InvokeStmt> {
                on { invokeExpr } doReturn nonLibraryInvokeExpr
            }.also(unitChain::addLast)
            val goToTrueBranchEnd = mock<GotoStmt>().also(unitChain::addLast)
            mock<InvokeStmt> {
                on { invokeExpr } doReturn nonLibraryInvokeExpr
            }.also(unitChain::addLast)
            val ifEnd = mock<ReturnVoidStmt>().also(unitChain::addLast)

            whenever(ifStart.target).thenReturn(ifEnd) // looks counter intuitive, but this is how the structure ends up
            whenever(goToTrueBranchEnd.target).thenReturn(ifEnd)

            StatementFilter(libraryProject).apply(body)
            IfStatementFilter(libraryProject).apply(body)

            assertThat(unitChain).containsExactly(
                ifEnd
            )
        }
    }
})
