package org.cafejojo.schaapi.miningpipeline.testgenerator.jimpleevosuite

import org.cafejojo.schaapi.miningpipeline.Pattern
import org.cafejojo.schaapi.miningpipeline.TestGenerator
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimpleNode
import org.cafejojo.schaapi.models.project.JavaProject
import java.io.File
import java.io.PrintStream

private const val DEFAULT_PATTERN_CLASS_NAME = "Patterns"

/**
 * Represents the test generator that generates tests based on patterns.
 */
class TestGenerator(
    private val library: JavaProject,
    private val outputDirectory: File,
    private val timeout: Int,
    private val processStandardStream: PrintStream? = null,
    private val processErrorStream: PrintStream? = null
) : TestGenerator<JimpleNode> {
    override fun generate(patterns: List<Pattern<JimpleNode>>): File {
        val outputPatterns = outputDirectory.resolve("patterns/").apply { mkdirs() }
        val outputTests = outputDirectory.resolve("tests/").apply { mkdirs() }

        ClassGenerator(DEFAULT_PATTERN_CLASS_NAME).apply {
            patterns.forEachIndexed { index, pattern ->
                run {
                    val method = generateMethod("pattern$index", pattern)
                    optimizeMethod(method)
                }
            }
            writeToFile(outputPatterns.absolutePath)
        }

        EvoSuiteRunner(
            fullyQualifiedClassName = DEFAULT_PATTERN_CLASS_NAME,
            classpath = outputPatterns.absolutePath + File.pathSeparator + library.classpath,
            outputDirectory = outputTests.absolutePath,
            generationTimeoutSeconds = timeout,
            processStandardStream = processStandardStream,
            processErrorStream = processErrorStream
        ).run()

        return File(outputDirectory, "${DEFAULT_PATTERN_CLASS_NAME}_ESTest.java")
    }
}
