package org.cafejojo.schaapi.miningpipeline.miner.github

import mu.KLogging
import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker
import org.cafejojo.schaapi.models.project.JavaMavenProject
import java.io.File

/**
 * Verifies (dynamically) if projects are using the correct library version.
 *
 * @param groupId group id of library maven project should depend on
 * @param artifactId artifact id of library maven project should depend on
 * @param version version of library maven project should depend on
 * @property displayOutput true iff output should be logged at INFO level
 */
class VersionVerifier(groupId: String, artifactId: String, version: String, private val displayOutput: Boolean = true) {
    private companion object : KLogging()

    private val query = "$groupId:$artifactId:jar:$version"

    /**
     * Verifies (dynamically) if the given [project] is using the correct library version.
     *
     * @param project a user project of the library
     */
    fun verify(project: JavaMavenProject) =
        if (createMavenInvoker(project).execute(createMavenInvocationRequest(project)).exitCode != 0) false
        else getDependencies(project).any { it.startsWith(query) }

    private fun createMavenInvocationRequest(project: JavaMavenProject) = DefaultInvocationRequest().apply {
        baseDirectory = project.projectDir
        goals = listOf("dependency:tree")
        isBatchMode = true
        javaHome = File(System.getProperty("java.home"))
        mavenOpts = """
            -DoutputFile=${getDependenciesListLocation(project)}
            -Dtokens=whitespace
        """.trimIndent()
    }

    private fun createMavenInvoker(project: JavaMavenProject) = DefaultInvoker().also {
        if (displayOutput) it.setOutputHandler(logger::info) else it.setOutputHandler(null)
        it.mavenHome = project.mavenDir
        it.workingDirectory = project.projectDir
    }

    private fun getDependenciesListLocation(project: JavaMavenProject) =
        File(project.projectDir, "_schaapi_project_dependencies.txt")

    private fun getDependencies(project: JavaMavenProject) =
        getDependenciesListLocation(project).readText().trim().split("\\s+".toRegex()).drop(1)
}
