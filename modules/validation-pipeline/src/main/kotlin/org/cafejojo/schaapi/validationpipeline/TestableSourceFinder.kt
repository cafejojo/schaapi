package org.cafejojo.schaapi.validationpipeline

import java.io.File

interface TestableSourceFinder {
    fun find(testFile: File, sourceFile: File): String?
}
