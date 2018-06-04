package org.cafejojo.schaapi.miningpipeline.patternfilter.jimple

import mu.KLogging
import org.cafejojo.schaapi.miningpipeline.PatternFilterRule
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimpleNode

private const val DEFAULT_MINIMUM_PATTERN_LENGTH = 2

/**
 * Filters out patterns that are too short.
 *
 * @property minimumLength the minimum length (inclusive) a pattern should have for it to be retained, which must at
 * least be 1. [DEFAULT_MINIMUM_PATTERN_LENGTH] by default
 */
class LengthPatternFilterRule(private val minimumLength: Int = DEFAULT_MINIMUM_PATTERN_LENGTH) :
    PatternFilterRule<JimpleNode> {
    private companion object : KLogging()

    override fun retain(pattern: List<JimpleNode>): Boolean =
        (pattern.size >= minimumLength).also { if (it) logger.debug { "Short pattern was detected: $pattern" } }
}
