package org.cafejojo.schaapi.pipeline.projectcompiler.javajar

import org.cafejojo.schaapi.models.Project
import org.cafejojo.schaapi.models.project.java.JavaJarProject
import org.cafejojo.schaapi.pipeline.ProjectCompiler
import java.io.FileInputStream
import java.util.jar.JarInputStream

/**
 * Compiles a Java project consisting of a single JAR.
 */
class ProjectCompiler : ProjectCompiler {
    /**
     * Compiles a project.
     *
     * @param project an uncompiled project
     * @return a compiled project
     */
    override fun compile(project: Project): Project {
        if (project !is JavaJarProject) throw IllegalArgumentException("Project must be JavaJarProject.")

        val classNames = mutableListOf<String>()
        val jarInputStream = JarInputStream(FileInputStream(project.classDir))
        while (true) {
            val jarEntry = jarInputStream.nextJarEntry ?: break

            if (jarEntry.name.endsWith(".class")) {
                classNames.add(jarEntry.name.dropLast(".class".length).replace('/', '.'))
            }
        }

        project.classNames = classNames

        return project
    }
}
