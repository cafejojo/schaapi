package org.cafejojo.schaapi.miningpipeline.projectcompiler.javamaven

import mu.KLogging
import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker
import org.cafejojo.schaapi.miningpipeline.CompilationException
import org.cafejojo.schaapi.miningpipeline.ProjectCompiler
import org.cafejojo.schaapi.models.project.JavaMavenProject
import java.io.File

/**
 * Compiles a Java project using Maven.
 *
 * @property displayOutput true iff output should be logged at INFO level
 */
class JavaMavenProjectCompiler(private val displayOutput: Boolean = false) : ProjectCompiler<JavaMavenProject> {
    private companion object : KLogging()

    override fun compile(project: JavaMavenProject): JavaMavenProject {
        runMaven(project)
        analyzeCompiledProject(project)

        logger.debug { "`maven install` of ${project.projectDir} was successful." }
        return project
    }

    private fun runMaven(project: JavaMavenProject) {
        val request = DefaultInvocationRequest().apply {
            baseDirectory = project.projectDir
            goals = listOf("clean", "install")
            isBatchMode = true
            javaHome = File(System.getProperty("java.home"))
            mavenOpts = "-DskipTests=true"
            pomFile = project.pomFile
        }

        val invoker = DefaultInvoker().apply {
            setOutputHandler(logger::info)
            mavenHome = project.mavenDir
            workingDirectory = project.projectDir
        }

        val result = invoker.execute(request)
        if (result.exitCode != 0)
            throw ProjectCompilationException("`maven install` of ${project.projectDir} failed: " +
                (result.executionException?.message ?: "Cause unknown."))
    }

    private fun analyzeCompiledProject(project: JavaMavenProject) {
        project.classes = project.classDir.walk().filter { it.isFile && it.extension == "class" }.toSet()
        project.classNames = project.classes.map {
            it.relativeTo(project.classDir).toString().dropLast(".class".length).replace(File.separatorChar, '.')
        }.toSet()

        project.dependencies = emptySet()
        project.classpath = project.classDir.absolutePath

        if (project.classes.isEmpty()) {
            logger.warn { "Maven project at ${project.projectDir.path} does not contain any classes." }
        }
    }
}

/**
 * Indicates that the compilation of a project was unsuccessful.
 */
class ProjectCompilationException(message: String? = null) : CompilationException(message)
