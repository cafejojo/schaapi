package org.cafejojo.schaapi.miningpipeline

import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarBuilder
import me.tongfei.progressbar.ProgressBarStyle
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
    private val libraryProject: LP,
    private val libraryProjectCompiler: ProjectCompiler<LP>,
    private val userProjectCompiler: ProjectCompiler<UP>,
    private val libraryUsageGraphGenerator: LibraryUsageGraphGenerator<LP, UP, N>,
    private val patternDetector: PatternDetector<N>,
    private val patternFilter: PatternFilter<N>,
    private val testGenerator: TestGenerator<N>
) {
    private companion object : KLogging() {
        private inline fun <reified T : Iterable<*>, R> T.next(map: (T) -> R, message: String): R =
            map(progressBarIterable(this, message) as? T ?: this)

        /**
         * Calls the specified function [map] on each element in `this` and returns the result as an iterable.
         *
         * Catches exceptions of type [E] and logs these. All other exceptions are thrown.
         */
        @Suppress("TooGenericExceptionCaught", "InstanceOfCheckForException") // This is intended behaviour
        private inline fun <reified E : RuntimeException, T, R : Any>
            Iterable<T>.next(map: (T) -> R, message: String): Iterable<R> =
            progressBarIterable(this, message).mapNotNull {
                try {
                    map(it)
                } catch (e: RuntimeException) {
                    if (e is E) logger.warn { e.message } else throw e
                    null
                }
            }

        /**
         * Calls the specified function [map] on each element in `this` and returns the result as an flat mapped
         * iterable.
         *
         * Catches exceptions of type [E] and logs these. All other exceptions are thrown.
         */
        @Suppress("TooGenericExceptionCaught", "InstanceOfCheckForException") // This is intended behaviour
        private inline fun <reified E : Exception, P, Q, T : Iterable<P>>
            T.nextFlatMap(map: (P) -> Iterable<Q>, message: String): Iterable<Q> =
            progressBarIterable(this, message).mapNotNull {
                try {
                    map(it)
                } catch (e: RuntimeException) {
                    if (e is E) logger.warn { e.message } else throw e
                    null
                }
            }.flatten()

        private fun <N> progressBarIterable(iterable: Iterable<N>, taskName: String): Iterable<N> =
            ProgressBar.wrap(iterable, ProgressBarBuilder().apply {
                setTaskName(taskName)
                setStyle(ProgressBarStyle.ASCII)
            })
    }

    private val csvWriter = CsvWriter<N>(outputDirectory)

    /**
     * Executes all steps in the pipeline.
     */
    @Suppress("TooGenericExceptionCaught") // In this case it is relevant to catch and log an Exception
    fun run() = try {
        compileLibraryUsageProject()

        logger.info { "Mining has started." }
        logger.info { "Output directory is ${outputDirectory.absolutePath}." }

        mineProjects()
            .compileUserProjects()
            .generateLibraryUsageGraphs()
            .findPatterns()
            .filterPatterns()
            .generateTests()
    } catch (e: Exception) {
        logger.error("A critical error occurred during the mining process causing it to be aborted.", e)
    } finally {
        logger.info { "Mining has finished." }
    }

    private fun compileLibraryUsageProject() {
        logger.info { "Compiling library project." }
        ProgressBar("Compile library Project", 1, ProgressBarStyle.ASCII).use { progressBar ->
            libraryProjectCompiler.compile(libraryProject)
            progressBar.step()
        }
        logger.info { "Compiled library project." }
    }

    private fun mineProjects(): Iterable<UP> {
        logger.info { "Started mining projects." }
        return projectMiner.mine(searchOptions).also { logger.info { "Successfully mined ${it.count()} projects." } }
    }

    private fun Iterable<UP>.compileUserProjects(): Iterable<UP> {
        logger.info { "Started compiling ${this.count()} projects." }
        return this.next<CompilationException, UP, UP>(userProjectCompiler::compile, "Compiling Projects")
            .also { logger.info { "Successfully compiled ${it.count()} projects." } }
    }

    private fun Iterable<UP>.generateLibraryUsageGraphs(): Iterable<N> {
        logger.info { "Started generating library usage graphs for ${this.count()} projects." }
        val lugs = this.nextFlatMap<LibraryUsageGraphGenerationException, UP, N, Iterable<UP>>(
            { libraryUsageGraphGenerator.generate(libraryProject, it) }, "Generating library-usage graphs")

        logger.info { "Successfully generated ${lugs.count()} library usage graphs." }
        csvWriter.writeGraphSizes(lugs)

        return lugs
    }

    private fun Iterable<N>.findPatterns(): Iterable<Pattern<N>> {
        logger.info { "Started finding patterns in ${this.count()} library usage graphs." }
        val patterns = this.next(patternDetector::findPatterns, "Finding patterns")
        logger.info { "Successfully found ${patterns.count()} patterns." }
        csvWriter.writePatternLengths(patterns)

        return patterns
    }

    private fun Iterable<Pattern<N>>.filterPatterns(): Iterable<Pattern<N>> {
        logger.info { "Started filtering ${this.count()} patterns." }
        val filtered = this.next(patternFilter::filter, "Filtering Patterns")
        logger.info { "${filtered.size} patterns remain after filtering." }
        csvWriter.writeFilteredPatternLengths(filtered)

        return filtered
    }

    private fun Iterable<Pattern<N>>.generateTests(): File {
        ProgressBar("Generate tests for filtered patterns", this.count().toLong(), ProgressBarStyle.ASCII)
            .use { progressBar ->
                logger.info { "Started generating test for ${this.count()} usage patterns." }
                val testFile = testGenerator.generate(this)
                logger.info { "Test generation has finished." }
                progressBar.stepTo(progressBar.max)

                return testFile
            }
    }
}
