package org.cafejojo.schaapi

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.cafejojo.schaapi.miningpipeline.MiningPipeline
import org.cafejojo.schaapi.miningpipeline.PatternFilter
import org.cafejojo.schaapi.miningpipeline.miner.directory.DirectorySearchOptions
import org.cafejojo.schaapi.miningpipeline.miner.directory.ProjectMiner
import org.cafejojo.schaapi.miningpipeline.patterndetector.ccspan.PatternDetector
import org.cafejojo.schaapi.miningpipeline.patternfilter.jimple.IncompleteInitPatternFilterRule
import org.cafejojo.schaapi.miningpipeline.patternfilter.jimple.LengthPatternFilterRule
import org.cafejojo.schaapi.miningpipeline.projectcompiler.javamaven.ProjectCompiler
import org.cafejojo.schaapi.miningpipeline.testgenerator.jimpleevosuite.TestGenerator
import org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.LibraryUsageGraphGenerator
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.project.JavaMavenProject
import java.io.File

/**
 * Mines a directory for user projects and generates tests based on these projects.
 *
 * Assumes that the passed library is a java maven project.
 */
internal class DirectoryMiningCommandLineInterface {
    companion object {
        internal fun addOptionsTo(options: Options) = options
            .addOption(Option
                .builder("u")
                .longOpt("user_base_dir")
                .desc("The directory containing user project directories.")
                .hasArg()
                .build())
    }

    /**
     * Mines a Directory.
     *
     * @throws [MissingArgumentException] if required arguments not set in [CommandLine].
     */
    fun run(cmd: CommandLine, mavenDir: File, library: File, output: File) {
        val userDirDirs = cmd.getOptionValue('u') ?: throw MissingArgumentException("u")

        val testGeneratorTimeout = cmd
            .getOptionOrDefault("test_generator_timeout", DEFAULT_TEST_GENERATOR_TIMEOUT).toInt()
        val testGeneratorEnableOutput = cmd.hasOption("test_generator_enable_output")

        val patternDetectorMinCount = cmd
            .getOptionOrDefault("pattern_detector_minimum_count", DEFAULT_PATTERN_DETECTOR_MINIMUM_COUNT).toInt()
        val maxSequenceLength = cmd
            .getOptionOrDefault("pattern_detector_maximum_sequence_length", DEFAULT_MAX_SEQUENCE_LENGTH).toInt()

        val libraryMaven = JavaMavenProject(library)

        MiningPipeline(
            outputDirectory = output,
            projectMiner = ProjectMiner { JavaMavenProject(it, mavenDir) },
            searchOptions = DirectorySearchOptions(File(userDirDirs)),
            libraryProjectCompiler = ProjectCompiler(),
            userProjectCompiler = ProjectCompiler(),
            libraryUsageGraphGenerator = LibraryUsageGraphGenerator,
            patternDetector = PatternDetector(patternDetectorMinCount, maxSequenceLength, GeneralizedNodeComparator()),
            patternFilter = PatternFilter(IncompleteInitPatternFilterRule(), LengthPatternFilterRule()),
            testGenerator = TestGenerator(
                library = libraryMaven,
                outputDirectory = output,
                timeout = testGeneratorTimeout,
                processStandardStream = if (testGeneratorEnableOutput) System.out else null,
                processErrorStream = if (testGeneratorEnableOutput) System.out else null
            )
        ).run(libraryMaven)
    }
}
