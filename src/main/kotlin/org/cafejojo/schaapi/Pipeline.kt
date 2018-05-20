package org.cafejojo.schaapi

import org.cafejojo.schaapi.common.LibraryUsageGraphGenerator
import org.cafejojo.schaapi.common.PatternDetector
import org.cafejojo.schaapi.common.PatternFilter
import org.cafejojo.schaapi.common.Project
import org.cafejojo.schaapi.common.ProjectCompiler
import org.cafejojo.schaapi.common.TestGenerator

/**
 * Represents the complete Schaapi pipeline.
 */
class Pipeline(
    private val projectCompiler: ProjectCompiler,
    private val libraryUsageGraphGenerator: LibraryUsageGraphGenerator,
    private val patternDetector: PatternDetector,
    private val patternFilter: PatternFilter,
    private val testGenerator: TestGenerator
) {
    /**
     * Executes all steps in the pipeline.
     */
    fun run(project: Project, libraryProject: Project) {
        projectCompiler.compile(libraryProject)

        projectCompiler.compile(project)
            .next { libraryUsageGraphGenerator.generate(libraryProject, it) }
            .next(patternDetector::findPatterns)
            .next(patternFilter::filter)
            .next(testGenerator::generate)
    }
}

/**
 * Calls the specified function [map] with `this` value as its argument and returns its result.
 */
inline infix fun <T, R> T.next(map: (T) -> R): R = map(this)
