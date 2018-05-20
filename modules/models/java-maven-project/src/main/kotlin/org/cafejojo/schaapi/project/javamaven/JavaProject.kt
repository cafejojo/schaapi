package org.cafejojo.schaapi.project.javamaven

import org.cafejojo.schaapi.common.Project
import java.io.File

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
     * The names of the project's compiled classes.
     *
     * May be null before the project is [compile]d.
     */
    var classNames: List<String>

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
}
