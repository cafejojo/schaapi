package org.cafejojo.schaapi.common

import java.io.File

/**
 * A project.
 */
interface Project {
    /**
     * The directory containing the project.
     */
    val projectDir: File

    /**
     * Compiles the project.
     */
    fun compile()
}

/**
 * A Java project.
 */
interface JavaProject : Project {
    /**
     * The directory containing the project's compiled class files.
     */
    val classDir: File

    /**
     * The directory containing the project's dependencies as JARs.
     */
    val dependencyDir: File

    /**
     * The project's compiled class files.
     *
     * May be null before the project is [compile]d.
     */
    var classes: List<File>

    /**
     * The project's dependencies as JARs.
     *
     * May be null before the project is [compile]d.
     */
    var dependencies: List<File>

    /**
     * The classpath needed to load the complete project.
     *
     * May be null before the project is [compile]d.
     */
    var classpath: String

    /**
     * Returns true if the given class is part of this project.
     *
     * @param className the name of a class, including the package
     */
    fun containsClass(className: String): Boolean
}

/**
 * A Maven project.
 */
interface MavenProject : Project {
    /**
     * The Maven configuration file.
     */
    val pomFile: File
}

/**
 * Indicates that the compilation of a project was unsuccessful.
 */
class ProjectCompilationException(message: String? = null) : Exception(message)
