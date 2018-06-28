package org.cafejojo.schaapi

import mu.KLogging
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.cafejojo.schaapi.miningpipeline.PatternFilter
import org.cafejojo.schaapi.miningpipeline.miner.directory.DirectoryProjectMiner
import org.cafejojo.schaapi.miningpipeline.miner.directory.DirectorySearchOptions
import org.cafejojo.schaapi.miningpipeline.miner.github.GitHubProjectMiner
import org.cafejojo.schaapi.miningpipeline.miner.github.MavenProjectSearchOptions
import org.cafejojo.schaapi.miningpipeline.patterndetector.ccspan.CCSpanPatternDetector
import org.cafejojo.schaapi.miningpipeline.patternfilter.jimple.EmptyLoopPatternFilterRule
import org.cafejojo.schaapi.miningpipeline.patternfilter.jimple.IncompleteInitPatternFilterRule
import org.cafejojo.schaapi.miningpipeline.patternfilter.jimple.InsufficientLibraryUsageFilter
import org.cafejojo.schaapi.miningpipeline.patternfilter.jimple.LengthPatternFilterRule
import org.cafejojo.schaapi.miningpipeline.projectcompiler.javamaven.MavenInstaller
import org.cafejojo.schaapi.miningpipeline.testgenerator.jimpleevosuite.TestGenerator
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimplePathEnumerator
import org.cafejojo.schaapi.models.project.JavaMavenProject
import org.cafejojo.schaapi.models.project.JavaProject
import java.io.File

abstract class Snippet {
    abstract fun addOptionsTo(options: Options): Options

    abstract fun setUp(cmd: CommandLine)
}

class MavenSnippet : Snippet() {
    lateinit var dir: File
    var repair = false

    override fun addOptionsTo(options: Options): Options =
        options
            .addOption(Option
                .builder()
                .longOpt("maven_dir")
                .desc("The directory to run Maven from.")
                .hasArg()
                .build())
            .addOption(Option
                .builder()
                .longOpt("repair_maven")
                .desc("Repairs the Maven installation.")
                .build())

    override fun setUp(cmd: CommandLine) {
        dir = File(cmd.getOptionValue("maven_dir") ?: JavaMavenProject.DEFAULT_MAVEN_HOME.absolutePath)
        repair = cmd.hasOption("repair_maven")
    }

    fun run() {
        MavenInstaller().installMaven(dir, overwrite = repair)
    }
}

class GitHubMavenMinerSnippet(private val maven: MavenSnippet) : Snippet() {
    companion object : KLogging()

    lateinit var token: String
    var maxProjects = 0
    lateinit var groupId: String
    lateinit var artifactId: String
    lateinit var version: String
    var sortByStargazers = false
    var sortByWatchers = false

    override fun addOptionsTo(options: Options): Options = options
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

    override fun setUp(cmd: CommandLine) {
        token = cmd.getOptionValue("github_oauth_token")
        maxProjects = cmd.getOptionValue("max_projects", DEFAULT_MAX_PROJECTS).toInt()
        groupId = cmd.getOptionValue("library_group_id")
        artifactId = cmd.getOptionValue("library_artifact_id")
        version = cmd.getOptionValue("library_version")
        sortByStargazers = cmd.hasOption("sort_by_stargazers")
        sortByWatchers = cmd.hasOption("sort_by_watchers")

        if (sortByStargazers && sortByWatchers) {
            logger.error { "Cannot sort repositories on both stargazers and watchers." }
        }
    }

    fun createMiner(outputDir: File) = GitHubProjectMiner(token, outputDir) { JavaMavenProject(it, maven.dir) }

    fun createOptions() =
        MavenProjectSearchOptions(groupId, artifactId, version, maxProjects)
            .also {
                it.sortByStargazers = sortByStargazers
                it.sortByWatchers = sortByWatchers
            }
}

class DirectoryMavenMinerSnippet(private val maven: MavenSnippet) : Snippet() {
    lateinit var userDirDir: File

    override fun addOptionsTo(options: Options): Options = options
        .addOption(Option
            .builder("u")
            .longOpt("user_base_dir")
            .desc("The directory containing user project directories.")
            .hasArg()
            .required()
            .build())

    override fun setUp(cmd: CommandLine) {
        userDirDir = File(cmd.getOptionValue("u"))
    }

    fun createMiner() = DirectoryProjectMiner { JavaMavenProject(it, maven.dir) }

    fun createOptions() = DirectorySearchOptions(userDirDir)
}

class CCSpanPatternDetectorSnippet : Snippet() {
    var minCount = 0
    var maxSequenceLength = 0

    override fun addOptionsTo(options: Options): Options = options
        .addOption(Option
            .builder()
            .longOpt("pattern_detector_minimum_count")
            .desc("The minimum number of occurrences for a statement to be considered frequent.")
            .type(Int::class.java)
            .hasArg()
            .build())
        .addOption(Option
            .builder()
            .longOpt("pattern_detector_maximum_sequence_length")
            .desc("The maximum length of sequences to be considered for pattern detection.")
            .type(Int::class.java)
            .hasArg()
            .build())

    override fun setUp(cmd: CommandLine) {
        minCount =
            cmd.getOptionValue("pattern_detector_minimum_count", DEFAULT_MINIMUM_COUNT).toInt()
        maxSequenceLength =
            cmd.getOptionValue("pattern_detector_maximum_sequence_length", DEFAULT_MAX_SEQUENCE_LENGTH).toInt()
    }

    fun createPatternDetector() =
        CCSpanPatternDetector(
            minCount,
            { JimplePathEnumerator(it, maxSequenceLength) },
            GeneralizedNodeComparator()
        )

    companion object {
        private const val DEFAULT_MINIMUM_COUNT = "2"
        private const val DEFAULT_MAX_SEQUENCE_LENGTH = "25"
    }
}

class PatternFilterSnippet : Snippet() {
    var minLibraryUsageCount = 0

    override fun addOptionsTo(options: Options): Options {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setUp(cmd: CommandLine) {
        minLibraryUsageCount =
            cmd.getOptionValue("pattern_minimum_library_usage_count", DEFAULT_MIN_LIBRARY_USAGE_COUNT).toInt()
    }

    fun createPatternFilter(libraryProject: JavaProject) = PatternFilter(
        IncompleteInitPatternFilterRule(),
        LengthPatternFilterRule(),
        EmptyLoopPatternFilterRule(),
        InsufficientLibraryUsageFilter(libraryProject, minLibraryUsageCount)
    )

    companion object {
        private const val DEFAULT_MIN_LIBRARY_USAGE_COUNT = "1"
    }
}

class JimpleEvoSuiteTestGeneratorSnippet : Snippet() {
    var timeout = 0
    var enableOutput = false

    override fun addOptionsTo(options: Options): Options = options
        .addOption(Option
            .builder()
            .longOpt("test_generator_enable_output")
            .desc("True if test generator output should be shown.")
            .hasArg(false)
            .build())
        .addOption(Option
            .builder()
            .longOpt("test_generator_timeout")
            .desc("The time limit for the test generator.")
            .type(Int::class.java)
            .hasArg()
            .build())

    override fun setUp(cmd: CommandLine) {
        timeout = cmd.getOptionValue("test_generator_timeout", DEFAULT_TIMEOUT).toInt()
        enableOutput = cmd.hasOption("test_generator_enable_output")
    }

    fun createTestGenerator(outputDir: File, libraryProject: JavaProject) =
        TestGenerator(
            outputDirectory = outputDir,
            library = libraryProject,
            timeout = timeout,
            processStandardStream = if (enableOutput) System.out else null,
            processErrorStream = if (enableOutput) System.out else null
        )

    companion object {
        private const val DEFAULT_TIMEOUT = "60"
    }
}
