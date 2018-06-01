package org.cafejojo.schaapi.githubtestreporter

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.util.FuelRouting
import com.github.kittinunf.result.Result
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Suppress("ClassNaming") // We follow Fuel's conventions
internal sealed class GitHubApi : FuelRouting {
    override val basePath = "https://api.github.com"

    init {
        FuelManager.instance.client = CustomHttpClient()
    }

    internal class accessTokenFor(installationId: Int, appToken: String) : GitHubApi() {
        override val method = Method.POST
        override val path = "/installations/$installationId/access_tokens"
        override val headers = mapOf(
            "Authorization" to "Bearer $appToken",
            "Accept" to "application/vnd.github.machine-man-preview+json"
        )
        override val params: List<Pair<String, Any?>> = emptyList()
        override val body = null
    }

    internal class reportCheckStarted(
        owner: String,
        repository: String,
        headBranch: String,
        headSha: String,
        checkName: String,
        token: String
    ) : GitHubApi() {
        override val method = Method.POST
        override val path = "/repos/$owner/$repository/check-runs"
        override val headers = mapOf(
            "Authorization" to "Token $token",
            "Accept" to "application/vnd.github.antiope-preview+json"
        )
        override val params: List<Pair<String, Any?>> = emptyList()
        override val body = JSON.Object(
            "name" to JSON.String(checkName),
            "head_branch" to JSON.String(headBranch),
            "head_sha" to JSON.String(headSha),
            "started_at" to JSON.String(now()),
            "status" to JSON.String("in_progress")
        ).toJsonString()
    }

    internal class reportCheckCompleted(
        owner: String,
        repository: String,
        checkRunId: Int,
        conclusion: String,
        token: String,
        messageTitle: String = "",
        messageSummary: String = "",
        messageText: String = ""
    ) : GitHubApi() {
        override val method = Method.PATCH
        override val path = "/repos/$owner/$repository/check-runs/$checkRunId"
        override val headers = mapOf(
            "Authorization" to "Token $token",
            "Accept" to "application/vnd.github.antiope-preview+json",
            "Content-Type" to "application/json"
        )
        override val params: List<Pair<String, Any?>> = emptyList()
        override val body = JSON.Object(
            "completed_at" to JSON.String(now()),
            "status" to JSON.String("completed"),
            "conclusion" to JSON.String(conclusion),
            "output" to JSON.Object(
                "title" to JSON.String(messageTitle),
                "summary" to JSON.String(messageSummary),
                "text" to JSON.String(messageText)
            )
        ).toJsonString()
    }
}

internal data class AccessToken(
    val token: String,
    val expiresAt: String
)

internal data class CheckRun(
    val id: Int
)

internal fun <T : Any> Triple<Request, Response, Result<T, FuelError>>.getResultOrThrowException() =
    third.let { result ->
        when (result) {
            is Result.Success -> result.get()
            is Result.Failure -> throw result.getException()
        }
    }

private fun now() = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

private sealed class JSON {
    @JsonSerialize(using = JSONStringSerializer::class)
    internal class String(val string: kotlin.String) : JSON()

    private class JSONStringSerializer(t: Class<JSON.String>? = null) : StdSerializer<JSON.String>(t) {
        override fun serialize(value: JSON.String, jgen: JsonGenerator, provider: SerializerProvider) {
            jgen.writeString(value.string)
        }
    }

    @JsonSerialize(using = JSONObjectSerializer::class)
    internal class Object(vararg pairs: Pair<kotlin.String, JSON>) : JSON() {
        val map = pairs.toMap()
    }

    private class JSONObjectSerializer(t: Class<JSON.Object>? = null) : StdSerializer<JSON.Object>(t) {
        override fun serialize(value: JSON.Object, jgen: JsonGenerator, provider: SerializerProvider) {
            jgen.writeObject(value.map)
        }
    }

    fun toJsonString() = ObjectMapper().writeValueAsString(this)
}
