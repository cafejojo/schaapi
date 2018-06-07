package org.cafejojo.schaapi.miningpipeline.miner.github

import mu.KLogging
import org.cafejojo.schaapi.miningpipeline.SearchOptions
import org.kohsuke.github.GHContent
import org.kohsuke.github.GitHub
import org.kohsuke.github.PagedSearchIterable

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

    var sortByStargazersCount = false
    var sortByWatchersCount = false

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
            .apply { if (sortByStargazersCount) sortByStargazersCount(this) }
            .apply { if (sortByWatchersCount) sortByWatchersCount(this) }
            .take(maxProjects)
            .map { it.owner }

        logger.info { "Found ${repositories.size} projects namesToStars using the Github v3 Search API." }
        return repositories.map { it.fullName }
    }

    private fun sortByStargazersCount(githubRepositories: PagedSearchIterable<GHContent>)
        : PagedSearchIterable<GHContent> {
        githubRepositories
            .also { logger.info { "Sorting owners by stargazers count." } }
            .sortedByDescending { it.owner.stargazersCount }

        val max = githubRepositories.first().owner
        val min = githubRepositories.last().owner
        logger.info { "Maximum stargazers: ${max.stargazersCount}, repo: ${max.fullName}" }
        logger.info { "Minimum stargazers: ${min.stargazersCount}, repo: ${min.fullName}" }
        logger.info { "Average stargazers: ${githubRepositories.sumBy { it.owner.stargazersCount }}" }

        return githubRepositories
    }

    private fun sortByWatchersCount(githubRepositories: PagedSearchIterable<GHContent>)
        : PagedSearchIterable<GHContent> {
        githubRepositories
            .also { logger.info { "Sorting owners by watcher count count." } }
            .sortedByDescending { it.owner.watchers }

        val max = githubRepositories.first().owner
        val min = githubRepositories.last().owner
        logger.info { "Maximum watchers: ${max.watchers}, repo: ${max.watchers}" }
        logger.info { "Minimum watchers: ${min.watchers}, repo: ${min.watchers}" }
        logger.info { "Average watchers: ${githubRepositories.sumBy { it.owner.watchers }}" }

        return githubRepositories
    }
}
