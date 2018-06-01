package org.cafejojo.schaapi.miningpipeline.patternfilter.jimple

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

internal object LengthPatternFilterRuleTest : Spek({
    describe("when filtering a pattern") {
        it("should return true for a pattern equal to the default length") {
            assertThat(LengthPatternFilterRule().retain(listOf(mock {}, mock {}))).isTrue()
        }

        it("should return false for a pattern shorter than the default length") {
            assertThat(LengthPatternFilterRule().retain(listOf(mock {}))).isFalse()
        }

        it("should return false for a pattern shorter than the default length") {
            assertThat(LengthPatternFilterRule().retain(listOf(mock {}))).isFalse()
        }

        it("should return true for a pattern longer than the passed length") {
            assertThat(LengthPatternFilterRule(1).retain(listOf(mock {}, mock {}))).isTrue()
        }

        it("should retain list if minimum length is negative") {
            assertThat(LengthPatternFilterRule(-1).retain(listOf(mock {}))).isTrue()
        }
    }
})
