package org.cafejojo.schaapi.testgenerator.jimpleevosuite

import org.cafejojo.schaapi.common.Pattern
import org.cafejojo.schaapi.common.TestGenerator
import org.cafejojo.schaapi.project.javamaven.JavaProject
import java.io.File
import java.io.OutputStream
import java.io.PrintStream

private const val DEFAULT_PATTERN_CLASS_NAME = "RegressionTest"

/**
 * Represents the test generator that generates tests based on patterns.
 */
class TestGenerator(
    private val library: JavaProject,
    private val outputDirectory: File,
    private val timeout: Int,
    private val processStandardStream: PrintStream? = null,
    private val processErrorStream: PrintStream? = null
) : TestGenerator {
    override fun generate(patterns: List<Pattern>): OutputStream {
        val outputPatterns = outputDirectory.resolve("patterns/").apply { mkdirs() }
        val outputTests = outputDirectory.resolve("tests/").apply { mkdirs() }

        ClassGenerator(DEFAULT_PATTERN_CLASS_NAME).apply {
            patterns.forEachIndexed { index, pattern ->
                generateMethod("pattern$index", pattern)
            }
            writeToFile(outputPatterns.absolutePath)
        }

        EvoSuiteTestGenerator(
            fullyQualifiedClassName = DEFAULT_PATTERN_CLASS_NAME,
            classpath = outputPatterns.absolutePath + File.pathSeparator + library.classpath,
            outputDirectory = outputTests.absolutePath,
            generationTimeoutSeconds = timeout,
            processStandardStream = processStandardStream,
            processErrorStream = processErrorStream
        ).run()

        return File(outputDirectory, "RegressionTest_ESTest.java").outputStream()
    }
}
