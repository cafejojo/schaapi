package org.cafejojo.schaapi.validationpipeline.testablesourcefinder

import mu.KLogging
import org.cafejojo.schaapi.validationpipeline.TestableSourceFinder
import org.springframework.stereotype.Component
import java.io.File
import java.io.IOException
import java.nio.file.Files

/**
 * Responsible for the finding the source of the pattern present in the file under test.
 */
@Component
class SingleTestPerFileTestableSourceFinder : TestableSourceFinder {
    private companion object : KLogging()

    override fun find(testFile: File, sourceFile: File): String? {
        try {
            val patternName = PatternNameFinder(testFile.readText()).find().firstOrNull() ?: return null

            val temporaryDirectory = Files.createTempDirectory("schaapi-decompiled-sources").toFile()

            val decompiledSource = Decompiler.decompile(sourceFile, temporaryDirectory) ?: return null

            val patternSource = MethodSourceRetriever(decompiledSource).getSourceOf(patternName)

            temporaryDirectory.deleteRecursively()

            return patternSource
        } catch (exception: IOException) {
            logger.warn("File(s) could not be loaded.", exception)
            return null
        }
    }
}
