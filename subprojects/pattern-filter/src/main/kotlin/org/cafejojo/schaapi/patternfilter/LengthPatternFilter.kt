package org.cafejojo.schaapi.patternfilter

import org.cafejojo.schaapi.common.Node
import org.cafejojo.schaapi.common.PatternFilter

private const val DEFAULT_MINIMUM_PATTERN_LENGTH = 2

/**
 * Filters out patterns that are too short.
 *
 * @property minimumLength the minimum length (inclusive) a pattern should have for it to be retained, which must at
 * least be 1. [DEFAULT_MINIMUM_PATTERN_LENGTH] by default
 */
class LengthPatternFilter(private val minimumLength: Int = DEFAULT_MINIMUM_PATTERN_LENGTH) : PatternFilter {
    init {
        require(minimumLength > 0) { "The minimum pattern length must be 1 or greater, was $minimumLength." }
    }

    override fun retain(pattern: List<Node>): Boolean = pattern.size >= minimumLength
}
