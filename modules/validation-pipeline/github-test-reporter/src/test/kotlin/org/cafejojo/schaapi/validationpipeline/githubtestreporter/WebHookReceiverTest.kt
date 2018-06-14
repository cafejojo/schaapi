package org.cafejojo.schaapi.validationpipeline.githubtestreporter

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.cafejojo.schaapi.validationpipeline.events.ValidateRequestReceivedEvent
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import org.springframework.context.ApplicationEventPublisher

object WebHookReceiverTest : Spek({
    it("can receive GitHub check suite requested web hooks") {
        val checkReporter = mock<CheckReporter>()
        var event: ValidateRequestReceivedEvent? = null
        val webHookReceiver = WebHookReceiver(
            checkReporter,
            ApplicationEventPublisher { event = it as? ValidateRequestReceivedEvent }
        )

        webHookReceiver.processWebHook("check_suite", json {
            "action" to "requested"
            "installation" to json {
                "id" to "12345"
            }
            "check_suite" to json {
                "head_branch" to "patch01"
                "head_sha" to "abc123"
            }
            "repository" to json {
                "name" to "schaapi"
                "full_name" to "cafejojo/schaapi"
            }
        }.toString())

        verify(checkReporter).reportStarted(12345, "cafejojo/schaapi", "patch01", "abc123")
        assertThat(event).isNotNull()
        assertThat(event?.directory?.path).contains("cafejojo/schaapi")
    }

    it("cannot process general GitHub check suite web hooks") {
        val checkReporter = mock<CheckReporter>()
        val webHookReceiver = WebHookReceiver(checkReporter, ApplicationEventPublisher {})

        val body = json {
            "action" to "general-action"
            "installation" to json {
                "id" to "12345"
            }
            "check_suite" to json {
                "head_branch" to "patch01"
                "head_sha" to "abc123"
            }
            "repository" to json {
                "name" to "schaapi"
                "full_name" to "cafejojo/schaapi"
            }
        }.toString()

        assertThatThrownBy { webHookReceiver.processWebHook("check_suite", body) }
            .isInstanceOf(IncomingWebHookException::class.java)
            .hasMessageContaining("general-action")

        verify(checkReporter, never()).reportStarted(any(), any(), any(), any())
    }

    it("cannot process general GitHub web hooks") {
        val webHookReceiver = WebHookReceiver(mock(), ApplicationEventPublisher {})

        assertThatThrownBy { webHookReceiver.processWebHook("general_event", "") }
            .isInstanceOf(IncomingWebHookException::class.java)
            .hasMessageContaining("general_event")
    }
})
