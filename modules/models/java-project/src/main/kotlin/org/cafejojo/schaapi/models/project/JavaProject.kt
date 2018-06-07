package org.cafejojo.schaapi.models.project

import org.cafejojo.schaapi.models.Project
import java.io.File

/**
 * A Java project.
 */
interface JavaProject : Project {
    /**
     * The directories containing the project's compiled class files.
     */
    val classDirs: List<File>

    /**
     * The directory containing the project's dependencies as JARs.
     */
    val dependencyDir: File

    /**
     * The project's compiled class files.
     *
     * May be null before the project is compiled.
     */
    var classes: List<File>

    /**
     * The names of the project's compiled classes.
     *
     * May be null before the project is compiled.
     */
    var classNames: List<String>

    /**
     * The project's dependencies as JARs.
     *
     * May be null before the project is compiled.
     */
    var dependencies: List<File>

    /**
     * The classpath needed to load the complete project.
     *
     * May be null before the project is compiled.
     */
    var classpath: String
}
