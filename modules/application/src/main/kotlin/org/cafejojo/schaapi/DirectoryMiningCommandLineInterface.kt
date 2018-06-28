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
internal class DirectoryMiningCommandLineInterface : CommandLineInterface() {
    internal companion object : KLogging()

    private val maven = MavenSnippet()

    init {
        snippets.add(maven)
    }

    override fun buildOptions(): Options {
        return super.buildOptions()
            .addOption(Option
                .builder("u")
                .longOpt("user_base_dir")
                .desc("The directory containing user project directories.")
                .hasArg()
                .required()
                .build())
    }

    override fun run(cmd: CommandLine) {

        val userDirDirs = cmd.getOptionValue("u")

        val libraryProject = JavaMavenProject(libraryDir, maven.dir)
        val jimpleLibraryUsageGraphGenerator = JimpleLibraryUsageGraphGenerator()

        MiningPipeline(
            outputDirectory = outputDir,
            projectMiner = DirectoryProjectMiner { JavaMavenProject(it, maven.dir) },
            searchOptions = DirectorySearchOptions(File(userDirDirs)),
            libraryProjectCompiler = JavaMavenProjectCompiler(displayOutput = true),
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
                outputDirectory = outputDir,
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
