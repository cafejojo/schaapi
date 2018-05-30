package org.cafejojo.schaapi.pipeline

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import kotlin.concurrent.thread

internal class FlatProgressBarTest : Spek({
    describe("flat progress bar") {
        context("illegal states and arguments") {
            it("cannot be constructed with a target smaller than the progress") {
                assertThatThrownBy { FlatProgressBar(86, 67) }
                    .isInstanceOf(IllegalArgumentException::class.java)
                    .hasMessage("Target must be larger than current progress.")
            }

            it("cannot be constructed with a negative progress") {
                assertThatThrownBy { FlatProgressBar(-78, 53) }
                    .isInstanceOf(IllegalArgumentException::class.java)
                    .hasMessage("Progress must be between 0 and target, inclusive.")
            }

            it("cannot change the target after it is done") {
                val progressBar = FlatProgressBar(56, 56)

                assertThatThrownBy { progressBar.setTarget(56) }
                    .isInstanceOf(IllegalStateException::class.java)
                    .hasMessage("Cannot change the target once the process has completed.")
            }

            it("cannot set the target lower than the current progress") {
                val progressBar = FlatProgressBar(49, 77)

                assertThatThrownBy { progressBar.setTarget(40) }
                    .isInstanceOf(IllegalArgumentException::class.java)
                    .hasMessage("Target cannot be smaller than current progress, 49.")
            }

            it("cannot change the progress after it is done") {
                val progressBar = FlatProgressBar(21, 21)

                assertThatThrownBy { progressBar.setProgress(21) }
                    .isInstanceOf(IllegalStateException::class.java)
                    .hasMessage("Cannot change the progress once the process has completed.")
            }

            it("cannot set the progress to a negative number") {
                val progressBar = FlatProgressBar(54, 96)

                assertThatThrownBy { progressBar.setProgress(-24) }
                    .isInstanceOf(IllegalArgumentException::class.java)
                    .hasMessage("Progress cannot be negative.")
            }

            it("cannot set the progress higher than the target") {
                val progressBar = FlatProgressBar(30, 43)

                assertThatThrownBy { progressBar.setProgress(47) }
                    .isInstanceOf(IllegalArgumentException::class.java)
                    .hasMessage("Progress cannot be higher than the target, 43.")
            }

            it("cannot increment the progress beyond target") {
                val progressBar = FlatProgressBar(51, 51)

                assertThatThrownBy { progressBar.incrementProgress() }
                    .isInstanceOf(IllegalStateException::class.java)
                    .hasMessage("Cannot increment the progress once the process has completed.")
            }
        }

        context("thread-safety") {
            it("can be incremented safely from multiple threads") {
                fun incrementLoop(progressBar: FlatProgressBar) {
                    repeat(10000) {
                        progressBar.incrementProgress()
                    }
                }

                val progressBar = FlatProgressBar(0, 30000)
                val threads = Array(3, { thread { incrementLoop(progressBar) } })
                threads.forEach { it.join() }

                assertThat(progressBar.getProgress()).isEqualTo(30000)
            }

            it("can change its target safely from multiple threads") {
                fun setProgressLoop(progressBar: FlatProgressBar) {
                    for (i in 1..10000) {
                        progressBar.setTarget(i)
                    }
                }

                val progressBar = FlatProgressBar(0, 57)
                val threads = Array(3, { thread { setProgressLoop(progressBar) } })
                threads.forEach { it.join() }

                assertThat(progressBar.getTarget()).isEqualTo(10000)
            }

            it("can change its progress safely from multiple threads") {
                fun setProgressLoop(progressBar: FlatProgressBar) {
                    for (i in 1..10000) {
                        progressBar.setProgress(i)
                    }
                }

                val progressBar = FlatProgressBar(0, 19465)
                val threads = Array(3, { thread { setProgressLoop(progressBar) } })
                threads.forEach { it.join() }

                assertThat(progressBar.getProgress()).isEqualTo(10000)
            }
        }

        context("progress normalization") {
            it("calculates the normalized value") {
                val progressBar = FlatProgressBar(64, 128)

                assertThat(progressBar.getNormalizedProgress()).isEqualTo(0.5)
            }

            it("applies the given mapper to the normalized value") {
                val progressBar = FlatProgressBar(13, 52)

                assertThat(progressBar.getNormalizedProgress { 3 * it }).isEqualTo(0.75)
            }
        }
    }
})

internal class HierarchicalProgressBarTest : Spek({
    describe("hierarchical progress bar") {
        context("bad-weather scenarios") {
            it("calculates the normalized in a thread-safe manner") {
                fun adjustProgressBar(flatProgressBar: FlatProgressBar) {
                    repeat(100000) {
                        synchronized(flatProgressBar) {
                            flatProgressBar.setTarget(flatProgressBar.getTarget() * 2)
                            flatProgressBar.setProgress(flatProgressBar.getProgress() * 2)
                        }

                        synchronized(flatProgressBar) {
                            flatProgressBar.setProgress(flatProgressBar.getProgress() / 2)
                            flatProgressBar.setTarget(flatProgressBar.getTarget() / 2)
                        }
                    }
                }

                val subProgressBars = listOf(
                    FlatProgressBar(1, 2),
                    FlatProgressBar(1, 2),
                    FlatProgressBar(1, 2)
                )
                val progressBar = HierarchicalProgressBar(subProgressBars.toSet())

                val threads = Array(3, { thread { adjustProgressBar(subProgressBars[it]) } })
                val normalizedProgress = progressBar.getNormalizedProgress()
                threads.forEach { it.join() }

                assertThat(normalizedProgress).isEqualTo(0.5)
            }
        }

        context("good-weather scenarios") {
            it("cannot be constructed without sub-bars") {
                assertThatThrownBy { HierarchicalProgressBar(emptySet()) }
                    .isInstanceOf(IllegalArgumentException::class.java)
                    .hasMessage("Hierarchical progress bar must contain at least one progress bar.")
            }

            it("uses the sums of the sub-processes to calculate the progress and target") {
                val progressBar = HierarchicalProgressBar(setOf(
                    FlatProgressBar(54, 114),
                    FlatProgressBar(29, 61),
                    FlatProgressBar(88, 143)
                ))

                assertThat(progressBar.getProgress()).isEqualTo(54 + 29 + 88)
                assertThat(progressBar.getTarget()).isEqualTo(114 + 61 + 143)
            }

            it("calculates its progress and target recursively") {
                val progressBar = HierarchicalProgressBar(setOf(
                    FlatProgressBar(81, 132),
                    HierarchicalProgressBar(setOf(
                        HierarchicalProgressBar(setOf(
                            FlatProgressBar(24, 50),
                            FlatProgressBar(61, 97)
                        )),
                        HierarchicalProgressBar(setOf(
                            FlatProgressBar(95, 150)
                        ))
                    ))
                ))

                assertThat(progressBar.getProgress()).isEqualTo(81 + 24 + 61 + 95)
                assertThat(progressBar.getTarget()).isEqualTo(132 + 50 + 97 + 150)
            }

            it("uses the sums of the sub-processes to calculate the normalized progress") {
                val progressBar = HierarchicalProgressBar(setOf(
                    FlatProgressBar(93, 142),
                    FlatProgressBar(64, 116),
                    FlatProgressBar(44, 96)
                ))

                assertThat(progressBar.getNormalizedProgress()).isEqualTo((93 + 64 + 44).toDouble() / (142 + 116 + 96))
            }

            it("does not apply the mapper recursively") {
                val progressBar = HierarchicalProgressBar(setOf(
                    FlatProgressBar(92, 149),
                    HierarchicalProgressBar(setOf(
                        FlatProgressBar(36, 150),
                        FlatProgressBar(63, 111)
                    )),
                    FlatProgressBar(92, 136)
                ))

                assertThat(progressBar.getNormalizedProgress { it + 0.05 })
                    .isEqualTo((92 + 36 + 63 + 92).toDouble() / (149 + 150 + 111 + 136) + 0.05)
            }

            it("is done when the sub-bars are done") {
                val progressBar = HierarchicalProgressBar(setOf(
                    HierarchicalProgressBar(setOf(
                        FlatProgressBar(71, 71),
                        FlatProgressBar(21, 21),
                        FlatProgressBar(79, 79)
                    )),
                    FlatProgressBar(40, 40)
                ))

                assertThat(progressBar.isDone()).isTrue()
            }
        }
    }
})
