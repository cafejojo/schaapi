package org.cafejojo.schaapi.pipeline.patternfilter.jimple

import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimpleNode
import org.cafejojo.schaapi.pipeline.PatternFilterRule
import soot.jimple.InvokeStmt
import soot.jimple.internal.JSpecialInvokeExpr

/**
 * Filters out patterns that start with `<init>` invokes but do not have a new statement.
 */
class IncompleteInitPatternFilterRule : PatternFilterRule<JimpleNode> {
    override fun retain(pattern: List<JimpleNode>): Boolean {
        if (pattern.isEmpty()) return true

        val firstStatement = pattern[0]
        val firstUnit = firstStatement.statement as? InvokeStmt ?: return true

        return firstUnit.invokeExpr !is JSpecialInvokeExpr || firstUnit.invokeExpr.method.name != "<init>"
    }
}
