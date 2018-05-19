package org.cafejojo.schaapi.patternfilter

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.SootNode
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.SootMethod
import soot.jimple.InvokeExpr
import soot.jimple.InvokeStmt
import soot.jimple.ReturnVoidStmt
import soot.jimple.internal.JSpecialInvokeExpr

internal class IncompleteInitPatternFilterTest : Spek({
    describe("filtering of init calls without new") {
        it("rejects patterns starting with init calls") {
            val initMethod = mock<SootMethod> {
                on { name } doReturn "<init>"
            }
            val invokeExpression = mock<JSpecialInvokeExpr> {
                on { method } doReturn initMethod
            }

            val pattern = listOf(
                mock<InvokeStmt> {
                    on { invokeExpr } doReturn invokeExpression
                },
                mock<ReturnVoidStmt>()
            ).map { SootNode(it) }

            assertThat(IncompleteInitPatternFilter().retain(pattern)).isFalse()
        }

        it("retains patterns that start with a special invoke, but not an init call") {
            val initMethod = mock<SootMethod> {
                on { name } doReturn "not-init"
            }
            val invokeExpression = mock<JSpecialInvokeExpr> {
                on { method } doReturn initMethod
            }

            val pattern = listOf(
                mock<InvokeStmt> {
                    on { invokeExpr } doReturn invokeExpression
                },
                mock<ReturnVoidStmt>()
            ).map { SootNode(it) }

            assertThat(IncompleteInitPatternFilter().retain(pattern)).isTrue()
        }

        it("retains patterns that start with a regular invoke") {
            val invokeExpression = mock<InvokeExpr>()

            val pattern = listOf(
                mock<InvokeStmt> {
                    on { invokeExpr } doReturn invokeExpression
                },
                mock<ReturnVoidStmt>()
            ).map { SootNode(it) }

            assertThat(IncompleteInitPatternFilter().retain(pattern)).isTrue()
        }

        it("retains patterns that do not start with an invoke") {
            val pattern = listOf(
                mock<ReturnVoidStmt>()
            ).map { SootNode(it) }

            assertThat(IncompleteInitPatternFilter().retain(pattern)).isTrue()
        }

        it("retains empty patterns") {
            val pattern = emptyList<SootNode>()

            assertThat(IncompleteInitPatternFilter().retain(pattern)).isTrue()
        }

        it("retains lists of non-Soot nodes") {
            val pattern = listOf(TestNode())

            assertThat(IncompleteInitPatternFilter().retain(pattern)).isTrue()
        }
    }
})
