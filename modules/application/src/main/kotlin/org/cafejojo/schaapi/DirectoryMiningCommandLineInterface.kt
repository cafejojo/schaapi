package org.cafejojo.schaapi

import mu.KLogging
import org.apache.commons.cli.CommandLine
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

/**
 * Mines a directory for user projects and generates tests based on these projects.
 *
 * Assumes that the passed library is a Java Maven project.
 */
internal class DirectoryMiningCommandLineInterface : CommandLineInterface() {
    internal companion object : KLogging()

    private val maven = MavenSnippet()
    private val directory = DirectoryMinerSnippet()
    private val patternDetector = PatternDetectorSnippet()
    private val testGenerator = TestGeneratorSnippet()

    init {
        snippets.add(maven)
        snippets.add(directory)
        snippets.add(patternDetector)
        snippets.add(testGenerator)
    }

    override fun run(cmd: CommandLine) {
        val libraryProject = JavaMavenProject(libraryDir, maven.dir)
        val jimpleLibraryUsageGraphGenerator = JimpleLibraryUsageGraphGenerator()

        MiningPipeline(
            outputDirectory = outputDir,
            projectMiner = DirectoryProjectMiner { JavaMavenProject(it, maven.dir) },
            searchOptions = DirectorySearchOptions(directory.userDirDir),
            libraryProjectCompiler = JavaMavenProjectCompiler(displayOutput = true),
            userProjectCompiler = JavaMavenProjectCompiler(),
            libraryUsageGraphGenerator = jimpleLibraryUsageGraphGenerator,
            patternDetector = CCSpanPatternDetector(
                patternDetector.minCount,
                { JimplePathEnumerator(it, patternDetector.maxSequenceLength) },
                GeneralizedNodeComparator()
            ),
            patternFilter = PatternFilter(
                IncompleteInitPatternFilterRule(),
                LengthPatternFilterRule(),
                EmptyLoopPatternFilterRule(),
                InsufficientLibraryUsageFilter(libraryProject, patternDetector.minLibraryUsageCount)
            ),
            testGenerator = TestGenerator(
                library = libraryProject,
                outputDirectory = outputDir,
                timeout = testGenerator.timeout,
                processStandardStream = if (testGenerator.enableOutput) System.out else null,
                processErrorStream = if (testGenerator.enableOutput) System.out else null
            )
        ).run(libraryProject)

        logger.info { "Found ${jimpleLibraryUsageGraphGenerator.lugStatistics.concreteMethods} concrete methods." }
        logger.info { "Found ${jimpleLibraryUsageGraphGenerator.lugStatistics.allStatements} statements." }
        logger.info { "Found ${jimpleLibraryUsageGraphGenerator.lugStatistics.validStatements} valid statements." }
    }
}
