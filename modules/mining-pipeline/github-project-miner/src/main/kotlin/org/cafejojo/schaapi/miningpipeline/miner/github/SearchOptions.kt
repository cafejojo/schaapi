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
            "Mining a maximum of $maxProjects GitHub maven projects which depend on " +
                "group id: $groupId, artifact id: $artifactId, version: $version."
        }

        val searchResults = gitHub.searchContent()
            .apply {
                q("dependency $groupId $artifactId $version")
                `in`("file")
                filename("pom")
                extension("xml")
            }
            .list()

        logger.info { "Found ${searchResults.totalCount} projects." }
        if (maxProjects < searchResults.totalCount) logger.info { "Will be capped at $maxProjects." }

        val repositories = searchResults
            .also { logger.info { "Sorting owners by stargazers count." } }
            .sortedByDescending { it.owner.stargazersCount }
            .also { logger.debug { "Sorted owners by stargazers count." } }
            .take(maxProjects)
            .map { it.owner }

        val namesToStars = repositories.map { it.fullName to it.stargazersCount }
        val maxStargazers = namesToStars.maxBy { it.second }
        val minStargazers = namesToStars.minBy { it.second }
        val meanStargazers = namesToStars.sumBy { it.second }.toDouble().div(repositories.size)

        logger.info { "Max stargazer count: ${maxStargazers?.first}, repo: ${maxStargazers?.second}." }
        logger.info { "Min stargazer count: ${minStargazers?.first}, repo: ${minStargazers?.second}." }
        logger.info { "Mean stargazer count: $meanStargazers." }

        logger.info { "Found ${namesToStars.size} projects namesToStars using the Github v3 Search API." }
        return namesToStars.map { it.first }
    }
}
