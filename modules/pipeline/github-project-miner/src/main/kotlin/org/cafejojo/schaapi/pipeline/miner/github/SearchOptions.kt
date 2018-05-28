package org.cafejojo.schaapi.pipeline.miner.github

import org.kohsuke.github.GitHub

/**
 * Represents options used to mine [GitHub].
 */
abstract class SearchOptions {
    /**
     * Search content on github with the given options and return a list of the full names of the found repositories.
     * @return list of full names of found repositories on [GitHub] based on the passed options
     */
    abstract fun searchContent(gitHub: GitHub): List<String>
}

/**
 * Maven search options, used to mine maven projects on [GitHub].
 *
 * @property groupId group id of library maven project should depend on
 * @property artifactId artifact id of library maven project should depend on
 * @property version version of library maven project should depend on
 */
class MavenProjectSeachOptions(
    private val groupId: String,
    private val artifactId: String,
    private val version: String
) : SearchOptions() {
    override fun searchContent(gitHub: GitHub): List<String> =
        gitHub.searchContent()
            .apply {
                q("dependency $groupId $artifactId $version")
                `in`("file")
                filename("pom")
                extension("xml")
            }
            .list()
            .map { it.owner.fullName }
}
