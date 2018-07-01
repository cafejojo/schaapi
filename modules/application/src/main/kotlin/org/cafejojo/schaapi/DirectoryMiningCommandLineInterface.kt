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
    private companion object : KLogging()

    private val maven = MavenOptionSet()
    private val directory = DirectoryMavenMinerOptionSet(maven)
    private val patternDetector = CCSpanPatternDetectorOptionSet()
    private val patternFilter = PatternFilterOptionSet()
    private val testGenerator = JimpleEvoSuiteTestGeneratorOptionSet()

    init {
        optionSets.add(maven)
        optionSets.add(directory)
        optionSets.add(patternDetector)
        optionSets.add(patternFilter)
        optionSets.add(testGenerator)
    }

    override fun run(cmd: CommandLine) {
        val libraryProject = JavaMavenProject(libraryDir, maven.dir)
        val jimpleLibraryUsageGraphGenerator = JimpleLibraryUsageGraphGenerator()

        maven.install()

        MiningPipeline(
            outputDirectory = outputDir,
            projectMiner = directory.createMiner(),
            searchOptions = directory.createOptions(),
            libraryProject = libraryProject,
            libraryProjectCompiler = JavaMavenProjectCompiler(true),
            userProjectCompiler = JavaMavenProjectCompiler(),
            libraryUsageGraphGenerator = jimpleLibraryUsageGraphGenerator,
            patternDetector = patternDetector.createPatternDetector(),
            patternFilter = patternFilter.createPatternFilter(libraryProject),
            testGenerator = testGenerator.createTestGenerator(outputDir, libraryProject)
        ).run()

        logger.info { "Found ${jimpleLibraryUsageGraphGenerator.lugStatistics.concreteMethods} concrete methods." }
        logger.info { "Found ${jimpleLibraryUsageGraphGenerator.lugStatistics.allStatements} statements." }
        logger.info { "Found ${jimpleLibraryUsageGraphGenerator.lugStatistics.validStatements} valid statements." }
    }
}
