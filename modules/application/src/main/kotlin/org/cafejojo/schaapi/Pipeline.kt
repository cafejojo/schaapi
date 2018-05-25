package org.cafejojo.schaapi

import org.cafejojo.schaapi.models.Project
import org.cafejojo.schaapi.pipeline.LibraryUsageGraphGenerator
import org.cafejojo.schaapi.pipeline.PatternDetector
import org.cafejojo.schaapi.pipeline.PatternFilter
import org.cafejojo.schaapi.pipeline.ProjectCompiler
import org.cafejojo.schaapi.pipeline.TestGenerator

/**
 * Represents the complete Schaapi pipeline.
 */
class Pipeline(
    private val libraryProjectCompiler: ProjectCompiler,
    private val userProjectCompiler: ProjectCompiler,
    private val libraryUsageGraphGenerator: LibraryUsageGraphGenerator,
    private val patternDetector: PatternDetector,
    private val patternFilter: PatternFilter,
    private val testGenerator: TestGenerator
) {
    /**
     * Executes all steps in the pipeline.
     */
    fun run(projects: List<Project>, libraryProject: Project) {
        libraryProjectCompiler.compile(libraryProject)

        projects.map { userProjectCompiler.compile(it) }
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
