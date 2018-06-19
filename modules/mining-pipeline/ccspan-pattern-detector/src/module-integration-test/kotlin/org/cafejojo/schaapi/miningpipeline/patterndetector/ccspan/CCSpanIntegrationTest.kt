package org.cafejojo.schaapi.miningpipeline.patterndetector.ccspan

import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimpleNode
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.SootNameEquivalenceChanger
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.IntType
import soot.jimple.IntConstant
import soot.jimple.Jimple

/**
 * Integration tests for the integration between [PatternDetector] and [GeneralizedNodeComparator].
 */
internal object CCSpanIntegrationTest : Spek({
    describe("integration of CCSpan with GeneralizedNodeComparator") {
        lateinit var patternDetector: PatternDetector<JimpleNode>

        beforeGroup {
            SootNameEquivalenceChanger.activate()
        }

        beforeEachTest {
            val nodeComparator = GeneralizedNodeComparator()
            patternDetector = PatternDetector(0, 10, nodeComparator)
        }

        it("can detect very simple patterns") {
            val localA = Jimple.v().newLocal("local", IntType.v())
            val stmtA2 = Jimple.v().newAssignStmt(localA, IntConstant.v(504))
            val nodeA2 = JimpleNode(stmtA2)
            val stmtA1 = Jimple.v().newAssignStmt(localA, IntConstant.v(591))
            val nodeA1 = JimpleNode(stmtA1, mutableListOf(nodeA2))

            val localB = Jimple.v().newLocal("local", IntType.v())
            val stmtB2 = Jimple.v().newAssignStmt(localB, IntConstant.v(504))
            val nodeB2 = JimpleNode(stmtB2)
            val stmtB1 = Jimple.v().newAssignStmt(localB, IntConstant.v(591))
            val nodeB1 = JimpleNode(stmtB1, mutableListOf(nodeB2))

            val patterns = patternDetector.findPatterns(listOf(nodeA1, nodeB1))

            assertThat(patterns).containsExactly(listOf(nodeA1, nodeA2))
        }
    }
})
