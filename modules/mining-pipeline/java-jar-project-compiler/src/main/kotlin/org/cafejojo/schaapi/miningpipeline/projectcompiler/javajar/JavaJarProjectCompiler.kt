package org.cafejojo.schaapi.miningpipeline.projectcompiler.javajar

import mu.KLogging
import org.cafejojo.schaapi.miningpipeline.ProjectCompiler
import org.cafejojo.schaapi.models.project.JavaJarProject
import java.io.FileInputStream
import java.util.jar.JarInputStream

/**
 * Finds all classes in a Java project consisting of a single JAR.
 */
class JavaJarProjectCompiler : ProjectCompiler<JavaJarProject> {
    private companion object : KLogging()

    override fun compile(project: JavaJarProject) =
        with(project) {
            classNames = findClasses(JarInputStream(FileInputStream(project.classDir)))
            if (classNames.isEmpty()) logger.warn { "Jar project at ${projectDir.path} is empty." }
            project
        }

    private fun findClasses(jarInputStream: JarInputStream): Set<String> {
        val classNames = mutableSetOf<String>()
        var jarEntry = jarInputStream.nextJarEntry

        while (jarEntry != null) {
            if (jarEntry.name.endsWith(".class") && !jarEntry.name.startsWith("META-INF")) {
                classNames.add(jarEntry.name.dropLast(".class".length).replace('/', '.'))
            }

            jarEntry = jarInputStream.nextJarEntry
        }

        return classNames
    }
}
