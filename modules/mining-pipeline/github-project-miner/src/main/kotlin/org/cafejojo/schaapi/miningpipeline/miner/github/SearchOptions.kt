package org.cafejojo.schaapi.miningpipeline.miner.github

import mu.KLogging
import org.cafejojo.schaapi.miningpipeline.SearchOptions
import org.kohsuke.github.GitHub

/**
 * Represents options used to mine [GitHub].
 */
interface GitHubSearchOptions : SearchOptions {
    /**
     * Search content on GitHub with the given options and return a list of the full names of the found repositories.
     *
     * @return list of full names of found repositories on [GitHub] based on the passed options
     */
    fun searchContent(gitHub: GitHub): List<String>
}

/**
 * Maven search options, used to mine maven projects on [GitHub].
 *
 * @property groupId group id of library maven project should depend on
 * @property artifactId artifact id of library maven project should depend on
 * @property version version of library maven project should depend on
 * @property maxProjects maximum amount of project names it should return
 */
class MavenProjectSearchOptions(
    private val groupId: String,
    private val artifactId: String,
    private val version: String,
    private val maxProjects: Int
) : GitHubSearchOptions {
    private companion object : KLogging()

    override fun searchContent(gitHub: GitHub): List<String> {
        logger.info {
            "Mining a maximum of ${maxProjects}github maven projects which depend on: " +
                "group id: $groupId, artifact id: $artifactId, version: $version."
        }

        return gitHub.searchContent()
            .apply {
                q("dependency $groupId $artifactId $version")
                `in`("file")
                filename("pom")
                extension("xml")
            }
            .list()
            .also { logger.info { "Found ${it.totalCount} project." } }
            .also { if (maxProjects > it.totalCount) logger.info { "Will be capped at $maxProjects." } }
            .take(maxProjects)
            .map { it.owner.fullName }
            .also { logger.info { "Found ${it.size} projects names using the Github v3 Search API." } }
    }
}
