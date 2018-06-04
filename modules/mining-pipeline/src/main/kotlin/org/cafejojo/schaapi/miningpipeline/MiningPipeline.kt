package org.cafejojo.schaapi.miningpipeline

import mu.KLogging
import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.Project
import java.text.SimpleDateFormat
import java.util.Calendar

/**
 * Represents the complete Schaapi pipeline.
 */
class MiningPipeline<SO : SearchOptions, UP : Project, LP : Project, N : Node>(
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

    private val report = StringBuilder()

    /**
     * Executes all steps in the pipeline.
     */
    fun run(libraryProject: LP): String {
        libraryProjectCompiler.compile(libraryProject)

        logAndAppendToReport("Mining has started")

        try {
            searchOptions
                .also { logAndAppendToReport("Start mining projects.") }
                .next(projectMiner::mine)
                .also { logAndAppendToReport("Successfully mined ${it.size} projects.") }

                .also { logAndAppendToReport("Start compiling ${it.size} projects.") }
                .nextCatchExceptions<CompilationException, UP, UP>(userProjectCompiler::compile)
                .also { logAndAppendToReport("Successfully compiled ${it.count()} projects.") }

                .also { logAndAppendToReport("Start generating library usage graphs for ${it.count()} projects.") }
                .flatMap { libraryUsageGraphGenerator.generate(libraryProject, it) }
                .also { logAndAppendToReport("Successfully generated library usage graph for ${it.count()} projects.") }

                .also { logAndAppendToReport("Start finding patterns in ${it.size} library usage graphs.") }
                .next(patternDetector::findPatterns)
                .also { logAndAppendToReport("Successfully found ${it.size} patterns.") }

                .also { logAndAppendToReport("Start filtering ${it.size} patterns.") }
                .next(patternFilter::filter)
                .also { logAndAppendToReport("${it.size} patterns remain after filtering.") }

                .also { logAndAppendToReport("Start generating test for ${it.size} usage patterns.") }
                .next(testGenerator::generate)
                .also { logAndAppendToReport("Test generation has finished.") }

            logger.info { "Tests have been successfully generated." }
        } catch (e: IllegalArgumentException) {
            logger.error("A critical error occurred during the mining process causing it to be aborted.", e)
            report.append("A critical error occurred during the mining process causing it to be aborted.", e)
        } catch (e: IllegalStateException) {
            logger.error("A critical error occurred during the mining process causing it to be aborted.", e)
            report.append("A critical error occurred during the mining process causing it to be aborted.", e)
        } finally {
            logger.info { "Mining has finished." }
        }

        return report.toString()
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

    private fun logAndAppendToReport(message: String) {
        val date = SimpleDateFormat("yyyy-MM-dd-HH-mm").format(Calendar.getInstance().time)
        report.appendln("$date: $message")
        logger.info { message }
    }
}
