package org.cafejojo.schaapi.models

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
