package org.cafejojo.schaapi.validationpipeline.githubtestreporter

import org.cafejojo.schaapi.validationpipeline.TestResults
import org.cafejojo.schaapi.validationpipeline.events.CIJobFailedEvent
import org.cafejojo.schaapi.validationpipeline.events.CIJobSucceededEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * An event listener for completed CI job runs.
 */
@Component
class CIJobCompletedListener(private val checkReporter: CheckReporter) {
    /**
     * Listens to [CIJobSucceededEvent] events and reports the included results to GitHub.
     */
    @EventListener
    fun handleCIJobSucceededEvent(event: CIJobSucceededEvent) {
        val metadata = event.metadata as? GitHubCIJobProjectMetadata ?: return

        if (event.testResults.hasFailures()) {
            checkReporter.reportFailure(
                metadata.installationId,
                metadata.fullName,
                metadata.checkRunId,
                CheckMessage("Check failed :(", formatTestResults(event.testResults))
            )
        } else {
            checkReporter.reportSuccess(
                metadata.installationId,
                metadata.fullName,
                metadata.checkRunId,
                CheckMessage("Check successful!", formatTestResults(event.testResults))
            )
        }
    }

    /**
     * Listens to [CIJobFailedEvent] events and reports the result to GitHub.
     */
    @EventListener
    fun handleCIJobFailedEvent(event: CIJobFailedEvent) {
        val metadata = event.metadata as? GitHubCIJobProjectMetadata ?: return

        checkReporter.reportFailure(
            metadata.installationId,
            metadata.fullName,
            metadata.checkRunId,
            CheckMessage("Check failed :(", event.exception.message ?: "Unknown failure.")
        )
    }

    private fun formatTestResults(testResults: TestResults) = with(testResults) {
        """
            **$passCount** out of **$totalCount** tests passed.

            |               |                                                   |
            | ------------- | ------------------------------------------------- |
            | **Total**     | $totalCount                                       |
            | **Pass**      | $passCount                                        |
            | **Fail**      | $failureCount                                     |
            | **Ignored**   | $ignoreCount                                      |
            | **Failures**  | ${failures.joinToString("<br><br>")}              |
        """.trimIndent()
    }
}
