package org.cafejojo.schaapi.miningpipeline.testgenerator.jimpleevosuite

import me.tongfei.progressbar.ProgressBar
import mu.KLogging
import org.cafejojo.schaapi.miningpipeline.Pattern
import org.cafejojo.schaapi.miningpipeline.TestGenerator
import org.cafejojo.schaapi.miningpipeline.createProgressBarBuilder
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimpleNode
import org.cafejojo.schaapi.models.project.JavaProject
import java.io.File
import java.io.PrintStream

private const val DEFAULT_PATTERN_CLASS_PACKAGE = "org.cafejojo.schaapi.patterns"
private const val DEFAULT_PATTERN_CLASS_NAME_PREFIX = "Pattern"

/**
 * Represents the test generator that generates tests based on patterns.
 */
class TestGenerator(
    private val library: JavaProject,
    outputDirectory: File,
    private val timeout: Int,
    private val parallel: Boolean = false,
    private val processStandardStream: PrintStream? = null,
    private val processErrorStream: PrintStream? = null
) : TestGenerator<JimpleNode> {
    private companion object : KLogging()

    private val outputPatterns = outputDirectory.resolve("patterns/").apply { mkdirs() }
    private val outputTests = outputDirectory.resolve("tests/").apply { mkdirs() }

    override fun generate(patterns: List<Pattern<JimpleNode>>) {
        writePatterns(patterns)
        generateTests(patterns)
    }

    private fun writePatterns(patterns: List<Pattern<JimpleNode>>) {
        logger.info { "Writing patterns to class files." }

        if (patterns.isEmpty()) {
            logger.warn { "No patterns were found in the user programs." }

            ClassGenerator("$DEFAULT_PATTERN_CLASS_PACKAGE.${DEFAULT_PATTERN_CLASS_NAME_PREFIX}_NONE")
                .apply { writeToFile(patternPath(0)) }
        }

        ProgressBar.wrap(patterns, createProgressBarBuilder("Creating pattern classes"))
            .forEachIndexed { i, pattern ->
                logger.debug { "Writing pattern $i to class file." }

                ClassGenerator("$DEFAULT_PATTERN_CLASS_PACKAGE.${DEFAULT_PATTERN_CLASS_NAME_PREFIX}_$i").apply {
                    val method = generateMethod("pattern$i", pattern)
                    optimizeMethod(method)
                    writeToFile(patternPath(i))
                }
            }

        logger.info { "Finished writing patterns to class files." }
    }

    private fun generateTests(patterns: List<Pattern<JimpleNode>>) {
        logger.info { "Running EvoSuite." }

        val progressBarBuilder = createProgressBarBuilder("Generating tests")
        val patternIndices = (0 until patterns.size).toList()
            .let { if (parallel) it.parallelStream() else it.stream() }
            .let { if (processStandardStream == null) ProgressBar.wrap(it, progressBarBuilder) else it }

        patternIndices.forEach { index ->
            logger.debug { "Generating tests for pattern $index." }

            EvoSuiteRunner(
                fullyQualifiedClassPrefix = DEFAULT_PATTERN_CLASS_PACKAGE,
                classpath = patternPath(index) + File.pathSeparator + library.classpath,
                outputDirectory = outputTests.absolutePath,
                generationTimeoutSeconds = timeout,
                processStandardStream = processStandardStream,
                processErrorStream = processErrorStream
            ).run()
        }

        logger.info { "Finished running EvoSuite." }
    }

    private fun patternPath(index: Int) = File(outputPatterns, "pattern$index").absolutePath
}
