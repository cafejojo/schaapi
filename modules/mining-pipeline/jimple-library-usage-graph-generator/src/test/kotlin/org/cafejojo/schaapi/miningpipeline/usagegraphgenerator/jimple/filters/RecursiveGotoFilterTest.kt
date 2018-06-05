package org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.filters

import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.jimple.GotoStmt

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
    }
})
