package org.cafejojo.schaapi

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.project.java.JavaJarProject
import org.cafejojo.schaapi.models.project.java.JavaMavenProject
import org.cafejojo.schaapi.pipeline.PatternFilter
import org.cafejojo.schaapi.pipeline.patterndetector.prefixspan.PatternDetector
import org.cafejojo.schaapi.pipeline.patternfilter.jimple.IncompleteInitPatternFilterRule
import org.cafejojo.schaapi.pipeline.patternfilter.jimple.LengthPatternFilterRule
import org.cafejojo.schaapi.pipeline.projectcompiler.javamaven.MavenInstaller
import org.cafejojo.schaapi.pipeline.testgenerator.jimpleevosuite.TestGenerator
import org.cafejojo.schaapi.pipeline.usagegraphgenerator.jimple.LibraryUsageGraphGenerator
import java.io.File
import org.cafejojo.schaapi.pipeline.projectcompiler.javajar.ProjectCompiler as JavaJarCompiler
import org.cafejojo.schaapi.pipeline.projectcompiler.javamaven.ProjectCompiler as JavaMavenCompiler

private const val DEFAULT_TEST_GENERATOR_TIMEOUT = "60"
private const val DEFAULT_PATTERN_DETECTOR_MINIMUM_COUNT = "3"

/**
 * Runs the complete first phase of the Schaapi pipeline.
 *
 * @param args the path to the output directory, the path to the library project, and the paths to the user projects
 */
fun main(args: Array<String>) {
    val options = buildOptions()
    val cmd = parseArgs(options, args) ?: return

    val mavenDir = File(cmd.getOptionValue("maven_dir") ?: JavaMavenProject.DEFAULT_MAVEN_HOME.absolutePath)
    val output = File(cmd.getOptionValue('o')).apply { mkdirs() }
    val library = JavaMavenProject(File(cmd.getOptionValue('l')), mavenDir)
    val users = cmd.getOptionValues('u').map { JavaJarProject(File(it)) }

    if (!mavenDir.resolve("bin/mvn").exists() || cmd.hasOption("repair_maven")) {
        MavenInstaller().installMaven(mavenDir)
    }

    val testGeneratorTimeout = cmd.getOptionOrDefault("test_generator_timeout", DEFAULT_TEST_GENERATOR_TIMEOUT).toInt()
    val testGeneratorEnableOutput = cmd.hasOption("test_generator_enable_output")

    Pipeline(
        libraryProjectCompiler = JavaMavenCompiler(),
        userProjectCompiler = JavaJarCompiler(),
        libraryUsageGraphGenerator = LibraryUsageGraphGenerator,
        patternDetector = PatternDetector(
            cmd.getOptionOrDefault("pattern_detector_minimum_count", DEFAULT_PATTERN_DETECTOR_MINIMUM_COUNT).toInt(),
            GeneralizedNodeComparator()
        ),
        patternFilter = PatternFilter(
            IncompleteInitPatternFilterRule(),
            LengthPatternFilterRule()
        ),
        testGenerator = TestGenerator(
            library = library,
            outputDirectory = output,
            timeout = testGeneratorTimeout,
            processStandardStream = if (testGeneratorEnableOutput) System.out else null,
            processErrorStream = if (testGeneratorEnableOutput) System.out else null
        )
    ).run(users, library)
}

private fun buildOptions(): Options =
    Options()
        .addOption(Option
            .builder("o")
            .longOpt("output_dir")
            .desc("The output directory.")
            .hasArg()
            .required()
            .build())
        .addOption(Option
            .builder("l")
            .longOpt("library_dir")
            .desc("The library directory.")
            .hasArg()
            .required()
            .build())
        .addOption(Option
            .builder("u")
            .longOpt("user_dirs")
            .desc("The user directories, separated by semi-colons.")
            .hasArgs()
            .valueSeparator(File.pathSeparatorChar)
            .required()
            .build())
        .addOption(Option
            .builder()
            .longOpt("maven_dir")
            .desc("The directory to run Maven from.")
            .hasArg()
            .build())
        .addOption(Option
            .builder()
            .longOpt("repair_maven")
            .desc("Repairs the Maven installation.")
            .build())
        .addOption(Option
            .builder()
            .longOpt("pattern_detector_minimum_count")
            .desc("The minimum number of occurrences for a statement to be considered frequent.")
            .type(Int::class.java)
            .hasArg()
            .build())
        .addOption(Option
            .builder()
            .longOpt("test_generator_enable_output")
            .desc("True if test generator output should be shown.")
            .hasArg(false)
            .build())
        .addOption(Option
            .builder()
            .longOpt("test_generator_timeout")
            .desc("The time limit for the test generator.")
            .type(Int::class.java)
            .hasArg()
            .build())

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
