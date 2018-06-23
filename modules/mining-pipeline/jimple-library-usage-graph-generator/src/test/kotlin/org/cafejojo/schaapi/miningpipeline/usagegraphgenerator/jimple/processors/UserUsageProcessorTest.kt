package org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.processors

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.filters.LIBRARY_CLASS
import org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.filters.NON_LIBRARY_CLASS
import org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.filters.USER_CLASS
import org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.filters.constructInvokeExprMock
import org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.libraryProject
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.Body
import soot.Local
import soot.PatchingChain
import soot.Unit
import soot.Value
import soot.jimple.Constant
import soot.jimple.IfStmt
import soot.jimple.internal.JEqExpr
import soot.util.IterableSet

/**
 * Unit tests for [UserUsageProcessor].
 */
internal object UserUsageProcessorTest : Spek({
    fun createBody(units: Collection<Unit>) =
        mock<Body> {
            on { it.units } doReturn  PatchingChain(IterableSet(units))
        }

    describe("user usage processor") {
        val libraryValue = constructInvokeExprMock(LIBRARY_CLASS)
        val userValue = constructInvokeExprMock(USER_CLASS)
        val nonLibraryValue = constructInvokeExprMock(NON_LIBRARY_CLASS)

        lateinit var userUsageProcessor: UserUsageProcessor

        beforeEachTest {
            userUsageProcessor = UserUsageProcessor(libraryProject)
        }

        it("removes user usages from if-statements") {
            val ifStmt = mock<IfStmt> {
                on { condition } doReturn userValue
            }

            userUsageProcessor.process(createBody(listOf(ifStmt)))

            val captor = argumentCaptor<Value>()
            verify(ifStmt).condition = captor.capture()

            val newCondition = captor.firstValue as? JEqExpr ?: throw AssertionError("Captured value is not a JEqExpr.")
            assertThat(newCondition.op1).isInstanceOf(Local::class.java)
            assertThat(newCondition.op2).isInstanceOf(Constant::class.java)
        }

        it("does not touch if-statements using the library") {
            val ifStmt = mock<IfStmt> {
                on { condition } doReturn libraryValue
            }

            userUsageProcessor.process(createBody(listOf(ifStmt)))

            verify(ifStmt, never()).condition = any()
        }
    }
})
