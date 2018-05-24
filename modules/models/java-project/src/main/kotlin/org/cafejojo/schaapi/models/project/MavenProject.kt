package org.cafejojo.schaapi.models.project

import org.cafejojo.schaapi.models.Project
import java.io.File

/**
 * A Maven project.
 */
interface MavenProject : Project {
    /**
     * The directory where Maven is installed.
     */
    val mavenDir: File

    /**
     * The Maven configuration file.
     */
    val pomFile: File
}
