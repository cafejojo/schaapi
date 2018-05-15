package org.cafejojo.schaapi

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.cafejojo.schaapi.patterndetector.PathEnumerator
import org.cafejojo.schaapi.patterndetector.PatternDetector
import org.cafejojo.schaapi.projectcompiler.JavaMavenProject
import org.cafejojo.schaapi.testgenerator.EvoSuiteRunner
import org.cafejojo.schaapi.testgenerator.SootClassGenerator
import org.cafejojo.schaapi.testgenerator.SootClassWriter
import org.cafejojo.schaapi.usagegraphgenerator.SootNode
import org.cafejojo.schaapi.usagegraphgenerator.SootProjectLibraryUsageGraphGenerator
import soot.jimple.Stmt
import java.io.File

private const val DEFAULT_PATTERN_CLASS_NAME = "RegressionTest"
private const val DEFAULT_TEST_GENERATOR_TIMEOUT = "60"
private const val DEFAULT_PATTERN_DETECTOR_MINIMUM_COUNT = "3"

/**
 * Runs the complete first phase of the Schaapi pipeline.
 *
 * @param args the path to the output directory, the path to the library project, and the paths to the user projects
 */
@SuppressWarnings("UnsafeCast")
fun main(args: Array<String>) {
    val options = buildOptions()
    val cmd = parseArgs(options, args) ?: return

    val output = File(cmd.getOptionValue('o')).apply { mkdirs() }
    val outputPatterns = output.resolve("patterns/").apply { mkdirs() }
    val outputTests = output.resolve("tests/").apply { mkdirs() }
    val library = JavaMavenProject(File(cmd.getOptionValue('l')))
    val users = cmd.getOptionValues('u').map { JavaMavenProject(File(it)) }

    library.compile()
    users.forEach { it.compile() }

    val userGraphs = users.map { SootProjectLibraryUsageGraphGenerator.generate(library, it) }
    val userPaths = userGraphs.flatMap { it.flatMap { it.flatMap { PathEnumerator(it).enumerate() } } }

    val patterns = PatternDetector(
        userPaths,
        cmd.getOptionOrDefault("pattern_detector_minimum_count", DEFAULT_PATTERN_DETECTOR_MINIMUM_COUNT).toInt()
    ).findFrequentSequences()

    val classGenerator = SootClassGenerator(DEFAULT_PATTERN_CLASS_NAME)
    patterns.forEachIndexed { index, pattern ->
        classGenerator.generateMethod("pattern$index", pattern.map { unit -> (unit as SootNode).unit as Stmt })
    }

    SootClassWriter.writeToFile(classGenerator.sootClass, outputPatterns.absolutePath)

    val testGeneratorTimeout = cmd.getOptionOrDefault("test_generator_timeout", DEFAULT_TEST_GENERATOR_TIMEOUT).toInt()
    EvoSuiteRunner(
        fullyQualifiedClassName = DEFAULT_PATTERN_CLASS_NAME,
        classPath = outputPatterns.absolutePath + ";" + library.classpath,
        outputDirectory = outputTests.absolutePath,
        generationTimeoutSeconds = testGeneratorTimeout
    ).run()
}

private fun buildOptions(): Options {
    val options = Options()

    options
        .addOption(Option
            .builder("o")
            .longOpt("output_dir")
            .desc("The output directory.")
            .hasArg(true)
            .required()
            .build())
        .addOption(Option
            .builder("l")
            .longOpt("library_dir")
            .desc("The library directory.")
            .hasArg(true)
            .required()
            .build())
        .addOption(Option
            .builder("u")
            .longOpt("user_dirs")
            .desc("The user directories, separated by semi-colons.")
            .hasArg(true)
            .valueSeparator(';')
            .required()
            .build())
        .addOption(Option
            .builder()
            .longOpt("pattern_detector_minimum_count")
            .desc("The minimum number of occurrences for a statement to be considered frequent.")
            .type(Int::class.java)
            .hasArg(true)
            .build())
        .addOption(Option
            .builder()
            .longOpt("test_generator_timeout")
            .desc("The time limit for the test generator.")
            .type(Int::class.java)
            .hasArg(true)
            .build())

    return options
}

private fun parseArgs(options: Options, args: Array<String>): CommandLine? {
    val parser = DefaultParser()

    return try {
        parser.parse(options, args)
    } catch (e: ParseException) {
        printHelpMessage(options)
        null
    }
}

private fun printHelpMessage(options: Options) {
    val helpFormatter = HelpFormatter()
    helpFormatter.optionComparator = null
    helpFormatter.printHelp("schaapi", options, true)
}

/**
 * Returns [CommandLine::getOptionValue], unless this is null, in which case [default] is returned.
 *
 * @param option the name of the option
 * @param default the value to return if the option's value is null
 * @return [CommandLine::getOptionValue], unless this is null, in which case [default] is returned
 */
fun CommandLine.getOptionOrDefault(option: String, default: String) =
    getOptionValue(option) ?: default
