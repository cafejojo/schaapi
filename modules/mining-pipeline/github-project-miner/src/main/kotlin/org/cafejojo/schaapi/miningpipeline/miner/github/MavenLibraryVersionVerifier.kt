package org.cafejojo.schaapi.miningpipeline.miner.github

import mu.KLogging
import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker
import org.cafejojo.schaapi.miningpipeline.TimedCallable
import org.cafejojo.schaapi.models.project.MavenProject
import java.io.File

/**
 * Verifies (dynamically) if projects are using the correct library version.
 *
 * @param groupId group id of the library that the maven project should depend on
 * @param artifactId artifact id of the library that the maven project should depend on
 * @param version version of the library that the maven project should depend on
 * @property timeout the time after which version verification should be interrupted, or 0 if there should be no limit
 * @property displayOutput true iff output should be logged at INFO level
 */
class MavenLibraryVersionVerifier(
    groupId: String,
    artifactId: String,
    version: String,
    private val displayOutput: Boolean = true,
    private val timeout: Long = 0L
) {
    private companion object : KLogging()

    private val query = "$groupId:$artifactId:jar:$version"

    /**
     * Verifies (dynamically) if the given [project] is using the correct library version.
     *
     * @param project a user project of the library
     */
    fun verify(project: MavenProject): Boolean {
        val result = TimedCallable(timeout) {
            createMavenInvoker(project).execute(createMavenInvocationRequest(project))
        }.call()

        return if (result?.exitCode != 0) false
        else getDependencies(project).any { it.startsWith(query) }
    }

    private fun createMavenInvocationRequest(project: MavenProject) =
        DefaultInvocationRequest().apply {
            baseDirectory = project.projectDir
            goals = listOf("dependency:tree")
            isBatchMode = true
            javaHome = File(System.getProperty("java.home"))
            mavenOpts = """
                -DoutputFile=${getDependenciesListLocation(project).absolutePath}
                -Dtokens=whitespace
            """.trimIndent().replace("\n", " ")
        }

    private fun createMavenInvoker(project: MavenProject) =
        DefaultInvoker().also {
            if (displayOutput) it.setOutputHandler(logger::info) else it.setOutputHandler(null)
            it.mavenHome = project.mavenDir
            it.workingDirectory = project.projectDir
        }

    private fun getDependenciesListLocation(project: MavenProject) =
        File(project.projectDir, "_schaapi_project_dependencies.txt")

    private fun getDependencies(project: MavenProject) =
        getDependenciesListLocation(project).readText().trim().lines().drop(1).map { it.trim() }
}
