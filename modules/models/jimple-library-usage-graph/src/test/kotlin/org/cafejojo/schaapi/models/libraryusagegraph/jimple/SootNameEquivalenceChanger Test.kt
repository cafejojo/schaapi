package org.cafejojo.schaapi.models.libraryusagegraph.jimple

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.IntType
import soot.jimple.Jimple

/**
 * Unit tests for [SootNameEquivalenceChanger].
 */
internal object SootNameEquivalenceChangerTest : Spek({
    describe("Soot hack") {
        it("causes local equivalence after the hack is activated") {
            SootNameEquivalenceChanger.activate()

            val localA = Jimple.v().newLocal("localA", IntType.v())
            val localB = Jimple.v().newLocal("localB", IntType.v())

            assertThat(localA.equivTo(localB)).isTrue()
        }
    }
})
