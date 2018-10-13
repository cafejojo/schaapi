package org.cafejojo.schaapi.miningpipeline.miner.github

import mu.KLogging
import org.cafejojo.schaapi.miningpipeline.ProjectMiner
import org.cafejojo.schaapi.models.Project
import org.cafejojo.schaapi.models.project.JavaMavenProject
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
 * @property token GitHub token
 * @property outputDirectory directory to store all the project directories. If directory doesn't exit new directory
 * is created
 * @property projectPacker packer which determines what type of [Project] to wrap the project directory in
 */
class GitHubProjectMiner<P : JavaMavenProject>(
    private var token: String,
    private val outputDirectory: File,
    private val projectPacker: (File) -> P
) : ProjectMiner<MavenProjectSearchOptions, P> {
    private companion object : KLogging()

    init {
        if (!outputDirectory.isDirectory) outputDirectory.mkdirs()
    }

    /**
     * Mine GitHub for projects with `pom.xml` files which contain searchContent dependency on searchContent library
     * with searchContent given group id, artifact id and version (number).
     *
     * @param searchOptions search options, which must be of type [MavenProjectSearchOptions]
     * @return list of [Project]s which likely depend on said library
     * @see GitHubProjectDownloader.download
     */
    override fun mine(searchOptions: MavenProjectSearchOptions): List<P> {
        val gitHub: GitHub = GitHub.connectUsingOAuth(token)
        logger.info { "Successfully authenticated using token." }

        val outProjects = outputDirectory.resolve("projects/").apply { mkdirs() }

        val projectNames = searchOptions.searchContent(gitHub)

        val versionVerifier = VersionVerifier(
            searchOptions.groupId, searchOptions.artifactId, searchOptions.version, false
        )

        return GitHubProjectDownloader(projectNames, outProjects, projectPacker)
            .download()
            .filter(versionVerifier::verify)
    }
}
