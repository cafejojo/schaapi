package org.cafejojo.schaapi.githubtestreporter

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.jackson.mapper
import com.github.kittinunf.fuel.jackson.responseObject

internal class CheckReporter {
    init {
        mapper.propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
    }

    fun reportStarted(installationId: Int, owner: String, repository: String, headBranch: String, headSha: String) =
        Fuel.request(GitHubApi.reportCheckStarted(
            owner,
            repository,
            headBranch,
            headSha,
            checkName = "breaking change detection",
            token = requestInstallationToken(installationId).token.also(::println)
        ))
            .responseObject<CheckRun>()
            .getResultOrThrowException()

    fun reportSuccess(
        installationId: Int,
        owner: String,
        repository: String,
        checkRunId: Int,
        checkMessage: CheckMessage? = null
    ) =
        Fuel.request(GitHubApi.reportCheckCompleted(
            owner,
            repository,
            checkRunId,
            conclusion = "success",
            token = requestInstallationToken(installationId).token,
            messageTitle = checkMessage?.title ?: "",
            messageSummary = checkMessage?.summary ?: "",
            messageText = checkMessage?.text ?: ""
        ))
            .responseObject<CheckRun>()
            .getResultOrThrowException()

    fun reportFailure(
        installationId: Int,
        owner: String,
        repository: String,
        checkRunId: Int,
        checkMessage: CheckMessage? = null
    ) =
        Fuel.request(GitHubApi.reportCheckCompleted(
            owner,
            repository,
            checkRunId,
            conclusion = "failure",
            token = requestInstallationToken(installationId).token,
            messageTitle = checkMessage?.title ?: "",
            messageSummary = checkMessage?.summary ?: "",
            messageText = checkMessage?.text ?: ""
        ))
            .responseObject<CheckRun>()
            .getResultOrThrowException()

    private fun requestInstallationToken(installationId: Int) =
        Fuel.request(GitHubApi.accessTokenFor(installationId, AppKeyGenerator.create()))
            .responseObject<AccessToken>()
            .getResultOrThrowException()
}

internal data class CheckMessage(val title: String, val summary: String, val text: String)
