package org.cafejojo.schaapi

import mu.KLogging
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.cafejojo.schaapi.miningpipeline.MiningPipeline
import org.cafejojo.schaapi.miningpipeline.projectcompiler.javajar.JavaJarProjectCompiler
import org.cafejojo.schaapi.miningpipeline.projectcompiler.javamaven.JavaMavenProjectCompiler
import org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.JimpleLibraryUsageGraphGenerator
import org.cafejojo.schaapi.models.project.JavaJarProject
import org.cafejojo.schaapi.models.project.JavaMavenProject

/**
 * Mines a directory for user projects and generates tests based on these projects.
 */
internal class DirectoryMiningCommandLineInterface : CommandLineInterface() {
    private companion object : KLogging()

    private val directoryMinerCli = DirectoryMiningCliOptionSet()
    private val maven = MavenOptionSet()
    private val directory = DirectoryMavenMinerOptionSet(maven)
    private val library = ProjectOptionSet()
    private val user = UserOptionSet()
    private val patternDetector = CCSpanPatternDetectorOptionSet()
    private val patternFilter = PatternFilterOptionSet()
    private val testGenerator = JimpleEvoSuiteTestGeneratorOptionSet()

    init {
        optionSets.add(directoryMinerCli)
        optionSets.add(maven)
        optionSets.add(directory)
        optionSets.add(library)
        optionSets.add(user)
        optionSets.add(patternDetector)
        optionSets.add(patternFilter)
        optionSets.add(testGenerator)
    }

    override fun run(cmd: CommandLine) {
        val jimpleLibraryUsageGraphGenerator = JimpleLibraryUsageGraphGenerator()
        maven.install()

        when (library.projectType) {
            ProjectType.JAVA_MAVEN -> {
                val libraryProject = JavaMavenProject(libraryDir, maven.dir)

                MiningPipeline(
                    projectMiner = directory.createMiner(),
                    libraryProjectCompiler = JavaMavenProjectCompiler(displayOutput = true),
                    userProjectCompiler = JavaMavenProjectCompiler(
                        skipCompile = directoryMinerCli.skipUserCompile,
                        timeout = user.timeout
                    ),
                    libraryUsageGraphGenerator = jimpleLibraryUsageGraphGenerator,
                    patternDetector = patternDetector.createPatternDetector(),
                    patternFilter = patternFilter.createPatternFilter(libraryProject),
                    testGenerator = testGenerator.createTestGenerator(outputDir, libraryProject)
                ).run(
                    outputDirectory = outputDir,
                    searchOptions = directory.createOptions(),
                    libraryProject = libraryProject
                )
            }
            ProjectType.JAVA_JAR -> {
                val libraryProject = JavaJarProject(libraryDir)

                MiningPipeline(
                    projectMiner = directory.createMiner(),
                    libraryProjectCompiler = JavaJarProjectCompiler(),
                    userProjectCompiler = JavaMavenProjectCompiler(
                        skipCompile = directoryMinerCli.skipUserCompile,
                        timeout = user.timeout
                    ),
                    libraryUsageGraphGenerator = jimpleLibraryUsageGraphGenerator,
                    patternDetector = patternDetector.createPatternDetector(),
                    patternFilter = patternFilter.createPatternFilter(libraryProject),
                    testGenerator = testGenerator.createTestGenerator(outputDir, libraryProject)
                ).run(
                    outputDirectory = outputDir,
                    searchOptions = directory.createOptions(),
                    libraryProject = libraryProject
                )
            }
        }

        logger.info { "Found ${jimpleLibraryUsageGraphGenerator.lugStatistics.concreteMethods} concrete methods." }
        logger.info { "Found ${jimpleLibraryUsageGraphGenerator.lugStatistics.allStatements} statements." }
        logger.info { "Found ${jimpleLibraryUsageGraphGenerator.lugStatistics.validStatements} valid statements." }
    }
}

/**
 * Behavior linked to [DirectoryMiningCommandLineInterface].
 */
private class DirectoryMiningCliOptionSet : OptionSet() {
    var skipUserCompile = false

    override fun addOptionsTo(options: Options): Options =
        options
            .addOption(Option
                .builder()
                .longOpt("skip_user_compile")
                .desc("Skip compilation of user projects.")
                .build())

    override fun read(cmd: CommandLine) {
        skipUserCompile = cmd.hasOption("skip_user_compile")
    }
}
