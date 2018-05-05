package org.cafejojo.schaapi.projectcompiler

import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker
import java.io.File

/**
 * Takes the path to a Maven project, compiles it with Maven, and prints the paths to the class files that were created.
 *
 * @param args the path to a Maven project
 */
fun main(args: Array<String>) {
    if (args.size != 1) {
        print("Invalid number of arguments")
        return
    }

    MavenInstaller().installMaven(MavenInstaller.DEFAULT_MAVEN_HOME)

    val projectDir = File(args[0])
    val project = ProjectCompiler().compileProject(projectDir)

    println(project)
}

/**
 * Compiles projects with Maven.
 */
class ProjectCompiler {
    /**
     * Runs Maven in [projectDir] and returns the [Project] that is created.
     *
     * @param projectDir the directory of the project to compile
     * @return the [Project] that is created
     */
    fun compileProject(projectDir: File): Project {
        val project = Project(projectDir)

        runMaven(project)

        return project
    }

    /**
     * Using Maven, cleans the project, compiles the source, and downloads the dependencies as JARs.
     * @param project the directory to run Maven in
     */
    private fun runMaven(project: Project) {
        val request = DefaultInvocationRequest().apply {
            pomFile = project.pomFile
            goals = listOf("clean", "install", "dependency:copy-dependencies")
        }

        val invoker = DefaultInvoker().apply {
            setOutputHandler(null)
            mavenHome = MavenInstaller.DEFAULT_MAVEN_HOME
        }

        val result = invoker.execute(request)
        if (result.exitCode != 0) {
            throw ProjectCompilationException("`maven install` executed unsuccessfully")
        }
    }
}

/**
 * Indicates that the compilation of a project was unsuccessful.
 */
class ProjectCompilationException(message: String? = null) : Exception(message)
