package org.cafejojo.schaapi

import mu.KLogging
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.cafejojo.schaapi.maveninstaller.MavenInstaller
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
import org.cafejojo.schaapi.miningpipeline.testgenerator.jimpleevosuite.TestGenerator
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimplePathEnumerator
import org.cafejojo.schaapi.models.project.JavaMavenProject
import org.cafejojo.schaapi.models.project.JavaProject
import java.io.File

/**
 * A piece of behavior for a [CommandLineInterface] that can "translate" parsed command-line arguments into components
 * for the mining pipeline.
 */
abstract class OptionSet {
    /**
     * Adds the command-line arguments specific for this option set to [options], and then returns the updated [options]
     * object.
     *
     * @param options the object to add options to
     * @return the same instance as [options] but with extra options
     */
    abstract fun addOptionsTo(options: Options): Options

    /**
     * Reads parsed command-line options into the fields of this [OptionSet].
     *
     * @param cmd the parsed command-line options
     */
    abstract fun read(cmd: CommandLine)
}

/**
 * Behavior linked to using the Maven distribution.
 */
@Suppress("LateinitUsage") // Values cannot be determined at initialization
class MavenOptionSet : OptionSet() {
    lateinit var dir: File
    private var repair = false

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

    override fun read(cmd: CommandLine) {
        dir = File(cmd.getOptionValue("maven_dir") ?: JavaMavenProject.DEFAULT_MAVEN_HOME.absolutePath)
        repair = cmd.hasOption("repair_maven")
    }

    /**
     * Installs Maven.
     */
    fun install() {
        MavenInstaller().installMaven(dir, overwrite = repair)
    }
}

/**
 * Behavior linked to mining Maven projects with the GitHub miner.
 *
 * @property maven the [MavenOptionSet] describing Maven-related behavior
 */
@Suppress("LateinitUsage") // Values cannot be determined at initialization
class GitHubMavenMinerOptionSet(private val maven: MavenOptionSet) : OptionSet() {
    private lateinit var token: String
    private var maxProjects = 0
    private lateinit var groupId: String
    private lateinit var artifactId: String
    private lateinit var version: String
    private var sortByStargazers = false
    private var sortByWatchers = false

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

    override fun read(cmd: CommandLine) {
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

    /**
     * Creates a GitHub miner for Maven projects.
     *
     * @param outputDir the directory in which the projects should be processed
     * @return a GitHub miner for Maven projects
     */
    fun createMiner(outputDir: File) = GitHubProjectMiner(token, outputDir) { JavaMavenProject(it, maven.dir) }

    /**
     * Creates the mining options for mining GitHub projects.
     *
     * @return the mining options for mining GitHub projects
     */
    fun createOptions() =
        MavenProjectSearchOptions(groupId, artifactId, version, maxProjects)
            .also {
                it.sortByStargazers = sortByStargazers
                it.sortByWatchers = sortByWatchers
            }

    private companion object : KLogging() {
        const val DEFAULT_MAX_PROJECTS = "20"
    }
}

/**
 * Behavior linked to mining Maven projects with the directory miner.
 *
 * @property maven the [MavenOptionSet] describing Maven-related behavior
 */
@Suppress("LateinitUsage") // Values cannot be determined at initialization
class DirectoryMavenMinerOptionSet(private val maven: MavenOptionSet) : OptionSet() {
    private lateinit var userDirDir: File

    override fun addOptionsTo(options: Options): Options = options
        .addOption(Option
            .builder("u")
            .longOpt("user_base_dir")
            .desc("The directory containing user project directories.")
            .hasArg()
            .required()
            .build())

    override fun read(cmd: CommandLine) {
        userDirDir = File(cmd.getOptionValue("u"))
    }

    /**
     * Creates a directory miner for Maven projects.
     *
     * @return a directory miner for Maven projects
     */
    fun createMiner() = DirectoryProjectMiner { JavaMavenProject(it, maven.dir) }

    /**
     * Creates the mining options for mining projects in a directory.
     *
     * @return the mining options for mining projects in a directory
     */
    fun createOptions() = DirectorySearchOptions(userDirDir)
}

/**
 * Behavior linked to detecting patterns with the CCSpan algorithm.
 */
class CCSpanPatternDetectorOptionSet : OptionSet() {
    private var minCount = 0
    private var maxSequenceLength = 0

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

    override fun read(cmd: CommandLine) {
        minCount =
            cmd.getOptionValue("pattern_detector_minimum_count", DEFAULT_MINIMUM_COUNT).toInt()
        maxSequenceLength =
            cmd.getOptionValue("pattern_detector_maximum_sequence_length", DEFAULT_MAX_SEQUENCE_LENGTH).toInt()
    }

    /**
     * Creates a CCSpan pattern detector.
     *
     * @return a CCSpan pattern detector
     */
    fun createPatternDetector() =
        CCSpanPatternDetector(
            minCount,
            { JimplePathEnumerator(it, maxSequenceLength) },
            GeneralizedNodeComparator()
        )

    private companion object {
        const val DEFAULT_MINIMUM_COUNT = "2"
        const val DEFAULT_MAX_SEQUENCE_LENGTH = "25"
    }
}

/**
 * Creates a pattern filter with a number of filter rules.
 */
class PatternFilterOptionSet : OptionSet() {
    private var minLibraryUsageCount = 0

    override fun addOptionsTo(options: Options): Options = options
        .addOption(Option
            .builder()
            .longOpt("pattern_minimum_library_usage_count")
            .desc("The minimum number of library usages per method.")
            .hasArg()
            .build())

    override fun read(cmd: CommandLine) {
        minLibraryUsageCount =
            cmd.getOptionValue("pattern_minimum_library_usage_count", DEFAULT_MIN_LIBRARY_USAGE_COUNT).toInt()
    }

    /**
     * Creates a pattern filter with a number of filter rules.
     *
     * @param libraryProject the library project
     * @return a pattern filter with a number of filter rules
     */
    fun createPatternFilter(libraryProject: JavaProject) = PatternFilter(
        IncompleteInitPatternFilterRule(),
        LengthPatternFilterRule(),
        EmptyLoopPatternFilterRule(),
        InsufficientLibraryUsageFilter(libraryProject, minLibraryUsageCount)
    )

    private companion object {
        const val DEFAULT_MIN_LIBRARY_USAGE_COUNT = "1"
    }
}

/**
 * Behavior linked to generating tests with EvoSuite from Jimple code.
 */
class JimpleEvoSuiteTestGeneratorOptionSet : OptionSet() {
    private var timeout = 0
    private var enableOutput = false

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
            .desc("The time limit per pattern for the test generator.")
            .type(Int::class.java)
            .hasArg()
            .build())

    override fun read(cmd: CommandLine) {
        timeout = cmd.getOptionValue("test_generator_timeout", DEFAULT_TIMEOUT).toInt()
        enableOutput = cmd.hasOption("test_generator_enable_output")
    }

    /**
     * Creates a test generator to generate tests with EvoSuite from Jimple code.
     *
     * @param outputDir the directory to store the tests in
     * @param libraryProject the library project
     * @return a test generator to generate tests with EvoSuite from Jimple code
     */
    fun createTestGenerator(outputDir: File, libraryProject: JavaProject) =
        TestGenerator(
            outputDirectory = outputDir,
            library = libraryProject,
            timeout = timeout,
            processStandardStream = if (enableOutput) System.out else null,
            processErrorStream = if (enableOutput) System.out else null
        )

    private companion object {
        const val DEFAULT_TIMEOUT = "60"
    }
}

/**
 * Behavior linked to whether the library project is a Maven or JAR project.
 */
class ProjectOptionSet : OptionSet() {
    var projectType = ProjectType.JAVA_MAVEN

    override fun addOptionsTo(options: Options): Options = options
        .addOption(Option
            .builder()
            .longOpt("library_type")
            .desc("The type of library. [${ProjectType.values().joinToString(", ") { it.type }}]")
            .hasArg()
            .build())

    override fun read(cmd: CommandLine) {
        if (cmd.hasOption("library_type")) {
            projectType = ProjectType.fromString(cmd.getOptionValue("library_type"))
                ?: throw IllegalArgumentException("Unknown library project type.")
        }
    }
}

/**
 * The type of the library project.
 */
enum class ProjectType(val type: String) {
    JAVA_MAVEN("javamaven"),
    JAVA_JAR("javajar");

    companion object {
        /**
         * Returns the enum value corresponding to the string [type].
         *
         * If no enum value can be found matching the given string, null is returned.
         *
         * @param type a string describing the library project type
         */
        fun fromString(type: String) =
            ProjectType.values()
                .filter { type == it.type }
                .getOrNull(0)
    }
}
