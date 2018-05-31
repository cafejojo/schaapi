package org.cafejojo.schaapi.miningpipeline.projectcompiler.javajar

import mu.KLogging
import org.cafejojo.schaapi.models.project.JavaJarProject
import org.cafejojo.schaapi.miningpipeline.ProjectCompiler
import java.io.FileInputStream
import java.util.jar.JarInputStream

/**
 * Finds all classes in a Java project consisting of a single JAR.
 */
class ProjectCompiler : ProjectCompiler<JavaJarProject> {
    private companion object : KLogging()

    override fun compile(project: JavaJarProject): JavaJarProject {
        val classNames = mutableListOf<String>()
        val jarInputStream = JarInputStream(FileInputStream(project.classDir))
        var jarEntry = jarInputStream.nextJarEntry

        if (jarEntry == null) {
            logger.warn("Jar project at ${project.projectDir.path} is empty.")
        }

        while (jarEntry != null) {
            if (jarEntry.name.endsWith(".class")) {
                classNames.add(jarEntry.name.dropLast(".class".length).replace('/', '.'))
            }

            jarEntry = jarInputStream.nextJarEntry
        }

        project.classNames = classNames

        return project
    }
}
