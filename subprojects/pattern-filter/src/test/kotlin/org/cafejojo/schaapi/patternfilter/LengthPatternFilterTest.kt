package org.cafejojo.schaapi.patternfilter

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class LengthPatternFilterTest : Spek({
    describe("when filtering a pattern") {
        it("should return true for a pattern equal to the default length") {
            assertThat(LengthPatternFilter().retain(listOf(TestNode(), TestNode()))).isTrue()
        }

        it("should return false for a pattern shorter than the default length") {
            assertThat(LengthPatternFilter().retain(listOf(TestNode()))).isFalse()
        }

        it("should return false for a pattern shorter than the default length") {
            assertThat(LengthPatternFilter().retain(listOf(TestNode()))).isFalse()
        }

        it("should return true for a pattern longer than the passed length") {
            assertThat(LengthPatternFilter(1).retain(listOf(TestNode(), TestNode()))).isTrue()
        }

        it("should give an exception if the given pattern length is 0") {
            assertThatThrownBy { LengthPatternFilter(0) }
                .isInstanceOf(TooSmallMinimumPatternLengthException::class.java)
        }
    }
})
