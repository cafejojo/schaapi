package org.cafejojo.schaapi

import mu.KLogging
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.cafejojo.schaapi.miningpipeline.MiningPipeline
import org.cafejojo.schaapi.miningpipeline.PatternFilter
import org.cafejojo.schaapi.miningpipeline.miner.directory.DirectoryProjectMiner
import org.cafejojo.schaapi.miningpipeline.miner.directory.DirectorySearchOptions
import org.cafejojo.schaapi.miningpipeline.patterndetector.ccspan.CCSpanPatternDetector
import org.cafejojo.schaapi.miningpipeline.patternfilter.jimple.EmptyLoopPatternFilterRule
import org.cafejojo.schaapi.miningpipeline.patternfilter.jimple.IncompleteInitPatternFilterRule
import org.cafejojo.schaapi.miningpipeline.patternfilter.jimple.InsufficientLibraryUsageFilter
import org.cafejojo.schaapi.miningpipeline.patternfilter.jimple.LengthPatternFilterRule
import org.cafejojo.schaapi.miningpipeline.projectcompiler.javamaven.JavaMavenProjectCompiler
import org.cafejojo.schaapi.miningpipeline.testgenerator.jimpleevosuite.TestGenerator
import org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.JimpleLibraryUsageGraphGenerator
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimplePathEnumerator
import org.cafejojo.schaapi.models.project.JavaMavenProject
import java.io.File

/**
 * Mines a directory for user projects and generates tests based on these projects.
 *
 * Assumes that the passed library is a Java Maven project.
 */
internal class DirectoryMiningCommandLineInterface {
    internal companion object : KLogging() {
        fun addOptionsTo(options: Options): Options =
            options.addOption(Option
                .builder("u")
                .longOpt("user_base_dir")
                .desc("The directory containing user project directories.")
                .hasArg()
                .build())
    }

    /**
     * Mines a directory.
     *
     * @throws [MissingArgumentException] if required arguments not set in [CommandLine].
     */
    fun run(cmd: CommandLine, mavenDir: File, library: File, output: File) {
        val userDirDirs = cmd.getOptionOrThrowException("u")

        val testGeneratorTimeout = cmd.getOptionValue("test_generator_timeout", DEFAULT_TEST_GENERATOR_TIMEOUT).toInt()
        val testGeneratorEnableOutput = cmd.hasOption("test_generator_enable_output")

        val patternDetectorMinCount =
            cmd.getOptionValue("pattern_detector_minimum_count", DEFAULT_PATTERN_DETECTOR_MINIMUM_COUNT).toInt()
        val maxSequenceLength =
            cmd.getOptionValue("pattern_detector_maximum_sequence_length", DEFAULT_MAX_SEQUENCE_LENGTH).toInt()
        val minLibraryUsageCount =
            cmd.getOptionValue("pattern_minimum_library_usage_count", DEFAULT_MIN_LIBRARY_USAGE_COUNT).toInt()

        val libraryProject = JavaMavenProject(library, mavenDir)
        val jimpleLibraryUsageGraphGenerator = JimpleLibraryUsageGraphGenerator()

        MiningPipeline(
            outputDirectory = output,
            projectMiner = DirectoryProjectMiner { JavaMavenProject(it, mavenDir) },
            searchOptions = DirectorySearchOptions(File(userDirDirs)),
            libraryProjectCompiler = JavaMavenProjectCompiler(true),
            userProjectCompiler = JavaMavenProjectCompiler(),
            libraryUsageGraphGenerator = jimpleLibraryUsageGraphGenerator,
            patternDetector = CCSpanPatternDetector(
                patternDetectorMinCount,
                { JimplePathEnumerator(it, maxSequenceLength) },
                GeneralizedNodeComparator()
            ),
            patternFilter = PatternFilter(
                IncompleteInitPatternFilterRule(),
                LengthPatternFilterRule(),
                EmptyLoopPatternFilterRule(),
                InsufficientLibraryUsageFilter(libraryProject, minLibraryUsageCount)
            ),
            testGenerator = TestGenerator(
                library = libraryProject,
                outputDirectory = output,
                timeout = testGeneratorTimeout,
                processStandardStream = if (testGeneratorEnableOutput) System.out else null,
                processErrorStream = if (testGeneratorEnableOutput) System.out else null
            )
        ).run(libraryProject)

        logger.info { "Found ${jimpleLibraryUsageGraphGenerator.lugStatistics.concreteMethods} concrete methods." }
        logger.info { "Found ${jimpleLibraryUsageGraphGenerator.lugStatistics.allStatements} statements." }
        logger.info { "Found ${jimpleLibraryUsageGraphGenerator.lugStatistics.validStatements} valid statements." }
    }
}
