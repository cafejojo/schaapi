package org.cafejojo.schaapi.validationpipeline

import java.io.File

/**
 * Responsible for finding the source of the pattern present in the file under test.
 */
interface TestableSourceFinder {
    /**
     * Finds the source of the pattern present in [sourceFile] under test in the given [testFile].
     */
    fun find(testFile: File, sourceFile: File): String?
}
