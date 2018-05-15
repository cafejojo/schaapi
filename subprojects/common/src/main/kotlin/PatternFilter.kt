import org.cafejojo.schaapi.common.Node

/**
 * Represents a filter for generated patterns.
 */
interface PatternFilter {
    /**
     * Determines if a generated [pattern] should be retained.
     *
     * @param pattern a generated pattern
     * @return whether the pattern should be retained
     */
    fun retain(pattern: List<Node>): Boolean
}
