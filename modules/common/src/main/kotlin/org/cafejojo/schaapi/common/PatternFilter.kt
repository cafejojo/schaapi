package org.cafejojo.schaapi.common

/**
 * Represents a pattern filter.
 *
 * @property rules rules that indicate whether a pattern should be retained or not
 */
class PatternFilter(private vararg val rules: PatternFilterRule) {
    /**
     * Performs filtering of patterns based on the given list of filtering rules.
     *
     * @param patterns list of patterns to be filtered
     * @return list of filtered patterns
     */
    fun filter(patterns: List<Pattern>) = patterns.filter { pattern -> rules.all { rule -> rule.retain(pattern) } }
}
