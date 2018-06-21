package org.cafejojo.schaapi.validationpipeline.githubtestreporter

import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.validationpipeline.CIJobException
import org.cafejojo.schaapi.validationpipeline.TestResults
import org.cafejojo.schaapi.validationpipeline.events.CIJobFailedEvent
import org.cafejojo.schaapi.validationpipeline.events.CIJobSucceededEvent
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it

object CIJobCompletedListenerTest : Spek({
    it("can receive a ci job failed event") {
        val checkReporter = mock<CheckReporter>()
        val ciJobCompletedListener = CIJobCompletedListener(checkReporter)

        val failedEvent = CIJobFailedEvent(
            CIJobException("CI job execution failed for some reason."),
            GitHubCIJobProjectMetadata(checkRunId = 123456, installationId = 6543421, fullName = "cafejojo/schaapi")
        )

        ciJobCompletedListener.handleCIJobFailedEvent(failedEvent)

        verify(checkReporter).reportFailure(
            6543421,
            "cafejojo/schaapi",
            123456,
            CheckMessage("Check failed :(", "CI job execution failed for some reason.")
        )
    }

    it("can receive a ci job succeeded event for failing tests") {
        val checkReporter = mock<CheckReporter>()
        val ciJobCompletedListener = CIJobCompletedListener(checkReporter)

        val succeededEvent = CIJobSucceededEvent(
            FakeFailingTestResults(),
            GitHubCIJobProjectMetadata(checkRunId = 123456, installationId = 6543421, fullName = "cafejojo/schaapi")
        )

        ciJobCompletedListener.handleCIJobSucceededEvent(succeededEvent)

        val messageCaptor = argumentCaptor<CheckMessage>()
        verify(checkReporter).reportFailure(
            eq(6543421),
            eq("cafejojo/schaapi"),
            eq(123456),
            messageCaptor.capture()
        )
        messageCaptor.firstValue.apply {
            assertThat(title).isEqualTo("Check failed :(")
            assertThat(summary).contains("| 5")
            assertThat(summary).contains("| 3")
            assertThat(summary).contains("| 2")
            assertThat(summary).contains("| 0")
            assertThat(summary).contains("failure message 1<br><br>failure message 2")
        }
    }

    it("can receive a ci job succeeded event for passing tests") {
        val checkReporter = mock<CheckReporter>()
        val ciJobCompletedListener = CIJobCompletedListener(checkReporter)

        val succeededEvent = CIJobSucceededEvent(
            FakePassingTestResults(),
            GitHubCIJobProjectMetadata(checkRunId = 123456, installationId = 6543421, fullName = "cafejojo/schaapi")
        )

        ciJobCompletedListener.handleCIJobSucceededEvent(succeededEvent)

        val messageCaptor = argumentCaptor<CheckMessage>()
        verify(checkReporter).reportSuccess(
            eq(6543421),
            eq("cafejojo/schaapi"),
            eq(123456),
            messageCaptor.capture()
        )
        messageCaptor.firstValue.apply {
            assertThat(title).isEqualTo("Check successful!")
            assertThat(summary).contains("| 5")
            assertThat(summary).contains("| 5")
            assertThat(summary).contains("| 0")
            assertThat(summary).contains("| 0")
        }
    }
})

private class FakeFailingTestResults : TestResults {
    override val subResults = mapOf<String, TestResults>()
    override val totalCount = 5
    override val passCount = 3
    override val ignoreCount = 0
    override val failureCount = 2
    override val failures = listOf("failure message 1", "failure message 2")
    override val isEmpty = false
}

private class FakePassingTestResults : TestResults {
    override val subResults = mapOf<String, TestResults>()
    override val totalCount = 5
    override val passCount = 5
    override val ignoreCount = 0
    override val failureCount = 0
    override val failures = emptyList<String>()
    override val isEmpty = false
}
