package org.cafejojo.schaapi.patterndetector

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it




internal class PatternDetectorTest : Spek({
    describe("Schaapi") {
        it("should work for strings") {
            assertThat(PatternDetector().testMe("hWR3L"))
                .isEqualTo("hWR3LhWR3L")
        }

        it("should work for empty strings") {
            assertThat(PatternDetector().testMe(""))
                .isEqualTo("")
        }
    }
})
