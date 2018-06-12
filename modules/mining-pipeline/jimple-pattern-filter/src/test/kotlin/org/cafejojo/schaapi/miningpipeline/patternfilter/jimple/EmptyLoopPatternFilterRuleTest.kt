package org.cafejojo.schaapi.miningpipeline.patternfilter.jimple

import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimpleNode
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.BooleanType
import soot.jimple.IntConstant
import soot.jimple.Jimple

internal object EmptyLoopPatternFilterRuleTest : Spek({
    describe("filtering of patterns containing empty loops") {
        it("rejects patterns containing an empty loop, starting with an if-statement") {
            val ifStmt = Jimple.v().newIfStmt(
                Jimple.v().newEqExpr(IntConstant.v(0), IntConstant.v(0)),
                Jimple.v().newReturnVoidStmt()
            )
            val gotoStmt = Jimple.v().newGotoStmt(ifStmt)
            val pattern = listOf(
                ifStmt,
                gotoStmt
            ).map { JimpleNode(it) }

            assertThat(EmptyLoopPatternFilterRule().retain(pattern)).isFalse()
        }

        it("rejects patterns containing an empty loop, starting with an if-statement, surrounded by other statements") {
            val nopStmt = Jimple.v().newNopStmt()
            val returnStmt = Jimple.v().newReturnVoidStmt()
            val ifStmt = Jimple.v().newIfStmt(
                Jimple.v().newEqExpr(IntConstant.v(0), IntConstant.v(0)),
                Jimple.v().newReturnVoidStmt()
            )
            val gotoStmt = Jimple.v().newGotoStmt(ifStmt)
            val pattern = listOf(
                nopStmt,
                ifStmt,
                gotoStmt,
                returnStmt
            ).map { JimpleNode(it) }

            assertThat(EmptyLoopPatternFilterRule().retain(pattern)).isFalse()
        }

        it("rejects patterns containing an empty loop, starting with an assignment") {
            val assignmentStmt = Jimple.v().newAssignStmt(
                Jimple.v().newLocal("test", BooleanType.v()),
                Jimple.v().newEqExpr(IntConstant.v(0), IntConstant.v(0))
            )
            val gotoStmt = Jimple.v().newGotoStmt(assignmentStmt)
            val pattern = listOf(
                assignmentStmt,
                gotoStmt
            ).map { JimpleNode(it) }

            assertThat(EmptyLoopPatternFilterRule().retain(pattern)).isFalse()
        }

        it("rejects patterns containing a nested loop") {
            val ifStmt = Jimple.v().newIfStmt(
                Jimple.v().newEqExpr(IntConstant.v(0), IntConstant.v(0)),
                Jimple.v().newReturnVoidStmt()
            )
            val innerIfStmt = Jimple.v().newIfStmt(
                Jimple.v().newEqExpr(IntConstant.v(0), IntConstant.v(0)),
                Jimple.v().newReturnVoidStmt()
            )
            val innerGotoStmt = Jimple.v().newGotoStmt(innerIfStmt)
            val gotoStmt = Jimple.v().newGotoStmt(ifStmt)
            val pattern = listOf(
                ifStmt,
                innerIfStmt,
                innerGotoStmt,
                gotoStmt
            ).map { JimpleNode(it) }

            assertThat(EmptyLoopPatternFilterRule().retain(pattern)).isFalse()
        }

        it("retains patterns containing a loop with one statement") {
            val ifStmt = Jimple.v().newIfStmt(
                Jimple.v().newEqExpr(IntConstant.v(0), IntConstant.v(0)),
                Jimple.v().newReturnVoidStmt()
            )
            val innerIfStmt = Jimple.v().newIfStmt(
                Jimple.v().newEqExpr(IntConstant.v(0), IntConstant.v(0)),
                Jimple.v().newReturnVoidStmt()
            )
            val gotoStmt = Jimple.v().newGotoStmt(ifStmt)
            val pattern = listOf(
                ifStmt,
                innerIfStmt,
                gotoStmt
            ).map { JimpleNode(it) }

            assertThat(EmptyLoopPatternFilterRule().retain(pattern)).isTrue()
        }

        it("retains empty patterns") {
            val pattern = emptyList<JimpleNode>()

            assertThat(EmptyLoopPatternFilterRule().retain(pattern)).isTrue()
        }
    }
})
