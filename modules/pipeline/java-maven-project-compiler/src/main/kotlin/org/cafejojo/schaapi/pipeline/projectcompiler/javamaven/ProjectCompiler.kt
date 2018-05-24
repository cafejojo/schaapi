package org.cafejojo.schaapi.pipeline.projectcompiler.javamaven

import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker
import org.cafejojo.schaapi.models.Project
import org.cafejojo.schaapi.models.project.java.JavaMavenProject
import org.cafejojo.schaapi.pipeline.ProjectCompiler
import java.io.File

/**
 * Compiles a Java project using Maven.
 */
class ProjectCompiler : ProjectCompiler {
    override fun compile(project: Project): Project {
        if (project !is JavaMavenProject) throw IllegalArgumentException("Project must be JavaMavenProject.")

        runMaven(project)

        project.classes = project.classDir.walk().filter { it.isFile && it.extension == "class" }.toList()
        project.classNames = project.classes.map {
            it.relativeTo(project.classDir).toString().dropLast(".class".length).replace(File.separatorChar, '.')
        }
        project.dependencies = project.dependencyDir.listFiles().orEmpty().toList()
        project.classpath =
            if (project.dependencies.isEmpty()) {
                project.classDir.absolutePath
            } else {
                project.classDir.absolutePath + File.pathSeparator +
                    project.dependencies.joinToString(File.pathSeparator) { it.absolutePath }
            }

        return project
    }

    private fun runMaven(project: JavaMavenProject) {
        val request = DefaultInvocationRequest().apply {
            baseDirectory = project.projectDir
            goals = listOf("clean", "install", "dependency:copy-dependencies")
            isBatchMode = true
            javaHome = File(System.getProperty("java.home"))
            pomFile = pomFile
        }

        val invoker = DefaultInvoker().apply {
            setOutputHandler(null)
            mavenHome = project.mavenDir
            workingDirectory = project.projectDir
        }

        val result = invoker.execute(request)
        if (result.exitCode != 0) {
            throw ProjectCompilationException("`maven install` executed unsuccessfully: " + result.executionException)
        }
    }
}

/**
 * Indicates that the compilation of a project was unsuccessful.
 */
class ProjectCompilationException(message: String? = null) : Exception(message)
