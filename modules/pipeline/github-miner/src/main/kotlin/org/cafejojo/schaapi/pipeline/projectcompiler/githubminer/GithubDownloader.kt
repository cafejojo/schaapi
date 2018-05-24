package org.cafejojo.schaapi.pipeline.projectcompiler.githubminer

import org.cafejojo.schaapi.models.Project

/**
 * Clones the github repositories and returns a list of java projects.
 *
 * @property repositoryNames the names of all repositories to be downloaded
 */
class GithubDownloader(val repositoryNames: Collection<String>) {
    /**
     * Start downloading repositories.
     */
    fun download(): List<Project> {
        println(repositoryNames.map { "https://github.com/$it\n" })
        return emptyList()
    }
}
