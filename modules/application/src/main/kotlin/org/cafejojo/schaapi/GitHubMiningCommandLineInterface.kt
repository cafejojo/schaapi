package org.cafejojo.schaapi

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.cafejojo.schaapi.miningpipeline.MiningPipeline
import org.cafejojo.schaapi.miningpipeline.PatternFilter
import org.cafejojo.schaapi.miningpipeline.miner.github.MavenProjectSearchOptions
import org.cafejojo.schaapi.miningpipeline.miner.github.ProjectMiner
import org.cafejojo.schaapi.miningpipeline.patterndetector.ccspan.PatternDetector
import org.cafejojo.schaapi.miningpipeline.patternfilter.jimple.IncompleteInitPatternFilterRule
import org.cafejojo.schaapi.miningpipeline.patternfilter.jimple.LengthPatternFilterRule
import org.cafejojo.schaapi.miningpipeline.projectcompiler.javajar.ProjectCompiler
import org.cafejojo.schaapi.miningpipeline.testgenerator.jimpleevosuite.TestGenerator
import org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.LibraryUsageGraphGenerator
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.project.JavaJarProject
import org.cafejojo.schaapi.models.project.JavaMavenProject
import java.io.File

internal const val DEFAULT_MAX_PROJECTS = "20"

/**
 * Mines GitHub for user projects and generates tests based on these projects.
 *
 * Assumes that the passed library is a java jar project.
 */
internal class GitHubMiningCommandLineInterface {
    internal companion object {
        fun addOptionsTo(options: Options): Options =
            options
                .addOption(Option
                    .builder()
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
    }

    /**
     * Mine GitHub.
     *
     * @throws [MissingArgumentException] if required arguments not set in [CommandLine].
     */
    fun run(cmd: CommandLine, mavenDir: File, library: File, output: File) {
        val token = cmd.getOptionOrThrowException("github_oauth_token")
        val maxProjects = cmd.getOptionOrDefault("max_projects", DEFAULT_MAX_PROJECTS).toInt()
        val groupId = cmd.getOptionOrThrowException("library_group_id")
        val artifactId = cmd.getOptionOrThrowException("library_artifact_id")
        val version = cmd.getOptionOrThrowException("library_version")

        val patternDetectorMinCount = cmd
            .getOptionOrDefault("pattern_detector_minimum_count", DEFAULT_PATTERN_DETECTOR_MINIMUM_COUNT).toInt()
        val maxSequenceLength = cmd
            .getOptionOrDefault("pattern_detector_maximum_sequence_length", DEFAULT_MAX_SEQUENCE_LENGTH).toInt()

        val testGeneratorTimeout = cmd
            .getOptionOrDefault("test_generator_timeout", DEFAULT_TEST_GENERATOR_TIMEOUT).toInt()
        val testGeneratorEnableOutput = cmd.hasOption("test_generator_enable_output")

        val libraryJar = JavaJarProject(library)

        MiningPipeline(
            outputDirectory = output,
            projectMiner = ProjectMiner(token, output) { JavaMavenProject(it, mavenDir) },
            searchOptions = MavenProjectSearchOptions(groupId, artifactId, version, maxProjects),
            libraryProjectCompiler = ProjectCompiler(),
            userProjectCompiler = org.cafejojo.schaapi.miningpipeline.projectcompiler.javamaven.ProjectCompiler(),
            libraryUsageGraphGenerator = LibraryUsageGraphGenerator,
            patternDetector = PatternDetector(patternDetectorMinCount, maxSequenceLength, GeneralizedNodeComparator()),
            patternFilter = PatternFilter(IncompleteInitPatternFilterRule(), LengthPatternFilterRule()),
            testGenerator = TestGenerator(
                library = libraryJar,
                outputDirectory = output,
                timeout = testGeneratorTimeout,
                processStandardStream = if (testGeneratorEnableOutput) System.out else null,
                processErrorStream = if (testGeneratorEnableOutput) System.out else null
            )
        ).run(libraryJar)
    }
}