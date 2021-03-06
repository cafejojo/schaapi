package org.cafejojo.schaapi

import mu.KLogging
import org.apache.commons.cli.CommandLine
import org.cafejojo.schaapi.miningpipeline.MiningPipeline
import org.cafejojo.schaapi.miningpipeline.projectcompiler.javajar.JavaJarProjectCompiler
import org.cafejojo.schaapi.miningpipeline.projectcompiler.javamaven.JavaMavenProjectCompiler
import org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.JimpleLibraryUsageGraphGenerator
import org.cafejojo.schaapi.models.project.JavaJarProject
import org.cafejojo.schaapi.models.project.JavaMavenProject

/**
 * Mines GitHub for user projects and generates tests based on these projects.
 */
internal class GitHubMiningCommandLineInterface : CommandLineInterface() {
    private companion object : KLogging()

    private val maven = MavenOptionSet()
    private val gitHub = GitHubMavenMinerOptionSet(maven)
    private val library = LibraryOptionSet()
    private val user = UserOptionSet()
    private val patternDetector = CCSpanPatternDetectorOptionSet()
    private val patternFilter = PatternFilterOptionSet()
    private val testGenerator = JimpleEvoSuiteTestGeneratorOptionSet()

    init {
        optionSets.add(maven)
        optionSets.add(gitHub)
        optionSets.add(library)
        optionSets.add(user)
        optionSets.add(patternFilter)
        optionSets.add(patternDetector)
        optionSets.add(testGenerator)
    }

    override fun run(cmd: CommandLine) {
        val jimpleLibraryUsageGraphGenerator = JimpleLibraryUsageGraphGenerator()
        maven.install()

        when (library.projectType) {
            ProjectType.JAVA_MAVEN -> {
                val libraryProject = JavaMavenProject(libraryDir, maven.dir)

                MiningPipeline(
                    projectMiner = gitHub.createMiner(outputDir),
                    libraryProjectCompiler = JavaMavenProjectCompiler(true),
                    userProjectCompiler = JavaMavenProjectCompiler(timeout = user.timeout),
                    libraryUsageGraphGenerator = jimpleLibraryUsageGraphGenerator,
                    patternDetector = patternDetector.createPatternDetector(),
                    patternFilter = patternFilter.createPatternFilter(libraryProject),
                    testGenerator = testGenerator.createTestGenerator(outputDir, libraryProject)
                ).run(
                    outputDirectory = outputDir,
                    searchOptions = gitHub.createOptions(),
                    libraryProject = libraryProject
                )
            }
            ProjectType.JAVA_JAR -> {
                val libraryProject = JavaJarProject(libraryDir)

                MiningPipeline(
                    projectMiner = gitHub.createMiner(outputDir),
                    libraryProjectCompiler = JavaJarProjectCompiler(),
                    userProjectCompiler = JavaMavenProjectCompiler(timeout = user.timeout),
                    libraryUsageGraphGenerator = jimpleLibraryUsageGraphGenerator,
                    patternDetector = patternDetector.createPatternDetector(),
                    patternFilter = patternFilter.createPatternFilter(libraryProject),
                    testGenerator = testGenerator.createTestGenerator(outputDir, libraryProject)
                ).run(
                    outputDirectory = outputDir,
                    searchOptions = gitHub.createOptions(),
                    libraryProject = libraryProject
                )
            }
        }

        logger.info { "Found ${jimpleLibraryUsageGraphGenerator.lugStatistics.concreteMethods} concrete methods." }
        logger.info { "Found ${jimpleLibraryUsageGraphGenerator.lugStatistics.allStatements} statements." }
        logger.info { "Found ${jimpleLibraryUsageGraphGenerator.lugStatistics.validStatements} valid statements." }
    }
}
