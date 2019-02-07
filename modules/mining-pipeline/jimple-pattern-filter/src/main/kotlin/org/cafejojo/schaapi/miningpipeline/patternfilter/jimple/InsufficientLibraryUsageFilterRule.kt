package org.cafejojo.schaapi.miningpipeline.patternfilter.jimple

import org.cafejojo.schaapi.miningpipeline.Pattern
import org.cafejojo.schaapi.miningpipeline.PatternFilterRule
import org.cafejojo.schaapi.miningpipeline.patternfilter.jimple.libraryusagefilters.StatementFilter
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimpleNode
import org.cafejojo.schaapi.models.project.JavaProject

/**
 * Filters out patterns that do not have enough calls to the library project.
 *
 * @param libraryProject the project that should be used
 * @property minUseCount the minimum number of usages per pattern to be qualified as a "sufficient user"
 */
class InsufficientLibraryUsageFilterRule(
    private val libraryProject: JavaProject,
    private val minUseCount: Int
) : PatternFilterRule<JimpleNode> {
    private val statementFilter = StatementFilter(libraryProject)

    /**
     * Returns true iff there are at least [minUseCount] nodes in [pattern] that use classes from the library project.
     *
     * @param pattern the pattern to check for library usages
     * @return true iff there are at least [minUseCount] nodes in [pattern] that use classes from the library project
     */
    override fun retain(pattern: Pattern<JimpleNode>) =
        pattern.count { statementFilter.retain(it.statement) } >= minUseCount
}
