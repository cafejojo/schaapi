package org.cafejojo.schaapi.githubtestreporter

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.jackson.mapper
import com.github.kittinunf.fuel.jackson.responseObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class CheckReporter(@Autowired private val appKeyGenerator: AppKeyGenerator) {
    init {
        mapper.propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
    }

    fun reportStarted(installationId: Int, owner: String, repository: String, headBranch: String, headSha: String) =
        GitHubApi.reportCheckStarted(
            owner,
            repository,
            headBranch,
            headSha,
            checkName = "breaking change detection",
            token = requestInstallationToken(installationId).token
        ).let { Fuel.request(it).responseObject<CheckRun>().getResultOrThrowException() }

    fun reportSuccess(
        installationId: Int,
        owner: String,
        repository: String,
        checkRunId: Int,
        checkMessage: CheckMessage? = null
    ) =
        GitHubApi.reportCheckCompleted(
            owner,
            repository,
            checkRunId,
            conclusion = "success",
            token = requestInstallationToken(installationId).token,
            messageTitle = checkMessage?.title ?: "",
            messageSummary = checkMessage?.summary ?: "",
            messageText = checkMessage?.text ?: ""
        ).let { Fuel.request(it).responseObject<CheckRun>().getResultOrThrowException() }

    fun reportFailure(
        installationId: Int,
        owner: String,
        repository: String,
        checkRunId: Int,
        checkMessage: CheckMessage? = null
    ) =
        GitHubApi.reportCheckCompleted(
            owner,
            repository,
            checkRunId,
            conclusion = "failure",
            token = requestInstallationToken(installationId).token,
            messageTitle = checkMessage?.title ?: "",
            messageSummary = checkMessage?.summary ?: "",
            messageText = checkMessage?.text ?: ""
        ).let { Fuel.request(it).responseObject<CheckRun>().getResultOrThrowException() }

    private fun requestInstallationToken(installationId: Int) =
        Fuel.request(GitHubApi.accessTokenFor(installationId, appKeyGenerator.create()))
            .responseObject<AccessToken>()
            .getResultOrThrowException()
}

data class CheckMessage(val title: String, val summary: String, val text: String)
