package org.cafejojo.schaapi.githubtestreporter

import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets.UTF_8
import java.util.Stack
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

object CheckReporterTest : Spek({
    it("can successfully obtain an installation token") {
        val (futureRequest, _) = mockHttpClient(json {
            "token" to "this-is-the-installation-token"
            "expires_at" to "end date"
        }).first()

        val appKeyGenerator = mock<AppKeyGenerator> {
            on { create() } doReturn "this-is-the-app-key"
        }

        val accessToken = CheckReporter(appKeyGenerator).requestInstallationToken(123456)

        with(futureRequest.get(0, TimeUnit.SECONDS)) {
            assertThat(url.toString()).contains("123456")
            assertThat(headers).contains(entry("Authorization", "Bearer this-is-the-app-key"))
        }

        assertThat(accessToken.token).isEqualTo("this-is-the-installation-token")
        assertThat(accessToken.expiresAt).isEqualTo("end date")
    }

    it("can successfully report the start of a new check to GitHub") {
        val (futureRequest, _) = mockHttpClient(
            json("installation token request response") {
                "token" to "this-is-the-installation-token"
                "expires_at" to "end date"
            },
            json("report started response") {
                "id" to 87575775
            }
        )[1]

        val appKeyGenerator = mock<AppKeyGenerator> {
            on { create() } doReturn "this-is-the-app-key"
        }

        val checkRun = CheckReporter(appKeyGenerator).reportStarted(
            installationId = 123456,
            owner = "cafejojo",
            repository = "schaapi",
            headBranch = "patch-1",
            headSha = "3c91f40ff5f92146e877488c1f3c7c1b3f9f252a"
        )

        with(futureRequest.get(0, TimeUnit.SECONDS)) {
            assertThat(url.toString()).contains("cafejojo/schaapi")
            assertThat(headers).contains(entry("Authorization", "Token this-is-the-installation-token"))

            assertThatJson(bodyContents()).node("head_branch").isEqualTo("patch-1")
            assertThatJson(bodyContents()).node("name").isEqualTo("breaking change detection")
            assertThatJson(bodyContents()).node("started_at").isPresent
            assertThatJson(bodyContents()).node("head_sha").isEqualTo("3c91f40ff5f92146e877488c1f3c7c1b3f9f252a")
            assertThatJson(bodyContents()).node("status").isEqualTo("in_progress")
        }

        assertThat(checkRun.id).isEqualTo(87575775)
    }

    it("can successfully report the end of a successful check to GitHub") {
        val (futureRequest, _) = mockHttpClient(
            json("installation token request response") {
                "token" to "this-is-the-installation-token"
                "expires_at" to "end date"
            },
            json("report finished response") {
                "id" to 87575775
            }
        )[1]

        val appKeyGenerator = mock<AppKeyGenerator> {
            on { create() } doReturn "this-is-the-app-key"
        }

        val checkRun = CheckReporter(appKeyGenerator).reportSuccess(
            installationId = 123456,
            owner = "cafejojo",
            repository = "schaapi",
            checkRunId = 87575775,
            checkMessage = CheckMessage(
                title = "Mandatory title of the message",
                summary = "Mandatory summary of the message",
                text = "Optional text of the message"
            )
        )

        with(futureRequest.get(0, TimeUnit.SECONDS)) {
            assertThat(url.toString()).contains("cafejojo/schaapi")
            assertThat(url.toString()).contains("87575775")
            assertThat(headers).contains(entry("Authorization", "Token this-is-the-installation-token"))

            assertThatJson(bodyContents()).apply {
                node("status").isEqualTo("completed")
                node("conclusion").isEqualTo("success")
                node("completed_at").isPresent
                node("output.title").isEqualTo("Mandatory title of the message")
                node("output.summary").isEqualTo("Mandatory summary of the message")
                node("output.text").isEqualTo("Optional text of the message")
            }
        }

        assertThat(checkRun.id).isEqualTo(87575775)
    }

    it("can successfully report the end of a failing check to GitHub") {
        val (futureRequest, _) = mockHttpClient(
            json("installation token request response") {
                "token" to "this-is-the-installation-token"
                "expires_at" to "end date"
            },
            json("report finished response") {
                "id" to 87575775
            }
        )[1]

        val appKeyGenerator = mock<AppKeyGenerator> {
            on { create() } doReturn "this-is-the-app-key"
        }

        val checkRun = CheckReporter(appKeyGenerator).reportFailure(
            installationId = 123456,
            owner = "cafejojo",
            repository = "schaapi",
            checkRunId = 87575775,
            checkMessage = CheckMessage(
                title = "Mandatory title of the message",
                summary = "Mandatory summary of the message",
                text = "Optional text of the message"
            )
        )

        with(futureRequest.get(0, TimeUnit.SECONDS)) {
            assertThat(url.toString()).contains("cafejojo/schaapi")
            assertThat(url.toString()).contains("87575775")
            assertThat(headers).contains(entry("Authorization", "Token this-is-the-installation-token"))

            assertThatJson(bodyContents()).apply {
                node("status").isEqualTo("completed")
                node("conclusion").isEqualTo("failure")
                node("completed_at").isPresent
                node("output.title").isEqualTo("Mandatory title of the message")
                node("output.summary").isEqualTo("Mandatory summary of the message")
                node("output.text").isEqualTo("Optional text of the message")
            }
        }

        assertThat(checkRun.id).isEqualTo(87575775)
    }
})

private fun mockHttpClient(vararg jsonResponses: JSONObject, code: Int = 200): List<Pair<Future<Request>, Response>> {
    val responses = jsonResponses.map { json ->
        mock<Response> {
            on { statusCode } doReturn code
            on { dataStream } doReturn ByteArrayInputStream(json.toString().toByteArray())
        }
    }

    val requestsAndResponses = responses.map { Pair(CompletableFuture<Request>(), it) }
    val requestsAndResponsesStack = Stack<Pair<CompletableFuture<Request>, Response>>().apply {
        addAll(requestsAndResponses.asReversed())
    }

    FuelConfiguration(mock {
        on { executeRequest(any()) } doAnswer { answer ->
            val (request, response) = requestsAndResponsesStack.pop()

            request.complete(answer.getArgument(0))
            response
        }
    })

    return requestsAndResponses
}

private fun Request.bodyContents() = ByteArrayOutputStream().let { outputStream ->
    bodyCallback?.invoke(request, outputStream, 0)

    String(outputStream.toByteArray(), UTF_8)
}
