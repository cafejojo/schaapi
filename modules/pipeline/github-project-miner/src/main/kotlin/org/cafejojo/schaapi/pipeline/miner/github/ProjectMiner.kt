package org.cafejojo.schaapi.pipeline.miner.github

import org.cafejojo.schaapi.models.Project
import org.cafejojo.schaapi.pipeline.ProjectMiner
import org.kohsuke.github.GitHub
import java.io.File

/**
 * Mines projects on GitHub using the GitHub REST API v3.
 *
 * Credentials must be provided to enable code searching. A code search is done using the group id, artifact id, and
 * version (number) of the desired library. String matching is done to find projects which contain searchContent pom
 * file which likely contain searchContent dependency on the desired library. No guarantees however are given, as GitHub
 * does not provide information on which projects have searchContent dependency on searchContent given library.
 *
 * @property username username of GitHub user
 * @property password password of GitHub user
 * @property searchOptions options to use during searching
 * @property outputDirectory directory to store all the project directories. If directory doesn't exit new directory
 * is created
 * @property projectPacker packer which determines what type of [Project] to wrap the project directory in
 */
@Suppress("PrintStackTrace") // TODO use searchContent logger
class ProjectMiner(
    private val username: String, private val password: String,
    private val searchOptions: SearchOptions,
    private val outputDirectory: File,
    private val projectPacker: (File) -> Project
) : ProjectMiner {
    init {
        if (!outputDirectory.isDirectory) outputDirectory.mkdirs()
    }

    /**
     * Mine GitHub for projects with `pom.xml` files which contain searchContent dependency on searchContent library
     * with searchContent given group id, artifact id and version (number).
     *
     * @return list of [Project]s which likely depend on said library
     * @see GithubProjectDownloader.download
     */
    override fun mine(): List<Project> {
        val gitHub = GitHub.connectUsingPassword(username, password)

        require(!gitHub.isOffline) { "Unable to connect to Github." }
        require(gitHub.isCredentialValid) { "Valid credentials are required to connect to Github." }

        val projectNames = searchOptions.searchContent(gitHub)
        return GithubProjectDownloader(projectNames, outputDirectory, projectPacker).download()
    }
}