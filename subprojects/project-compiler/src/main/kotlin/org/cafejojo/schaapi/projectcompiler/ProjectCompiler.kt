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
    val classes = ProjectCompiler(projectDir).compileProject()

    println("Found ${classes.size} classes")
    classes.forEach { println(it.absolutePath) }
}

/**
 * Compiles a project.
 */
class ProjectCompiler(private val projectDir: File) {
    private val pomFile: File

    init {
        if (!projectDir.isDirectory) {
            throw IllegalArgumentException("Given project directory does not exist")
        }

        pomFile = projectDir.resolve("pom.xml")
        if (!pomFile.isFile) {
            throw IllegalArgumentException("Given project directory is not a Maven project")
        }
    }

    /**
     * Runs `maven install` and finds the class files that were created.
     *
     * @return the class files that were created
     */
    fun compileProject(): List<File> {
        runMavenInstall()
        return findClassFiles()
    }

    private fun runMavenInstall() {
        val request = DefaultInvocationRequest().also {
            it.pomFile = pomFile
            it.goals = listOf("clean", "install")
        }

        val invoker = DefaultInvoker().also {
            it.setOutputHandler(null)
            it.mavenHome = MavenInstaller.DEFAULT_MAVEN_HOME
        }

        val result = invoker.execute(request)
        if (result.exitCode != 0) {
            throw ProjectCompilationException("`maven install` executed unsuccessfully")
        }
    }

    private fun findClassFiles(): List<File> {
        val classDir = File(projectDir, "target/classes")
        if (!classDir.isDirectory) {
            throw ProjectCompilationException("Could not find `target/classes` directory after running `maven install`")
        }

        return classDir.walk().filter { it.isFile && it.extension == "class" }.toList()
    }
}

/**
 * Indicates that the compilation of a project was unsuccessful.
 */
class ProjectCompilationException(message: String? = null) : Exception(message)
