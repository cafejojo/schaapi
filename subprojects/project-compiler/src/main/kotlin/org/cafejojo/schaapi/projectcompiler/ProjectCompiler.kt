package org.cafejojo.schaapi.projectcompiler

import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker
import java.io.File
import java.util.Collections

fun main(args: Array<String>) {
    if (args.size != 2) {
        throw IllegalArgumentException("Invalid number of arguments")
    }

    val projectDir = File(args[0])
    val mavenHome = File(args[1])

    val classes = ProjectCompiler(projectDir, mavenHome).compileProject()

    println("Found ${classes.size} classes")
    classes.forEach({ println(it.absolutePath) })
}

class ProjectCompiler(private val projectDir: File, private val mavenHome: File) {
    private val pomFile = projectDir.resolve("pom.xml")

    init {
        if (!projectDir.isDirectory) {
            throw IllegalArgumentException()
        }
        if (!mavenHome.isDirectory) {
            throw IllegalArgumentException()
        }
        if (!File(mavenHome, "bin/mvn").isFile) {
            throw IllegalArgumentException()
        }
        if (!pomFile.isFile) {
            throw IllegalArgumentException()
        }
    }

    fun compileProject(): List<File> {
        val request = DefaultInvocationRequest()
        request.pomFile = pomFile
        request.goals = Collections.singletonList("install")

        val invoker = DefaultInvoker()
        invoker.mavenHome = mavenHome
        val result = invoker.execute(request)
        if (result.exitCode != 0) {
            throw IllegalStateException()
        }

        val classDir = File(projectDir, "target/classes")
        if (!classDir.isDirectory) {
            throw IllegalStateException()
        }

        return classDir.walk().filter { it.isFile && it.extension == "class" }.toList()
    }
}
