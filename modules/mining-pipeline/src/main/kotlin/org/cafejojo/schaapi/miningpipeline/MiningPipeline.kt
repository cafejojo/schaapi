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
    private val projectMiner: ProjectMiner<SO, UP>,
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
    fun run(outputDirectory: File, searchOptions: SO, libraryProject: LP) {
        logger.info { "Compiling library project." }
        createProgressBarBuilder("Compiling library project")
            .also { it.setInitialMax(1) }
            .build()
            .use { progressBar ->
                libraryProjectCompiler.compile(libraryProject)
                progressBar.step()
            }
        logger.info { "Compiled library project." }

        logger.info { "Mining has started." }
        logger.info { "Output directory is ${outputDirectory.absolutePath}." }

        val csvWriter = CsvWriter<N>(outputDirectory)

        try {
            searchOptions
                .also { logger.info { "Started mining projects." } }
                .next(projectMiner::mine)
                .also { logger.info { "Successfully mined ${it.count()} projects." } }

                .also { logger.info { "Started compiling ${it.count()} user projects." } }
                .nextMapNotNull(
                    catchWrapper<CompilationException, UP, UP?>(userProjectCompiler::compile, null),
                    "Compiling user projects"
                )
                .also { logger.info { "Successfully compiled ${it.count()} user projects." } }

                .also { logger.info { "Started generating library-usage graphs for ${it.count()} projects." } }
                .nextFlatMap(
                    catchWrapper<RuntimeException, UP, Iterable<N>>(
                        { libraryUsageGraphGenerator.generate(libraryProject, it) },
                        emptyList()
                    ),
                    "Generating library-usage graphs"
                )
                .also { logger.info { "Successfully generated ${it.size} library-usage graphs." } }
                .also { csvWriter.writeGraphSizes(it) }

                .also { logger.info { "Started finding patterns in ${it.size} library-usage graphs." } }
                .next(patternDetector::findPatterns, "Finding patterns")
                .also { logger.info { "Successfully found ${it.count()} patterns." } }
                .also { csvWriter.writePatternLengths(it) }

                .also { logger.info { "Started filtering ${it.count()} patterns." } }
                .next(patternFilter::filter, "Filtering patterns")
                .also { logger.info { "${it.size} patterns remain after filtering." } }
                .also { csvWriter.writeFilteredPatternLengths(it) }

                .also { logger.info { "Started generating tests for ${it.size} usage patterns." } }
                .next(testGenerator::generate)
                .also { logger.info { "Test generation has finished." } }

            logger.info { "Tests have been successfully generated." }
        } catch (e: Exception) {
            logger.error("A critical error occurred during the mining process causing it to be aborted.", e)
        } finally {
            logger.info { "Mining has finished." }
        }
    }

    private fun <T, R> T.next(map: (T) -> R): R = map(this)

    private inline fun <T, R> Iterable<T>.next(map: (Iterable<T>) -> R, message: String): R =
        map(progressBarIterable(this, message) as? Iterable<T> ?: this)

    private inline fun <T, R : Any> Iterable<T>.nextMapNotNull(map: (T) -> R?, message: String): List<R> =
        progressBarIterable(this, message).mapNotNull(map)

    private fun <T, R> Iterable<T>.nextFlatMap(map: (T) -> Iterable<R>, message: String): List<R> =
        progressBarIterable(this, message).flatMap { map(it) }

    @Suppress("TooGenericExceptionCaught", "InstanceOfCheckForException") // This is intended behaviour
    private inline fun <reified E : RuntimeException, T, R>
        catchWrapper(crossinline map: (T) -> R, default: R): (T) -> R = {
        try {
            map(it)
        } catch (e: RuntimeException) {
            if (e is E) logger.warn { e.message } else throw e
            default
        }
    }

    private fun <N> progressBarIterable(iterable: Iterable<N>, taskName: String): Iterable<N> =
        ProgressBar.wrap(iterable, createProgressBarBuilder(taskName))
}

/**
 * Creates a progress bar builder with the default settings for progress bars in the mining pipeline set.
 *
 * @param taskName the name of the progress bar
 */
fun createProgressBarBuilder(taskName: String) =
    ProgressBarBuilder().apply {
        setTaskName(taskName)
        setStyle(ProgressBarStyle.ASCII)
        setPrintStream(System.out)
    }
