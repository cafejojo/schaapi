package org.cafejojo.schaapi

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.cafejojo.schaapi.miningpipeline.MiningPipeline
import org.cafejojo.schaapi.miningpipeline.PatternFilter
import org.cafejojo.schaapi.miningpipeline.miner.github.MavenProjectSearchOptions
import org.cafejojo.schaapi.miningpipeline.miner.github.ProjectMiner
import org.cafejojo.schaapi.miningpipeline.patterndetector.ccspan.PatternDetector
import org.cafejojo.schaapi.miningpipeline.patternfilter.jimple.IncompleteInitPatternFilterRule
import org.cafejojo.schaapi.miningpipeline.patternfilter.jimple.LengthPatternFilterRule
import org.cafejojo.schaapi.miningpipeline.projectcompiler.javajar.ProjectCompiler
import org.cafejojo.schaapi.miningpipeline.projectcompiler.javamaven.MavenInstaller
import org.cafejojo.schaapi.miningpipeline.testgenerator.jimpleevosuite.TestGenerator
import org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.LibraryUsageGraphGenerator
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.project.JavaJarProject
import org.cafejojo.schaapi.models.project.JavaMavenProject
import java.io.File
import org.cafejojo.schaapi.miningpipeline.projectcompiler.javamaven.ProjectCompiler as JavaMavenCompiler

private const val DEFAULT_TEST_GENERATOR_TIMEOUT = "60"
private const val DEFAULT_PATTERN_DETECTOR_MINIMUM_COUNT = "3"
private const val DEFAULT_MAX_PROJECTS = "20"
private const val DEFAULT_MAXIMUM_SEQUENCE_LENGTH = "25"

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
    val library = JavaJarProject(File(cmd.getOptionValue('l')))

    val token = cmd.getOptionValue("github_oauth_token") ?: return
    val maxProjects = cmd.getOptionOrDefault("max_projects", DEFAULT_MAX_PROJECTS).toInt()
    val groupId = cmd.getOptionValue("library_group_id") ?: return
    val artifactId = cmd.getOptionValue("library_artifact_id") ?: return
    val version = cmd.getOptionValue("library_version") ?: return

    if (!mavenDir.resolve("bin/mvn").exists() || cmd.hasOption("repair_maven")) {
        MavenInstaller().installMaven(mavenDir)
    }

    val testGeneratorTimeout = cmd.getOptionOrDefault("test_generator_timeout", DEFAULT_TEST_GENERATOR_TIMEOUT).toInt()
    val testGeneratorEnableOutput = cmd.hasOption("test_generator_enable_output")

    MiningPipeline(
        outputDirectory = output,
        projectMiner = ProjectMiner(token, output) { JavaMavenProject(it, mavenDir) },
        searchOptions = MavenProjectSearchOptions(groupId, artifactId, version, maxProjects),
        libraryProjectCompiler = ProjectCompiler(),
        userProjectCompiler = JavaMavenCompiler(),
        libraryUsageGraphGenerator = LibraryUsageGraphGenerator,
        patternDetector = PatternDetector(
            cmd.getOptionOrDefault("pattern_detector_minimum_count", DEFAULT_PATTERN_DETECTOR_MINIMUM_COUNT).toInt(),
            cmd.getOptionOrDefault("pattern_detector_maximum_sequence_length", DEFAULT_MAXIMUM_SEQUENCE_LENGTH).toInt(),
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
    ).run(library)
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
            .required()
            .build())
        .addOption(Option
            .builder()
            .longOpt("library_artifact_id")
            .desc("Artifact id of library mined projects should have a dependency on.")
            .hasArg()
            .required()
            .build())
        .addOption(Option
            .builder()
            .longOpt("library_version")
            .desc("Version of library mined projects should have a dependency on.")
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
fun CommandLine.getOptionOrDefault(option: String, default: String) =
    getOptionValue(option) ?: default
