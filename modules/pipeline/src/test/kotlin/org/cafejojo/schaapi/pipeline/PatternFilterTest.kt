package org.cafejojo.schaapi.pipeline

import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.SimpleNode
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

internal object PatternFilterTest : Spek({
    describe("the pattern filter") {
        it("retains all patterns if no rules given") {
            val patterns = listOf(
                listOf(SimpleNode()),
                listOf(SimpleNode())
            )

            val filteredPatterns = PatternFilter().filter(patterns)

            assertThat(filteredPatterns).isEqualTo(patterns)
        }

        it("retains all patterns that are in accordance with the given rules") {
            val lengthTwoPattern = listOf(SimpleNode(), SimpleNode())
            val patterns = listOf(
                listOf(SimpleNode()),
                lengthTwoPattern,
                emptyList()
            )

            val filteredPatterns = PatternFilter(
                NoEmptyPatternFilterRule(),
                NoLengthOnePatternFilterRule()
            ).filter(patterns)

            assertThat(filteredPatterns).containsExactly(lengthTwoPattern)
        }
    }
})

private class NoLengthOnePatternFilterRule : PatternFilterRule {
    override fun retain(pattern: List<Node>) = pattern.size != 1
}

private class NoEmptyPatternFilterRule : PatternFilterRule {
    override fun retain(pattern: List<Node>) = pattern.isNotEmpty()
}
