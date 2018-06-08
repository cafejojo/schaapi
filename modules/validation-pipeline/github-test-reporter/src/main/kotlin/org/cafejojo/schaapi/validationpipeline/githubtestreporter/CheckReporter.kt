package org.cafejojo.schaapi.validationpipeline.githubtestreporter

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.jackson.mapper
import com.github.kittinunf.fuel.jackson.responseObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Reports information about checks to GitHub.
 */
@Service
class CheckReporter(@Autowired private val appKeyGenerator: AppKeyGenerator) {
    init {
        mapper.propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
    }

    /**
     * Reports a 'started' check to GitHub.
     *
     * When successful, a CheckRun object containing the ID needed for future check requests is returned.
     */
    fun reportStarted(installationId: Int, repositoryFullName: String, headBranch: String, headSha: String) =
        GitHubApi.reportCheckStarted(
            repositoryFullName,
            headBranch,
            headSha,
            checkName = "breaking change detection",
            token = requestInstallationToken(installationId).token
        ).let { Fuel.request(it).responseObject<CheckRun>().getResultOrThrowException() }

    /**
     * Reports a 'finished successful' check to GitHub.
     */
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

    /**
     * Reports a 'finished failing' check to GitHub.
     */
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

    internal fun requestInstallationToken(installationId: Int) =
        Fuel.request(GitHubApi.accessTokenFor(installationId, appKeyGenerator.create()))
            .responseObject<InstallationToken>()
            .getResultOrThrowException()
}

/**
 * A message to be displayed on the checks page containing the results of a check run.
 */
data class CheckMessage(val title: String, val summary: String, val text: String)

/**
 * A check run.
 */
data class CheckRun(val id: Int)

/**
 * An access token for a specific installation.
 */
internal data class InstallationToken(val token: String, val expiresAt: String)
