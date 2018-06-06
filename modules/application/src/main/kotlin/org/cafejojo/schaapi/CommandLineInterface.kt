package org.cafejojo.schaapi

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.cafejojo.schaapi.miningpipeline.projectcompiler.javamaven.MavenInstaller
import org.cafejojo.schaapi.models.project.JavaMavenProject
import java.io.File

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
    val cmd = parseArgs(options, args) ?: return

    val mavenDir = File(cmd.getOptionValue("maven_dir") ?: JavaMavenProject.DEFAULT_MAVEN_HOME.absolutePath)
    val output = File(cmd.getOptionValue('o')).apply { mkdirs() }
    val library = File(cmd.getOptionValue('l'))

    if (!mavenDir.resolve("bin/mvn").exists() || cmd.hasOption("repair_maven")) {
        MavenInstaller().installMaven(mavenDir)
    }

    val type = cmd.getOptionOrDefault("pipeline_type", "")
    try {
        when (type) {
            "directory" -> DirectoryMiningCommandLineInterface().run(cmd, mavenDir, library, output)
            "github" -> GithubMiningCommandLineInterface().run(cmd, mavenDir, library, output)
            else -> println("Given pipeline_type was not recognized.")
        }
    } catch (e: MissingArgumentException) {
        println(e.messageForType(type))
    }
}

private fun buildOptions(): Options =
    Options()
        .addOption(Option
            .builder()
            .longOpt("pipeline_type")
            .desc("The desired pipeline type")
            .hasArg()
            .required()
            .build()
        )
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
            .longOpt("user_base_dir")
            .desc("The directory containing user project directories.")
            .hasArg()
            .build())
        .addOption(Option
            .builder("t")
            .longOpt("github_oauth_token")
            .desc("Token of GitHub account used for searching.")
            .hasArg()
            .build())
        .addOption(Option
            .builder()
            .longOpt("max_projects")
            .desc("Maximum amount of projects to download from GitHub.")
            .hasArg()
            .build())
        .addOption(Option
            .builder()
            .longOpt("library_group_id")
            .desc("Group id of library mined projects should have a dependency on.")
            .hasArg()
            .build())
        .addOption(Option
            .builder()
            .longOpt("library_artifact_id")
            .desc("Artifact id of library mined projects should have a dependency on.")
            .hasArg()
            .build())
        .addOption(Option
            .builder()
            .longOpt("library_version")
            .desc("Version of library mined projects should have a dependency on.")
            .hasArg()
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
 * Returns [CommandLine.getOptionValue], unless this is null, in which case [default] is returned.
 *
 * @param option the name of the option
 * @param default the value to return if the option's value is null
 * @return [CommandLine.getOptionValue], unless this is null, in which case [default] is returned
 */
fun CommandLine.getOptionOrDefault(option: String, default: String) = getOptionValue(option) ?: default
