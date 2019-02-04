package org.cafejojo.schaapi.miningpipeline.testgenerator.jimpleevosuite

import mu.KLogging
import org.cafejojo.schaapi.miningpipeline.Pattern
import org.cafejojo.schaapi.miningpipeline.TestGenerator
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimpleNode
import org.cafejojo.schaapi.models.project.JavaProject
import java.io.File
import java.io.PrintStream

private const val DEFAULT_PATTERN_CLASS_PACKAGE = "org.cafejojo.schaapi.patterns"
private const val DEFAULT_PATTERN_CLASS_NAME_PREFIX = "Patterns"

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
    private companion object : KLogging()

    override fun generate(patterns: List<Pattern<JimpleNode>>) {
        val outputPatterns = outputDirectory.resolve("patterns/").apply { mkdirs() }
        val outputTests = outputDirectory.resolve("tests/").apply { mkdirs() }

        if (patterns.isEmpty()) {
            logger.warn { "No patterns were found in the user programs." }

            ClassGenerator("$DEFAULT_PATTERN_CLASS_PACKAGE.${DEFAULT_PATTERN_CLASS_NAME_PREFIX}_NONE").apply {
                writeToFile(outputPatterns.absolutePath)
            }
        } else {
            patterns.forEachIndexed { index, pattern ->
                ClassGenerator("$DEFAULT_PATTERN_CLASS_PACKAGE.${DEFAULT_PATTERN_CLASS_NAME_PREFIX}_$index").apply {
                    val method = generateMethod("pattern$index", pattern)
                    optimizeMethod(method)
                    writeToFile(outputPatterns.absolutePath)
                }
            }
        }

        EvoSuiteRunner(
            fullyQualifiedClassPrefix = DEFAULT_PATTERN_CLASS_PACKAGE,
            classpath = outputPatterns.absolutePath + File.pathSeparator + library.classpath,
            outputDirectory = outputTests.absolutePath,
            generationTimeoutSeconds = timeout,
            processStandardStream = processStandardStream,
            processErrorStream = processErrorStream
        ).run()
    }
}
