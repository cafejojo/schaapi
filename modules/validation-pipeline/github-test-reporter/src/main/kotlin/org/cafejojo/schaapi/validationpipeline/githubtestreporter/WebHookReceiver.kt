package org.cafejojo.schaapi.validationpipeline.githubtestreporter

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.cafejojo.schaapi.validationpipeline.events.ValidationRequestReceivedEvent
import org.cafejojo.schaapi.validationpipeline.githubtestreporter.events.CheckSuiteEvent
import org.cafejojo.schaapi.validationpipeline.githubtestreporter.events.InstallationEvent
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import java.io.File

/**
 * Receives GitHub web hooks.
 */
@Controller
@EnableAutoConfiguration
class WebHookReceiver(private val checkReporter: CheckReporter, private val publisher: ApplicationEventPublisher) {
    private val mapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)

    /**
     * Processes a GitHub web hook.
     *
     * @param eventType the type of event, see <a href="https://developer.github.com/webhooks/#events">GitHub docs</a>
     * @param body the request body
     */
    @RequestMapping("/process-webhook")
    @ResponseBody
    fun processWebHook(@RequestHeader("X-GitHub-Event") eventType: String, @RequestBody body: String): String {
        when (eventType) {
            "check_suite" -> handleCheckSuiteEvent(body)
            "installation" -> handleInstallationEvent(body)
            else -> throw IncomingWebHookException("Cannot process web hooks for events of type $eventType.")
        }

        return "Webhook received."
    }

    private fun handleCheckSuiteEvent(body: String) {
        val checkSuiteEvent = mapper.readValue(body, CheckSuiteEvent::class.java)

        if (!checkSuiteEvent.isRequested()) {
            throw IncomingWebHookException(
                "Cannot process check suite web hooks for action ${checkSuiteEvent.action}."
            )
        }

        println("""
                I received a check suite event. Here's what I should do next:

                - Create check run with the following properties:
                    Installation id is ${checkSuiteEvent.installation.id}
                    For commit ${checkSuiteEvent.checkSuite.headSha}
                    On branch ${checkSuiteEvent.checkSuite.headBranch}
                    For repository ${checkSuiteEvent.repository.fullName}

                    with status `in_progress` and started_at set to the current time

                - Start a run of the regression tests
                """.trimIndent()
        )

        with(checkSuiteEvent) {
            checkReporter.reportStarted(
                installation.id,
                repository.fullName,
                checkSuite.headBranch,
                checkSuite.headSha
            )

            publisher.publishEvent(ValidationRequestReceivedEvent(
                identifier = checkSuiteEvent.checkSuite.headSha,
                directory = File(Properties.testsStorageLocation, repository.fullName),
                downloadUrl = "https://github.com/${repository.fullName}/archive/${checkSuite.headSha}.zip"
            ))
        }
    }

    private fun handleInstallationEvent(body: String) {
        val installationEvent = mapper.readValue(body, InstallationEvent::class.java)

        when {
            installationEvent.isCreated() ->
                installationEvent.repositories?.forEach {
                    File(Properties.testsStorageLocation, it.fullName).mkdirs()
                }
            installationEvent.isDeleted() ->
                installationEvent.installation.account?.let { account ->
                    File(Properties.testsStorageLocation, account.login).deleteRecursively()
                }
        }
    }
}

internal class IncomingWebHookException(message: String) : Exception(message)
