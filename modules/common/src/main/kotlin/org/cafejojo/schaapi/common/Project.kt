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
}
