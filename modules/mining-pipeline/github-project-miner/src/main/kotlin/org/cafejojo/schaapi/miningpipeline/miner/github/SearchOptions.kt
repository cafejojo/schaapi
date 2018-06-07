package org.cafejojo.schaapi.miningpipeline.miner.github

import mu.KLogging
import org.cafejojo.schaapi.miningpipeline.SearchOptions
import org.kohsuke.github.GHContent
import org.kohsuke.github.GHContentSearchBuilder
import org.kohsuke.github.GitHub
import org.kohsuke.github.PagedSearchIterable

/**
 * Represents options used to mine [GitHub].
 */
abstract class GitHubSearchOptions(private val maxProjects: Int) : SearchOptions {
    private companion object : KLogging()

    var sortByStargazers = false
    var sortByWatchers = false

    /**
     * Search content on GitHub with the given options and return a list of the full names of the found repositories.
     *
     * @return list of full names of found repositories on [GitHub] based on the passed options
     */
    fun searchContent(gitHub: GitHub): List<String> {
        val searchResults = buildGitHubSearchContent(gitHub).list()

        logger.info { "Found ${searchResults.totalCount} projects." }
        if (maxProjects < searchResults.totalCount) logger.info { "Will be capped at $maxProjects." }

        val repositories = searchResults
            .apply { if (sortByStargazers) sortByStargazers(this) }
            .apply { if (sortByWatchers) sortByWatchers(this) }
            .take(maxProjects)
            .map { it.owner }

        logger.info { "Found ${repositories.size} projects namesToStars using the Github v3 Search API." }
        return repositories.map { it.fullName }
    }

    protected abstract fun buildGitHubSearchContent(gitHub: GitHub): GHContentSearchBuilder

    private fun sortByStargazers(githubRepositories: PagedSearchIterable<GHContent>): PagedSearchIterable<GHContent> {
        githubRepositories
            .also { logger.info { "Sorting owners by stargazers count." } }
            .sortedByDescending { it.owner.stargazersCount }

        val max = githubRepositories.first().owner
        val min = githubRepositories.last().owner
        logger.info { "Maximum stargazers: ${max.stargazersCount}, repository: ${max.fullName}." }
        logger.info { "Minimum stargazers: ${min.stargazersCount}, repository: ${min.fullName}." }
        logger.info { "Average stargazers: ${githubRepositories.sumBy { it.owner.stargazersCount }}." }

        return githubRepositories
    }

    private fun sortByWatchers(githubRepositories: PagedSearchIterable<GHContent>): PagedSearchIterable<GHContent> {
        githubRepositories
            .also { logger.info { "Sorting owners by watcher count count." } }
            .sortedByDescending { it.owner.watchers }

        val max = githubRepositories.first().owner
        val min = githubRepositories.last().owner
        logger.info { "Maximum watchers: ${max.watchers}, repository: ${max.watchers}." }
        logger.info { "Minimum watchers: ${min.watchers}, repository: ${min.watchers}." }
        logger.info { "Average watchers: ${githubRepositories.sumBy { it.owner.watchers }}." }

        return githubRepositories
    }
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
) : GitHubSearchOptions(maxProjects) {
    private companion object : KLogging()

    override fun buildGitHubSearchContent(gitHub: GitHub): GHContentSearchBuilder {
        logger.info { "Mining a maximum of $maxProjects GitHub maven projects." }
        logger.info { "Should depend on group id: $groupId, artifact id: $artifactId, version: $version." }

        return gitHub.searchContent().apply {
            q("dependency $groupId $artifactId $version")
            `in`("file")
            filename("pom")
            extension("xml")
        }
    }
}
