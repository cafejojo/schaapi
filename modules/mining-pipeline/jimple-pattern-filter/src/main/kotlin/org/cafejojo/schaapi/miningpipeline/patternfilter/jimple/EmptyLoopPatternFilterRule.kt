package org.cafejojo.schaapi.miningpipeline.patternfilter.jimple

import mu.KLogging
import org.cafejojo.schaapi.miningpipeline.PatternFilterRule
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimpleNode
import soot.jimple.GotoStmt

/**
 * Filters out patterns containing an empty loop.
 */
class EmptyLoopPatternFilterRule : PatternFilterRule<JimpleNode> {
    private companion object : KLogging()

    override fun retain(pattern: List<JimpleNode>) =
        pattern
            .none { node ->
                pattern.getOrNull(pattern.indexOf(node) - 1)?.let {
                    it.statement === (node.statement as? GotoStmt)?.target
                } ?: false
            }
            .also { if (!it) logger.debug { "Empty loop pattern was detected: $pattern" } }
}
