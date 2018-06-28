package org.cafejojo.schaapi

import mu.KLogging
import org.apache.commons.cli.CommandLine
import org.cafejojo.schaapi.miningpipeline.MiningPipeline
import org.cafejojo.schaapi.miningpipeline.projectcompiler.javamaven.JavaMavenProjectCompiler
import org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.JimpleLibraryUsageGraphGenerator
import org.cafejojo.schaapi.models.project.JavaMavenProject

/**
 * Mines a directory for user projects and generates tests based on these projects.
 *
 * Assumes that the passed library is a Java Maven project.
 */
internal class DirectoryMiningCommandLineInterface : CommandLineInterface() {
    internal companion object : KLogging()

    private val maven = MavenSnippet()
    private val directory = DirectoryMavenMinerSnippet(maven)
    private val patternDetector = CCSpanPatternDetectorSnippet()
    private val patternFilter = PatternFilterSnippet()
    private val testGenerator = JimpleEvoSuiteTestGeneratorSnippet()

    init {
        snippets.add(maven)
        snippets.add(directory)
        snippets.add(patternDetector)
        snippets.add(testGenerator)
    }

    override fun run(cmd: CommandLine) {
        val libraryProject = JavaMavenProject(libraryDir, maven.dir)
        val jimpleLibraryUsageGraphGenerator = JimpleLibraryUsageGraphGenerator()

        maven.run()

        MiningPipeline(
            outputDirectory = outputDir,
            projectMiner = directory.createMiner(outputDir),
            searchOptions = directory.createOptions(),
            libraryProjectCompiler = JavaMavenProjectCompiler(true),
            userProjectCompiler = JavaMavenProjectCompiler(),
            libraryUsageGraphGenerator = jimpleLibraryUsageGraphGenerator,
            patternDetector = patternDetector.createPatternDetector(),
            patternFilter = patternFilter.createFilter(libraryProject),
            testGenerator = testGenerator.create(outputDir, libraryProject)
        ).run(libraryProject)

        logger.info { "Found ${jimpleLibraryUsageGraphGenerator.lugStatistics.concreteMethods} concrete methods." }
        logger.info { "Found ${jimpleLibraryUsageGraphGenerator.lugStatistics.allStatements} statements." }
        logger.info { "Found ${jimpleLibraryUsageGraphGenerator.lugStatistics.validStatements} valid statements." }
    }
}
