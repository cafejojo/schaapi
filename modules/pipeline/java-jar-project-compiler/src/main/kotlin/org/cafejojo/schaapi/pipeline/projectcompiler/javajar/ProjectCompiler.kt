package org.cafejojo.schaapi.pipeline.projectcompiler.javajar

import org.cafejojo.schaapi.models.Project
import org.cafejojo.schaapi.models.project.java.JavaJarProject
import org.cafejojo.schaapi.pipeline.ProjectCompiler
import java.io.FileInputStream
import java.util.jar.JarInputStream

/**
 * Finds all classes in a Java project consisting of a single JAR.
 */
class ProjectCompiler : ProjectCompiler {
    override fun compile(project: Project): Project {
        if (project !is JavaJarProject) throw IllegalArgumentException("Project must be JavaJarProject.")

        val classNames = mutableListOf<String>()
        val jarInputStream = JarInputStream(FileInputStream(project.classDir))
        var jarEntry = jarInputStream.nextJarEntry

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
