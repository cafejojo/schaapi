package org.cafejojo.schaapi.models.project

import org.cafejojo.schaapi.models.Project
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
     * May be empty before the project is compiled.
     */
    var classes: Set<File>

    /**
     * The names of the project's compiled classes.
     *
     * May be empty before the project is compiled.
     */
    var classNames: Set<String>

    /**
     * The project's dependencies as JARs.
     *
     * May be empty before the project is compiled.
     */
    var dependencies: Set<File>

    /**
     * The classpath needed to load the complete project.
     *
     * May be empty before the project is compiled.
     */
    var classpath: String
}
