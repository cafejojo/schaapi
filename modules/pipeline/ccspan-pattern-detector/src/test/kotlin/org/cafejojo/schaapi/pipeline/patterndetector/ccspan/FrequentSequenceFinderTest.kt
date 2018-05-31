package org.cafejojo.schaapi.pipeline.patterndetector.ccspan

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class FrequentSequenceFinderTest : Spek({
    describe("Good weather") {
        it("finds all frequent closed contiguous sequences") {
            val sequence1 = listOf(3, 1, 1, 2, 3)
            val sequence2 = listOf(1, 2, 3, 2)
            val sequence3 = listOf(3, 1, 2, 3)
            val sequence4 = listOf(1, 2, 2, 3, 1)

            val frequentSequences = FrequentSequenceFinder(
                listOf(sequence1, sequence2, sequence3, sequence4),
                2
            ).findFrequentSequences()

            assertThat(frequentSequences).containsExactlyInAnyOrder(
                listOf(3, 1),
                listOf(1, 2),
                listOf(2, 3),
                listOf(1, 2, 3)
            )
        }
    }
})
