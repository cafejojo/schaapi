package org.cafejojo.schaapi.common

import org.cafejojo.schaapi.common.Node

/**
 * Represents a filter for generated patterns.
 */
interface PatternFilter {
    /**
     * Determines if [pattern] should be retained.
     *
     * @param pattern a generated pattern
     * @return true if the pattern should be retained
     */
    fun retain(pattern: List<Node>): Boolean
}
