package org.cafejojo.schaapi.project.javamaven

import org.cafejojo.schaapi.common.Project
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
