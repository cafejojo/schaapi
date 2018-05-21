package org.cafejojo.schaapi.pipeline

/**
 * Filters [Pattern]s based on a set of rules.
 *
 * @property rules the criteria that indicate whether a [Pattern] should be retained
 */
class PatternFilter(private vararg val rules: PatternFilterRule) {
    /**
     * Performs filtering of [Pattern]s based on the [PatternFilter]'s rules.
     *
     * @param patterns the list of patterns to be filtered
     * @return the patterns that were retained by the filters
     */
    fun filter(patterns: List<Pattern>) = patterns.filter { pattern -> rules.all { rule -> rule.retain(pattern) } }
}
