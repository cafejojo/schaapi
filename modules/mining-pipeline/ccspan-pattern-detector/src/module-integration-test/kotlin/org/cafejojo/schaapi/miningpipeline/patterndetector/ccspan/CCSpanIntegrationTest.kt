package org.cafejojo.schaapi.miningpipeline.patterndetector.ccspan

import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimpleNode
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimplePathEnumerator
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.SootNameEquivalenceChanger
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.IntType
import soot.jimple.IntConstant
import soot.jimple.Jimple

/**
 * Integration tests for the integration between [CCSpanPatternDetector] and [GeneralizedNodeComparator].
 */
internal object CCSpanIntegrationTest : Spek({
    describe("integration of CCSpan with GeneralizedNodeComparator") {
        lateinit var patternDetector: CCSpanPatternDetector<JimpleNode>
        lateinit var enumerator: (JimpleNode) -> JimplePathEnumerator

        beforeGroup {
            SootNameEquivalenceChanger.activate()
        }

        beforeEachTest {
            val nodeComparator = GeneralizedNodeComparator()
            enumerator = { node -> JimplePathEnumerator(node, 10) }
            patternDetector = CCSpanPatternDetector(0, nodeComparator)
        }

        it("can detect very simple patterns") {
            val localA = Jimple.v().newLocal("local", IntType.v())
            val nodeA2 = JimpleNode(Jimple.v().newAssignStmt(localA, IntConstant.v(504)))
            val nodeA1 = JimpleNode(Jimple.v().newAssignStmt(localA, IntConstant.v(591)), mutableListOf(nodeA2))

            val localB = Jimple.v().newLocal("local", IntType.v())
            val nodeB2 = JimpleNode(Jimple.v().newAssignStmt(localB, IntConstant.v(504)))
            val nodeB1 = JimpleNode(Jimple.v().newAssignStmt(localB, IntConstant.v(591)), mutableListOf(nodeB2))

            val sequences = listOf(nodeA1, nodeB1).flatMap { enumerator(it).enumerate() }
            val patterns = patternDetector.findPatterns(sequences)

            assertThat(patterns).containsExactly(listOf(nodeA1, nodeA2))
        }

        it("correctly uses the custom equivalence method to compare singleton lists") {
            val localA = Jimple.v().newLocal("local", IntType.v())
            val nodeA1 = JimpleNode(Jimple.v().newAssignStmt(localA, IntConstant.v(107)))

            val localB = Jimple.v().newLocal("local", IntType.v())
            val nodeB2 = JimpleNode(Jimple.v().newAssignStmt(localB, IntConstant.v(944)))
            val nodeB1 = JimpleNode(Jimple.v().newAssignStmt(localB, IntConstant.v(107)), mutableListOf(nodeB2))

            val sequences = listOf(nodeA1, nodeB1).flatMap { enumerator(it).enumerate() }
            val patterns = patternDetector.findPatterns(sequences)

            assertThat(patterns).containsExactlyInAnyOrder(
                listOf(nodeA1),
                listOf(nodeB1, nodeB2)
            )
        }
    }
})
