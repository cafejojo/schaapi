package org.cafejojo.schaapi

import mu.KLogging
import org.apache.commons.cli.CommandLine
import org.cafejojo.schaapi.miningpipeline.MiningPipeline
import org.cafejojo.schaapi.miningpipeline.projectcompiler.javajar.JavaJarProjectCompiler
import org.cafejojo.schaapi.miningpipeline.projectcompiler.javamaven.JavaMavenProjectCompiler
import org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.JimpleLibraryUsageGraphGenerator
import org.cafejojo.schaapi.models.project.JavaJarProject

internal const val DEFAULT_MAX_PROJECTS = "20"

/**
 * Mines GitHub for user projects and generates tests based on these projects.
 *
 * Assumes that the passed library is a Java JAR project.
 */
internal class GitHubMiningCommandLineInterface : CommandLineInterface() {
    companion object : KLogging()

    private val maven = MavenSnippet()
    private val gitHub = GitHubMavenMinerSnippet(maven)
    private val patternDetector = CCSpanPatternDetectorSnippet()
    private val patternFilter = PatternFilterSnippet()
    private val testGenerator = JimpleEvoSuiteTestGeneratorSnippet()

    init {
        snippets.add(maven)
        snippets.add(gitHub)
        snippets.add(patternFilter)
        snippets.add(patternDetector)
        snippets.add(testGenerator)
    }

    override fun run(cmd: CommandLine) {
        val libraryProject = JavaJarProject(libraryDir)
        val jimpleLibraryUsageGraphGenerator = JimpleLibraryUsageGraphGenerator()

        maven.install()

        MiningPipeline(
            outputDirectory = outputDir,
            projectMiner = gitHub.createMiner(outputDir),
            searchOptions = gitHub.createOptions(),
            libraryProjectCompiler = JavaJarProjectCompiler(),
            userProjectCompiler = JavaMavenProjectCompiler(),
            libraryUsageGraphGenerator = jimpleLibraryUsageGraphGenerator,
            patternDetector = patternDetector.createPatternDetector(),
            patternFilter = patternFilter.createPatternFilter(libraryProject),
            testGenerator = testGenerator.createTestGenerator(outputDir, libraryProject)
        ).run(libraryProject)

        logger.info { "Found ${jimpleLibraryUsageGraphGenerator.lugStatistics.concreteMethods} concrete methods." }
        logger.info { "Found ${jimpleLibraryUsageGraphGenerator.lugStatistics.allStatements} statements." }
        logger.info { "Found ${jimpleLibraryUsageGraphGenerator.lugStatistics.validStatements} valid statements." }
    }
}
