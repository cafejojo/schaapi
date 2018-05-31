package org.cafejojo.schaapi.githubtestreporter

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
class WebHookReceiver {
    private val mapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)

    /**
     * Processes a GitHub Web Hook.
     *
     * @param eventType the type of event, see <a href="https://developer.github.com/webhooks/#events">GitHub docs</a>
     * @param body the request body
     */
    @RequestMapping("/process-webhook")
    @ResponseBody
    fun processWebhook(@RequestHeader("X-GitHub-Event") eventType: String, @RequestBody body: String): String {
        when (eventType) {
            "check_suite" -> {
                val checkSuiteEvent = mapper.readValue(body, CheckSuiteEvent::class.java)

                println("""
                    I received a check suite event. Here's what I should do next:

                    - Create check run with the following properties:
                        Installation id is ${checkSuiteEvent.installation.id}
                        For commit ${checkSuiteEvent.checkSuite.headSha}
                        On branch ${checkSuiteEvent.checkSuite.headBranch}
                        with status `in_progress` and started_at set to the current time

                    - Start a run of the regression tests
                    """.trimIndent()
                )
            }
            else -> throw IllegalStateException("Cannot process webhooks for events of type $eventType.")
        }

        return "Webhook received."
    }
}
