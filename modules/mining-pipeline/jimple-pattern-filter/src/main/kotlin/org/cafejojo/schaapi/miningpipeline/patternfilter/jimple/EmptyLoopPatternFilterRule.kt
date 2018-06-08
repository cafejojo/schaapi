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
            .filter { it.statement is GotoStmt }
            .all { node ->
                if (pattern.getOrNull(pattern.indexOf(node) - 1)?.statement === (node.statement as? GotoStmt)?.target)
                    false.also { if (!it) logger.debug { "Empty loop pattern was detected: $pattern" } }
                else
                    true
            }
}
