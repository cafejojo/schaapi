package org.cafejojo.schaapi.patternfilter

import org.cafejojo.schaapi.common.Node
import org.cafejojo.schaapi.common.PatternFilter

private const val DEFAULT_MINIMUM_PATTERN_LENGTH = 2

/**
 * Filters out patterns that are too short.
 *
 * @property minimumLength the minimum length (inclusive) a pattern should have for it to be retained.
 * [DEFAULT_MINIMUM_PATTERN_LENGTH] by default
 */
class LengthPatternFilter(private val minimumLength: Int = DEFAULT_MINIMUM_PATTERN_LENGTH) : PatternFilter {
    init {
        if (minimumLength < 1) throw TooSmallMinimumPatternLengthException(minimumLength)
    }

    override fun retain(pattern: List<Node>): Boolean = pattern.size >= minimumLength
}

/**
 * A [RuntimeException] occurring when the passed minimum length is less than 1.
 */
class TooSmallMinimumPatternLengthException(length: Int) :
    RuntimeException("A minimum pattern length must be 1 or greater, was $length.")
