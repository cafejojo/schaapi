package org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.filters

import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.jimple.GotoStmt
import soot.jimple.Jimple
import soot.jimple.JimpleBody

internal object RecursiveGotoFilterTest : Spek({
    describe("recursive goto filter") {
        lateinit var filter: RecursiveGotoFilter

        beforeEachTest {
            filter = RecursiveGotoFilter()
        }

        it("does not retain a recursive goto statement") {
            @SuppressWarnings("UnsafeCast") // Unavoidable
            val goto = mock<GotoStmt> { on { it.target } doAnswer { it.mock as GotoStmt } }

            assertThat(filter.retain(goto)).isFalse()
        }

        it("retains a non-recursive goto statement") {
            val gotoA = mock<GotoStmt> {}
            val gotoB = mock<GotoStmt> { on { it.target } doReturn gotoA }

            assertThat(filter.retain(gotoB)).isTrue()
        }

        it("retains a goto that is in a loop of gotos") {
            val gotoA = mock<GotoStmt> {}
            val gotoB = mock<GotoStmt> {}

            gotoA.target = gotoB
            gotoB.target = gotoA

            assertThat(filter.retain(gotoA)).isTrue()
        }

        it("retains a goto that goes to a recursive goto") {
            @SuppressWarnings("UnsafeCast") // Unavoidable
            val gotoA = mock<GotoStmt> { on { it.target } doAnswer { it.mock as GotoStmt } }
            val gotoB = mock<GotoStmt> { on { it.target } doReturn gotoA }

            assertThat(filter.retain(gotoB)).isTrue()
        }

        it("does not retain a goto of which the target has been removed") {
            val stmt = Jimple.v().newBreakpointStmt()
            val goto = Jimple.v().newGotoStmt(stmt)

            val body = JimpleBody()
            body.units.addAll(listOf(goto, stmt))
            body.units.remove(stmt)

            assertThat(filter.retain(goto)).isFalse()
        }

        it("does not retain a goto of which the target is the first statement which has been removed") {
            val stmt = Jimple.v().newBreakpointStmt()
            val goto = Jimple.v().newGotoStmt(stmt)

            val body = JimpleBody()
            body.units.addAll(listOf(stmt, goto))
            body.units.remove(stmt)

            assertThat(filter.retain(goto)).isFalse()
        }

        it("retains a goto of which the target is the first statement which has been removed but there is a statement "
            + "in between") {
            val stmtA = Jimple.v().newBreakpointStmt()
            val stmtB = Jimple.v().newBreakpointStmt()
            val goto = Jimple.v().newGotoStmt(stmtA)

            val body = JimpleBody()
            body.units.addAll(listOf(stmtA, stmtB, goto))
            body.units.remove(stmtA)

            assertThat(filter.retain(goto)).isTrue()
        }

        it("empties a body of mutually recursive gotos") {
            val stmt = Jimple.v().newBreakpointStmt()
            val gotoA = Jimple.v().newGotoStmt(stmt)
            val gotoB = Jimple.v().newGotoStmt(stmt)
            val gotoC = Jimple.v().newGotoStmt(gotoB)

            val body = JimpleBody()
            body.units.addAll(listOf(stmt, gotoB, gotoC, gotoA))
            body.units.remove(stmt)
            filter.apply(body)

            assertThat(body.units).isEmpty()
        }
    }
})
