package org.cafejojo.schaapi.common

/**
 * Decides whether a [Pattern] should be retained.
 */
interface PatternFilterRule {
    /**
     * Determines if [pattern] should be retained.
     *
     * @param pattern a generated pattern
     * @return true if the pattern should be retained
     */
    fun retain(pattern: List<Node>): Boolean
}
