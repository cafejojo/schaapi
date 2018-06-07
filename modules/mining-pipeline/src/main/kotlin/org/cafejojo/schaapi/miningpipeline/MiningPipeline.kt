package org.cafejojo.schaapi.miningpipeline

import mu.KLogging
import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.Project
import java.io.File

/**
 * Represents the complete Schaapi pipeline.
 */
class MiningPipeline<SO : SearchOptions, UP : Project, LP : Project, N : Node>(
    private val outputDirectory: File,
    private val projectMiner: ProjectMiner<SO, UP>,
    private val searchOptions: SO,
    private val libraryProjectCompiler: ProjectCompiler<LP>,
    private val userProjectCompiler: ProjectCompiler<UP>,
    private val libraryUsageGraphGenerator: LibraryUsageGraphGenerator<LP, UP, N>,
    private val patternDetector: PatternDetector<N>,
    private val patternFilter: PatternFilter<N>,
    private val testGenerator: TestGenerator<N>
) {
    private companion object : KLogging()

    /**
     * Executes all steps in the pipeline.
     */
    @Suppress("TooGenericExceptionCaught") // In this case it is relevant to catch and log an Exception
    fun run(libraryProject: LP) {
        libraryProjectCompiler.compile(libraryProject)

        logger.info { "Mining has started." }
        logger.info { "Output directory is ${outputDirectory.absolutePath}." }

        try {
            searchOptions
                .also { logger.info { "Started mining projects." } }
                .next(projectMiner::mine)
                .also { logger.info { "Successfully mined ${it.size} projects." } }

                .also { logger.info { "Started compiling ${it.size} projects." } }
                .nextCatchExceptions<CompilationException, UP, UP>(userProjectCompiler::compile)
                .also { logger.info { "Successfully compiled ${it.count()} projects." } }

                .also { logger.info { "Started generating library usage graphs for ${it.count()} projects." } }
                .flatMap { libraryUsageGraphGenerator.generate(libraryProject, it) }
                .also { logger.info { "Successfully generated ${it.size} library usage graphs." } }

                .also { logger.info { "Started finding patterns in ${it.size} library usage graphs." } }
                .next(patternDetector::findPatterns)
                .also { logger.info { "Successfully found ${it.size} patterns." } }

                .also { logger.info { "Started filtering ${it.size} patterns." } }
                .next(patternFilter::filter)
                .also { logger.info { "${it.size} patterns remain after filtering." } }

                .also { logger.info { "Started generating test for ${it.size} usage patterns." } }
                .next(testGenerator::generate)
                .also { logger.info { "Test generation has finished." } }

            logger.info { "Tests have been successfully generated." }
        } catch (e: Exception) {
            logger.error("A critical error occurred during the mining process causing it to be aborted.", e)
        } finally {
            logger.info { "Mining has finished." }
        }
    }

    /**
     * Calls the specified function [map] with `this` value as its argument and returns its result.
     */
    private fun <T, R> T.next(map: (T) -> R): R = map(this)

    /**
     * Calls the specified function [map] on each element in `this` and returns the result as an iterable.
     */
    private fun <T, R> Iterable<T>.next(map: (T) -> R): Iterable<R> = this.map { map(it) }

    /**
     * Calls the specified function [map] on each element in `this` and returns the result as an iterable.
     *
     * Catches exceptions of type [E] and logs these. All other exceptions are thrown.
     */
    @Suppress("TooGenericExceptionCaught", "InstanceOfCheckForException") // This is intended behaviour
    private inline fun <reified E : RuntimeException, T, R : Any>
        Iterable<T>.nextCatchExceptions(map: (T) -> R): Iterable<R> = this.mapNotNull {
        try {
            map(it)
        } catch (e: RuntimeException) {
            if (e is E) {
                logger.warn(e.message, e)
                null
            } else throw e
        }
    }
}
