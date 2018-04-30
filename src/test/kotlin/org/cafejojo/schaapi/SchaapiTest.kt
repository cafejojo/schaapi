package org.cafejojo.schaapi

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

internal class SchaapiTest : Spek({
    describe("Schaapi") {
        it("should work for negative numbers") {
            assertThat(Schaapi().testMe(-49643))
                .isEqualTo(-99288)
        }

        it("should work for 0") {
            assertThat(Schaapi().testMe(0))
                .isEqualTo(0)
        }

        it("should work for positive numbers") {
            assertThat(Schaapi().testMe(27280))
                .isEqualTo(54560)
        }
    }
})
