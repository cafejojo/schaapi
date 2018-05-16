package org.cafejojo.schaapi.patternfilter

import org.cafejojo.schaapi.common.Node
import org.cafejojo.schaapi.common.PatternFilter

private const val DEFAULT_MINIMUM_PATTERN_LENGTH = 2

/**
 * Filters out patterns that start with `<init>` invokes but do not have a new statement.
 *
 * @property minimumLength the minimum length a pattern should have for it to be retained.
 * [DEFAULT_MINIMUM_PATTERN_LENGTH] by default.
 */
class LengthPatternFilter(private val minimumLength: Int = DEFAULT_MINIMUM_PATTERN_LENGTH) : PatternFilter {
    override fun retain(pattern: List<Node>): Boolean = pattern.size >= minimumLength
}
