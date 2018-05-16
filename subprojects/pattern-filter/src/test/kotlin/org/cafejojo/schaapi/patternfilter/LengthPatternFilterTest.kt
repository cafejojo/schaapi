package org.cafejojo.schaapi.patternfilter

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class LengthPatternFilterTest : Spek({
    describe("when filtering a pattern") {
        it("should give true for a pattern equal to the default length") {
            assertThat(LengthPatternFilter().retain(listOf(TestNode(), TestNode()))).isTrue()
        }

        it("should give false for a pattern shorter than the default length") {
            assertThat(LengthPatternFilter().retain(listOf(TestNode()))).isFalse()
        }

        it("should give true for a pattern longer than the passed length") {
            assertThat(LengthPatternFilter(1).retain(listOf(TestNode(), TestNode()))).isTrue()
        }
    }
})
