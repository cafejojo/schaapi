package org.cafejojo.schaapi.validationpipeline.githubinteraction.githubapi

import com.github.kittinunf.fuel.core.FuelError
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
        repositoryFullName: String,
        headBranch: String,
        headSha: String,
        checkName: String,
        token: String
    ) : GitHubApi() {
        override val method = Method.POST
        override val path = "/repos/$repositoryFullName/check-runs"
        override val headers = mapOf(
            "Authorization" to "Token $token",
            "Accept" to "application/vnd.github.antiope-preview+json"
        )
        override val params: List<Pair<String, Any?>> = emptyList()
        override val body = json {
            "name" to checkName
            "head_branch" to headBranch
            "head_sha" to headSha
            "started_at" to now()
            "status" to "in_progress"
        }.toString()
    }

    internal class reportCheckCompleted(
        repositoryFullName: String,
        checkRunId: Int,
        conclusion: String,
        token: String,
        messageTitle: String = "",
        messageSummary: String = "",
        messageText: String = ""
    ) : GitHubApi() {
        override val method = Method.PATCH
        override val path = "/repos/$repositoryFullName/check-runs/$checkRunId"
        override val headers = mapOf(
            "Authorization" to "Token $token",
            "Accept" to "application/vnd.github.antiope-preview+json",
            "Content-Type" to "application/json"
        )
        override val params: List<Pair<String, Any?>> = emptyList()

        override val body = json {
            "completed_at" to now()
            "status" to "completed"
            "conclusion" to conclusion
            "output" to json {
                "title" to messageTitle
                "summary" to messageSummary
                "text" to messageText
            }
        }.toString()
    }
}

internal fun <T : Any> Triple<Request, Response, Result<T, FuelError>>.getResultOrThrowException() =
    third.let { result ->
        when (result) {
            is Result.Success -> result.get()
            is Result.Failure -> throw result.getException()
        }
    }

private fun now() = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
