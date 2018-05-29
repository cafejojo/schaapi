package org.cafejojo.schaapi.pipeline

import org.cafejojo.schaapi.models.Node

/**
 * Decides whether a [Pattern] should be retained.
 */
interface PatternFilterRule<N : Node> {
    /**
     * Determines if [pattern] should be retained.
     *
     * @param pattern a generated pattern
     * @return true if the pattern should be retained
     */
    fun retain(pattern: List<N>): Boolean
}
