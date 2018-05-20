package org.cafejojo.schaapi.common

/**
 * Represents a pattern filter.
 *
 * @property rules rules that indicated whether a pattern should be retained or not
 */
class PatternFilter(private vararg val rules: PatternFilterRule ) {
    /**
     * Performs filtering of patterns based on the gives list of filtering rules.
     *
     * @param patterns list of patterns to be filtered
     * @return list of filtered patterns
     */
    fun filter(patterns: List<Pattern>) = patterns.filter { pattern -> rules.map { it.retain(pattern) }.all { it } }
}
