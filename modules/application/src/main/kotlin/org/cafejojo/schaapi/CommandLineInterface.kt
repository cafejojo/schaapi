package org.cafejojo.schaapi

import mu.KLogging
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.cafejojo.schaapi.miningpipeline.projectcompiler.javamaven.MavenInstaller
import org.cafejojo.schaapi.models.project.JavaMavenProject
import java.io.File
import kotlin.system.exitProcess

internal const val DEFAULT_PIPELINE_TYPE = "directory"
internal const val DEFAULT_TEST_GENERATOR_TIMEOUT = "60"
internal const val DEFAULT_PATTERN_DETECTOR_MINIMUM_COUNT = "2"
internal const val DEFAULT_MAX_SEQUENCE_LENGTH = "25"

/**
 * Runs the complete first phase of the Schaapi pipeline.
 *
 * @param args the path to the output directory, the path to the library project, and the paths to the user projects
 */
fun main(args: Array<String>) {
    val options = buildOptions()
        .apply { DirectoryMiningCommandLineInterface.addOptionsTo(this) }
        .apply { GitHubMiningCommandLineInterface.addOptionsTo(this) }

    val cmd = parseArgs(options, args) ?: exitProcess(-1)

    val mavenDir = File(cmd.getOptionValue("maven_dir") ?: JavaMavenProject.DEFAULT_MAVEN_HOME.absolutePath)
    val output = File(cmd.getOptionValue('o')).apply { mkdirs() }
    val library = File(cmd.getOptionValue('l'))

    MavenInstaller().installMaven(mavenDir, overwrite = cmd.hasOption("repair_maven"))

    val flavor = cmd.getOptionValue("flavor", DEFAULT_PIPELINE_TYPE)
    try {
        when (flavor) {
            "directory" -> DirectoryMiningCommandLineInterface().run(cmd, mavenDir, library, output)
            "github" -> GitHubMiningCommandLineInterface().run(cmd, mavenDir, library, output)
            else -> {
                KLogging().logger.error { "Given pipeline flavor was not recognized." }
                exitProcess(-1)
            }
        }
    } catch (e: MissingArgumentException) {
        KLogging().logger.error { e.messageForFlavor(flavor) }
        exitProcess(-1)
    }
}

private fun buildOptions(): Options =
    Options()
        .addOption(Option
            .builder()
            .longOpt("flavor")
            .desc("The desired pipeline flavor")
            .hasArg()
            .build())
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
            .longOpt("pattern_detector_maximum_sequence_length")
            .desc("The maximum length of sequences to be considered for pattern detection.")
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
        println(e.message)
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
 * Returns [CommandLine.getOptionValue], unless this is null, in which case a [MissingArgumentException] is thrown.
 *
 * @param option the name of the option
 * @return [CommandLine.getOptionValue], unless this is null, in which case a [MissingArgumentException] is thrown
 * with the [option] in its message
 */
internal fun CommandLine.getOptionOrThrowException(option: String) =
    getOptionValue(option) ?: throw MissingArgumentException(option)
