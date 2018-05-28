package org.cafejojo.schaapi.pipeline.miner.github

import org.cafejojo.schaapi.models.Project
import org.cafejojo.schaapi.models.project.java.JavaMavenProject
import org.cafejojo.schaapi.pipeline.ProjectMiner
import org.kohsuke.github.GitHub
import java.io.File

/**
 * Mines projects on GitHub using the GitHub REST API v3.
 *
 * Credentials must be provided to enable code searching. A code search is done using the group id, artifact id, and
 * version (number) of the desired library. String matching is done to find projects which contain a pom file which
 * likely contain a dependency on the desired library. No guarantees however are given, as GitHub does not provide
 * information on which projects have a dependency on a given library.
 *
 * @property username username of GitHub user
 * @property password password of GitHub user
 * @property outputDirectory directory to store all the project directories. If directory doesn't exit new directory
 * is created
 * @property projectPacker packer which determines what type of [Project] to wrap the project directory in
 */
@Suppress("PrintStackTrace") // TODO use a logger
class ProjectMiner(
    private val username: String, private val password: String,
    private val outputDirectory: File,
    private val projectPacker: (File) -> Project
) : ProjectMiner {
    init {
        if (!outputDirectory.isDirectory) outputDirectory.mkdirs()
    }

    /**
     * Mine GitHub for projects with `pom.xml` files which contain a dependency on a library with a given group id,
     * artifact id and version (number).
     *
     * @param groupId the group id of library
     * @param artifactId the artifact id of the library
     * @param version the version (number) of the library
     * @return list of [Project]s which likely depend on said library
     * @see GithubProjectDownloader.download
     */
    override fun mine(groupId: String, artifactId: String, version: String): List<Project> {
        val github = GitHub.connectUsingPassword(username, password)

        require(!github.isOffline) { "Unable to connect to Github." }
        require(github.isCredentialValid) { "Valid credentials are required to connect to Github." }

        val projectNames = github.searchContent()
            .apply {
                q("dependency $groupId $artifactId $version")
                `in`("file")
                filename("pom")
                extension("xml")
            }
            .list()
            .map { it.owner.fullName }

        return GithubProjectDownloader(projectNames, outputDirectory, projectPacker).download()
    }
}

fun main(args: Array<String>) {
    ProjectMiner(
        args[0],
        args[1],
        File(""),
        { file -> JavaMavenProject(file) })
        .mine("com.google.guava", "guava", "25.1-jre")
}
