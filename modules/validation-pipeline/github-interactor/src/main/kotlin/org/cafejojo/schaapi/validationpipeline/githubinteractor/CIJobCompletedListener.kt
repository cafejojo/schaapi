package org.cafejojo.schaapi.validationpipeline.githubinteractor

import org.cafejojo.schaapi.validationpipeline.TestResults
import org.cafejojo.schaapi.validationpipeline.TestableSourceFinder
import org.cafejojo.schaapi.validationpipeline.events.CIJobFailedEvent
import org.cafejojo.schaapi.validationpipeline.events.CIJobSucceededEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * An event listener for completed CI job runs.
 */
@Component
class CIJobCompletedListener(
    private val checkReporter: CheckReporter,
    private val testableSourceFinder: TestableSourceFinder
) {
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
                CheckMessage("Check failed :(", formatSummary(event.testResults), formatDetails(event.testResults))
            )
        } else {
            checkReporter.reportSuccess(
                metadata.installationId,
                metadata.fullName,
                metadata.checkRunId,
                CheckMessage("Check successful!", formatSummary(event.testResults), formatDetails(event.testResults))
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

    private fun formatSummary(testResults: TestResults) = with(testResults) {
        """
            _Schaapi is an experimental tool and the accuracy of its results is not guaranteed._

            **$passCount** out of **$totalCount** tests passed.

            |                       |                                                   |
            | --------------------- | ------------------------------------------------- |
            | **Total**             | $totalCount                                       |
            | **Pass**              | $passCount                                        |
            | **Fail**              | $failureCount                                     |
            | **Ignored**           | $ignoreCount                                      |
        """.trimIndent()
    }

    private fun formatDetails(testResults: TestResults) = with(testResults) {
        testResults.failures.entries.mapIndexed { index, (file, message) ->
            val testableSource = testableSourceFinder.find(
                file.parentFile.resolve(file.nameWithoutExtension + ".java"),
                file.parentFile.resolve("Patterns.class")
            )

            ("**Failure ${index + 1}**\n" +
                message + "\n\n" +
                "The failure occurred in the following pattern:\n" +
                "```java \n" +
                testableSource + "\n" +
                "```")
        }.joinToString("\n\n")
    }
}
