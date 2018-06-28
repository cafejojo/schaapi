package org.cafejojo.schaapi

import mu.KLogging
import org.apache.commons.cli.CommandLine
import org.cafejojo.schaapi.miningpipeline.MiningPipeline
import org.cafejojo.schaapi.miningpipeline.PatternFilter
import org.cafejojo.schaapi.miningpipeline.miner.github.GitHubProjectMiner
import org.cafejojo.schaapi.miningpipeline.miner.github.MavenProjectSearchOptions
import org.cafejojo.schaapi.miningpipeline.patterndetector.ccspan.CCSpanPatternDetector
import org.cafejojo.schaapi.miningpipeline.patternfilter.jimple.EmptyLoopPatternFilterRule
import org.cafejojo.schaapi.miningpipeline.patternfilter.jimple.IncompleteInitPatternFilterRule
import org.cafejojo.schaapi.miningpipeline.patternfilter.jimple.InsufficientLibraryUsageFilter
import org.cafejojo.schaapi.miningpipeline.patternfilter.jimple.LengthPatternFilterRule
import org.cafejojo.schaapi.miningpipeline.projectcompiler.javajar.JavaJarProjectCompiler
import org.cafejojo.schaapi.miningpipeline.projectcompiler.javamaven.JavaMavenProjectCompiler
import org.cafejojo.schaapi.miningpipeline.projectcompiler.javamaven.MavenInstaller
import org.cafejojo.schaapi.miningpipeline.testgenerator.jimpleevosuite.TestGenerator
import org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.JimpleLibraryUsageGraphGenerator
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimplePathEnumerator
import org.cafejojo.schaapi.models.project.JavaJarProject
import org.cafejojo.schaapi.models.project.JavaMavenProject

internal const val DEFAULT_MAX_PROJECTS = "20"

/**
 * Mines GitHub for user projects and generates tests based on these projects.
 *
 * Assumes that the passed library is a Java JAR project.
 */
internal class GitHubMiningCommandLineInterface : CommandLineInterface() {
    companion object : KLogging()

    private val maven = MavenSnippet()
    private val gitHub = GitHubMinerSnippet()
    private val patternDetector = PatternDetectorSnippet()
    private val testGenerator = TestGeneratorSnippet()

    init {
        snippets.add(maven)
        snippets.add(gitHub)
        snippets.add(patternDetector)
    }

    override fun run(cmd: CommandLine) {
        val libraryProject = JavaJarProject(libraryDir)
        val jimpleLibraryUsageGraphGenerator = JimpleLibraryUsageGraphGenerator()

        MavenInstaller().installMaven(maven.dir, overwrite = maven.repair)

        MiningPipeline(
            outputDirectory = outputDir,
            projectMiner = GitHubProjectMiner(gitHub.token, outputDir) { JavaMavenProject(it, maven.dir) },
            searchOptions = MavenProjectSearchOptions(gitHub.groupId,
                gitHub.artifactId,
                gitHub.version,
                gitHub.maxProjects).apply {
                this.sortByStargazers = cmd.hasOption("sort_by_stargazers")
                this.sortByWatchers = cmd.hasOption("sort_by_watchers")
            },
            libraryProjectCompiler = JavaJarProjectCompiler(),
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
