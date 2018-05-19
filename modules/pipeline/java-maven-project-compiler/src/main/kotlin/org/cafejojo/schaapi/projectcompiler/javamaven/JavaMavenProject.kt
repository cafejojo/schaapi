package org.cafejojo.schaapi.projectcompiler.javamaven

import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker
import org.cafejojo.schaapi.common.JavaProject
import org.cafejojo.schaapi.common.MavenProject
import org.cafejojo.schaapi.common.ProjectCompilationException
import java.io.File

/**
 * A Java project using Maven.
 */
@SuppressWarnings("LateinitUsage") // Refer to PR #23
class JavaMavenProject(
    override val projectDir: File,
    override val mavenDir: File = MavenInstaller.DEFAULT_MAVEN_HOME
) : JavaProject, MavenProject {
    override val pomFile = File(projectDir, "pom.xml")
    override val classDir = File(projectDir, "target/classes")
    override val dependencyDir = File(projectDir, "target/dependency")

    override lateinit var classes: List<File>
    override lateinit var classNames: List<String>
    override lateinit var dependencies: List<File>
    override lateinit var classpath: String

    init {
        if (!projectDir.isDirectory) {
            throw IllegalArgumentException("Given project directory does not exist")
        }
        if (!pomFile.isFile) {
            throw IllegalArgumentException("Given project directory is not a Maven project")
        }

        classDir.mkdirs()
        dependencyDir.mkdirs()
    }

    override fun compile() {
        runMaven()

        classes = classDir.walk().filter { it.isFile && it.extension == "class" }.toList()
        classNames = classes.map {
            it.relativeTo(classDir)
                .toString()
                .dropLast(".class".length)
                .replace(File.separatorChar, '.')
        }
        dependencies = dependencyDir.listFiles().orEmpty().toList()
        classpath =
            if (dependencies.isEmpty()) {
                classDir.absolutePath
            } else {
                classDir.absolutePath + File.pathSeparator +
                    dependencies.joinToString(File.pathSeparator) { it.absolutePath }
            }
    }

    private fun runMaven() {
        val request = DefaultInvocationRequest().apply {
            baseDirectory = projectDir
            goals = listOf("clean", "install", "dependency:copy-dependencies")
            isBatchMode = true
            javaHome = File(System.getProperty("java.home"))
            pomFile = pomFile
        }

        val invoker = DefaultInvoker().apply {
            setOutputHandler(null)
            mavenHome = mavenDir
            workingDirectory = projectDir
        }

        val result = invoker.execute(request)
        if (result.exitCode != 0) {
            throw ProjectCompilationException("`maven install` executed unsuccessfully: " + result.executionException)
        }
    }
}
