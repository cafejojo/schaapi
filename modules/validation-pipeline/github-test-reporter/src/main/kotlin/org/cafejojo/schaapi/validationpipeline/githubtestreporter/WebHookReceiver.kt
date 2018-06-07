package org.cafejojo.schaapi.validationpipeline.githubtestreporter

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

/**
 * Receives GitHub web hooks.
 */
@Controller
@EnableAutoConfiguration
class WebHookReceiver(val checkReporter: CheckReporter) {
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
            "check_suite" -> {
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
                        For repository ${checkSuiteEvent.repository.owner.login}/${checkSuiteEvent.repository.name}

                        with status `in_progress` and started_at set to the current time

                    - Start a run of the regression tests
                    """.trimIndent()
                )

                with(checkSuiteEvent) {
                    checkReporter.reportStarted(
                        installation.id,
                        repository.owner.login,
                        repository.name,
                        checkSuite.headBranch,
                        checkSuite.headSha
                    )
                }
            }
            else -> throw IncomingWebHookException("Cannot process web hooks for events of type $eventType.")
        }

        return "Webhook received."
    }
}

internal class IncomingWebHookException(message: String) : Exception(message)
