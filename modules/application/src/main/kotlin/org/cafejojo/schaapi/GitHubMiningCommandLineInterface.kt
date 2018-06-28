package org.cafejojo.schaapi

import mu.KLogging
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
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
    private val maven = MavenSnippet()

    init {
        snippets.add(maven)
    }

    companion object : KLogging()

    override fun buildOptions(): Options {
        return super.buildOptions()
            .addOption(Option
                .builder()
                .longOpt("github_oauth_token")
                .desc("Token of GitHub account used for searching.")
                .hasArg()
                .required()
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
                .longOpt("sort_by_stargazers")
                .desc("True if GitHub projects should be sorted by stars.")
                .hasArg(false)
                .build())
            .addOption(Option
                .builder()
                .longOpt("sort_by_watchers")
                .desc("True if GitHub projects should be sorted by watchers.")
                .hasArg(false)
                .build())
    }

    override fun run(cmd: CommandLine) {
        val token = cmd.getOptionValue("github_oauth_token")
        val maxProjects = cmd.getOptionValue("max_projects", DEFAULT_MAX_PROJECTS).toInt()
        val groupId = cmd.getOptionValue("library_group_id")
        val artifactId = cmd.getOptionValue("library_artifact_id")
        val version = cmd.getOptionValue("library_version")

        if (cmd.hasOption("sort_by_stargazers") && cmd.hasOption("sort_by_watchers")) {
            logger.error { "Cannot sort repositories on both stargazers and watchers." }
        }

        val libraryProject = JavaJarProject(libraryDir)
        val jimpleLibraryUsageGraphGenerator = JimpleLibraryUsageGraphGenerator()

        maven.run()

        MiningPipeline(
            outputDirectory = outputDir,
            projectMiner = GitHubProjectMiner(token, outputDir) { JavaMavenProject(it, maven.dir) },
            searchOptions = MavenProjectSearchOptions(groupId, artifactId, version, maxProjects).apply {
                this.sortByStargazers = cmd.hasOption("sort_by_stargazers")
                this.sortByWatchers = cmd.hasOption("sort_by_watchers")
            },
            libraryProjectCompiler = JavaJarProjectCompiler(),
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
