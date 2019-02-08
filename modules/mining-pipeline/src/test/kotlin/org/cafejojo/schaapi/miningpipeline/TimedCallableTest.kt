package org.cafejojo.schaapi.miningpipeline

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Unit tests for [TimedCallable].
 */
internal object TimedCallableTest : Spek({
    describe("a timed callable") {
        it("returns the callable's value if the task finishes withing the time limit") {
            val callable = TimedCallable(10) { 4 }

            assertThat(callable.call()).isEqualTo(4)
        }

        it("returns null if the task does not finish within the time limit") {
            val callable = TimedCallable(1) {
                Thread.sleep(5000)
                9
            }

            assertThat(callable.call()).isNull()
        }

        it("returns null if a waiting task does not finish within the time limit") {
            val callable = TimedCallable(1) {
                TimedCallable(5) {
                    Thread.sleep(9000)
                    14
                }.call()
            }

            assertThat(callable.call()).isNull()
        }
    }
})
