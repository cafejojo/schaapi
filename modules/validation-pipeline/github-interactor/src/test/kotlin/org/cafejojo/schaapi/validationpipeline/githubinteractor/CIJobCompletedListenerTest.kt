package org.cafejojo.schaapi.validationpipeline.githubinteractor

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.validationpipeline.CIJobException
import org.cafejojo.schaapi.validationpipeline.TestResults
import org.cafejojo.schaapi.validationpipeline.TestableSourceFinder
import org.cafejojo.schaapi.validationpipeline.events.CIJobFailedEvent
import org.cafejojo.schaapi.validationpipeline.events.CIJobSucceededEvent
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import java.io.File

object CIJobCompletedListenerTest : Spek({
    it("can receive a CI job failed event") {
        val checkReporter = mock<CheckReporter>()
        val testableSourceFinder = mock<TestableSourceFinder>()
        val ciJobCompletedListener = CIJobCompletedListener(checkReporter, testableSourceFinder)

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

    it("can receive a CI job succeeded event for failing tests") {
        val checkReporter = mock<CheckReporter>()
        val testableSourceFinder = mock<TestableSourceFinder> {
            on { find(any(), any()) } doReturn listOf("test-source-1", "test-source-2")
        }
        val ciJobCompletedListener = CIJobCompletedListener(checkReporter, testableSourceFinder)

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
            assertThat(text).contains("failure message 1")
            assertThat(text).contains("test-source-1")
            assertThat(text).contains("failure message 2")
            assertThat(text).contains("test-source-2")
        }
    }

    it("can receive a CI job succeeded event for passing tests") {
        val checkReporter = mock<CheckReporter>()
        val testableSourceFinder = mock<TestableSourceFinder>()
        val ciJobCompletedListener = CIJobCompletedListener(checkReporter, testableSourceFinder)

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
    override val failures = mapOf(
        mock<File> {
            on { parentFile } doReturn mock<File>()
            on { name } doReturn "test_file_1.class"
        } to "failure message 1",
        mock<File> {
            on { parentFile } doReturn mock<File>()
            on { name } doReturn "test_file_2.class"
        } to "failure message 2"
    )
    override val isEmpty = false
}

private class FakePassingTestResults : TestResults {
    override val subResults = mapOf<String, TestResults>()
    override val totalCount = 5
    override val passCount = 5
    override val ignoreCount = 0
    override val failureCount = 0
    override val failures = mapOf<File, String>()
    override val isEmpty = false
}
