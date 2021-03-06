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

    private fun <T> Iterable<T>.averageOf(selector: (T) -> Int): Double {
        val sum = this.map { selector(it) }.sum()
        return if (this.count() > 0) sum.toDouble().div(this.count()) else 0.0
    }

    /**
     * Search content on GitHub with the given options and return a list of the full names of the found repositories.
     *
     * @return list of full names of found repositories on [GitHub] based on the passed options
     */
    fun searchContent(gitHub: GitHub): List<Pair<String, String>> {
        val searchResults = buildGitHubSearchContent(gitHub).list()

        logger.info { "Found ${searchResults.totalCount} projects." }
        if (maxProjects < searchResults.totalCount) logger.info { "Will be capped at $maxProjects." }

        val names = searchResults
            .apply { if (sortByStargazers) sortByStargazers(this) }
            .apply { if (sortByWatchers) sortByWatchers(this) }
            .take(maxProjects)
            .mapNotNull {
                if (it.owner.branches.contains("master")) it.owner.fullName to "master"
                else null
            }

        logger.info { "Found ${names.size} project names using the GitHub v3 Search API." }
        return names
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
        logger.info { "Average stargazers: ${githubRepositories.averageOf { it.owner.stargazersCount }}." }

        return githubRepositories
    }

    private fun sortByWatchers(githubRepositories: PagedSearchIterable<GHContent>): PagedSearchIterable<GHContent> {
        githubRepositories
            .also { logger.info { "Sorting owners by watcher count." } }
            .sortedByDescending { it.owner.watchers }

        val max = githubRepositories.first().owner
        val min = githubRepositories.last().owner
        logger.info { "Maximum watchers: ${max.watchers}, repository: ${max.watchers}." }
        logger.info { "Minimum watchers: ${min.watchers}, repository: ${min.watchers}." }
        logger.info { "Average watchers: ${githubRepositories.averageOf { it.owner.watchers }}." }

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
    val groupId: String,
    val artifactId: String,
    val version: String,
    private val maxProjects: Int
) : GitHubSearchOptions(maxProjects) {
    private companion object : KLogging()

    override fun buildGitHubSearchContent(gitHub: GitHub): GHContentSearchBuilder {
        logger.info { "Mining a maximum of $maxProjects GitHub Maven projects." }
        logger.info { "Should depend on group id: $groupId, artifact id: $artifactId, version: $version." }

        return gitHub.searchContent().apply {
            q("dependency $groupId $artifactId $version")
            `in`("file")
            filename("pom")
            extension("xml")
            path("/")
        }
    }
}
