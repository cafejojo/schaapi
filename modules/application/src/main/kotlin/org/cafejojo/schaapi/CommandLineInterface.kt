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

internal const val DEFAULT_TEST_GENERATOR_TIMEOUT = "60"
internal const val DEFAULT_PATTERN_DETECTOR_MINIMUM_COUNT = "2"
internal const val DEFAULT_MAX_SEQUENCE_LENGTH = "25"
internal const val DEFAULT_MIN_LIBRARY_USAGE_COUNT = "1"

/**
 * Runs the complete first phase of the Schaapi pipeline.
 *
 * @param args the path to the output directory, the path to the library project, and the paths to the user projects
 */
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        KLogging().logger.error { "At least one argument expected." }
        exitProcess(-1)
    }

    val flavor = args[0]
    val remainingArgs = args.drop(1).toTypedArray()
    when (flavor) {
        "directory" -> DirectoryMiningCommandLineInterface().run(remainingArgs)
        "github" -> GitHubMiningCommandLineInterface().run(remainingArgs)
        else -> {
            KLogging().logger.error { "Unrecognized pipeline flavor: $flavor." }
            exitProcess(-1)
        }
    }
}

abstract class CommandLineInterface {
    lateinit var outputDir: File
    lateinit var libraryDir: File

    var testGeneratorTimeout = 0
    var testGeneratorEnableOutput = false

    var patternDetectorMinCount = 0
    var maxSequenceLength = 0
    var minLibraryUsageCount = 0

    val snippets: MutableList<Snippet> = mutableListOf()

    protected open fun buildOptions(): Options {
        return Options()
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
    }

    private fun parse(args: Array<String>): CommandLine {
        val options = buildOptions()
        val parser = DefaultParser()

        return try {
            parser.parse(options, args)
        } catch (e: ParseException) {
            println(e.message)
            printHelpMessage(options)
            exitProcess(-1)
        }
    }

    fun run(args: Array<String>) {
        val cmd = parse(args)

        outputDir = File(cmd.getOptionValue('o')).apply { mkdirs() }
        libraryDir = File(cmd.getOptionValue('l'))

        testGeneratorTimeout = cmd.getOptionValue("test_generator_timeout", DEFAULT_TEST_GENERATOR_TIMEOUT).toInt()
        testGeneratorEnableOutput = cmd.hasOption("test_generator_enable_output")

        patternDetectorMinCount =
            cmd.getOptionValue("pattern_detector_minimum_count", DEFAULT_PATTERN_DETECTOR_MINIMUM_COUNT).toInt()
        maxSequenceLength =
            cmd.getOptionValue("pattern_detector_maximum_sequence_length", DEFAULT_MAX_SEQUENCE_LENGTH).toInt()
        minLibraryUsageCount =
            cmd.getOptionValue("pattern_minimum_library_usage_count", DEFAULT_MIN_LIBRARY_USAGE_COUNT).toInt()
    }

    abstract fun run(cmd: CommandLine)

    private fun printHelpMessage(options: Options) {
        val helpFormatter = HelpFormatter()
        helpFormatter.optionComparator = null
        helpFormatter.printHelp("schaapi", options, true)
    }
}

abstract class Snippet {
    abstract fun addOptionsTo(options: Options): Options

    abstract fun setUp(cmd: CommandLine)

    abstract fun run()
}

class MavenSnippet : Snippet() {
    lateinit var dir: File
    var repair = false

    override fun addOptionsTo(options: Options): Options =
        options
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

    override fun setUp(cmd: CommandLine) {
        dir = File(cmd.getOptionValue("maven_dir") ?: JavaMavenProject.DEFAULT_MAVEN_HOME.absolutePath)
        repair = cmd.hasOption("repair_maven")
    }

    override fun run() {
        MavenInstaller().installMaven(dir, overwrite = repair)
    }
}
