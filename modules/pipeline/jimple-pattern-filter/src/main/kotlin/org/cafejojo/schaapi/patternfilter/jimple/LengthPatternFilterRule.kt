package org.cafejojo.schaapi.patternfilter.jimple

import org.cafejojo.schaapi.common.Node
import org.cafejojo.schaapi.common.PatternFilterRule

private const val DEFAULT_MINIMUM_PATTERN_LENGTH = 2

/**
 * Filters out patterns that are too short.
 *
 * @property minimumLength the minimum length (inclusive) a pattern should have for it to be retained, which must at
 * least be 1. [DEFAULT_MINIMUM_PATTERN_LENGTH] by default
 */
class LengthPatternFilterRule(private val minimumLength: Int = DEFAULT_MINIMUM_PATTERN_LENGTH) :
    PatternFilterRule {
    override fun retain(pattern: List<Node>): Boolean = pattern.size >= minimumLength
}