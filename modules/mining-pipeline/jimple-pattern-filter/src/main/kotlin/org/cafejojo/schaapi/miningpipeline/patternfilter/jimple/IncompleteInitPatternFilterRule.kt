package org.cafejojo.schaapi.miningpipeline.patternfilter.jimple

import mu.KLogging
import org.cafejojo.schaapi.miningpipeline.PatternFilterRule
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimpleNode
import soot.jimple.InvokeStmt
import soot.jimple.internal.JSpecialInvokeExpr

/**
 * Filters out patterns that start with `<init>` invokes but do not have a new statement.
 */
class IncompleteInitPatternFilterRule : PatternFilterRule<JimpleNode> {
    private companion object : KLogging()

    override fun retain(pattern: List<JimpleNode>): Boolean {
        if (pattern.isEmpty()) return true

        val firstStatement = pattern[0]
        val firstUnit = firstStatement.statement as? InvokeStmt ?: return true

        return (firstUnit.invokeExpr !is JSpecialInvokeExpr || firstUnit.invokeExpr.method.name != "<init>")
            .also { if (it) logger.debug { "Incomplete init pattern was detected: $pattern" } }
    }
}
