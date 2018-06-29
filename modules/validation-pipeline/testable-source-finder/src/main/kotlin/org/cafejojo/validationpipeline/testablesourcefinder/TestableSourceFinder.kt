package org.cafejojo.validationpipeline.testablesourcefinder

import mu.KLogging
import java.io.File
import java.io.IOException
import java.nio.file.Files

/**
 * Responsible for the finding the source of the pattern present in [sourceFile] under test in the given [testFile].
 */
class TestableSourceFinder(private val testFile: File, private val sourceFile: File) {
    private companion object : KLogging()

    /**
     * Finds the source of the pattern present in [sourceFile] under test in the given [testFile].
     */
    fun find(): String? {
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
