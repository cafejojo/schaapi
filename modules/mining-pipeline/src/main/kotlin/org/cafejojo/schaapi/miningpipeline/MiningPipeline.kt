package org.cafejojo.schaapi.miningpipeline

import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.Project

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
    /**
     * Executes all steps in the pipeline.
     */
    fun run(libraryProject: LP) {
        libraryProjectCompiler.compile(libraryProject)

        searchOptions
            .next(projectMiner::mine)
            .next(userProjectCompiler::compile)
            .flatMap { libraryUsageGraphGenerator.generate(libraryProject, it) }
            .next(patternDetector::findPatterns)
            .next(patternFilter::filter)
            .next(testGenerator::generate)
    }
}

/**
 * Calls the specified function [map] with `this` value as its argument and returns its result.
 */
fun <T, R> T.next(map: (T) -> R): R = map(this)

/**
 * Calls the specified function [map] on each element in `this` and returns the result as an iterable.
 */
fun <T, R> Iterable<T>.next(map: (T) -> R): Iterable<R> = this.map { map(it) }