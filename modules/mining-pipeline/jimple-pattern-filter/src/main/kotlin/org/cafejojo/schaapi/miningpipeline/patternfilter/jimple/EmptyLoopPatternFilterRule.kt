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
        pattern.none { node ->
            val statement = node.statement

            if (statement is GotoStmt) {
                val previousStatements = pattern.takeWhile { it.statement !== statement }

                if (previousStatements.isNotEmpty() && statement.target === previousStatements.last().statement) {
                    return@none true
                        .also { if (!it) logger.debug { "Empty loop pattern was detected: $pattern" } }
                }
            }
            false
        }
}
