package org.cafejojo.schaapi.patternfilter.jimple

import org.cafejojo.schaapi.common.Node
import org.cafejojo.schaapi.common.PatternFilter
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimpleNode
import soot.jimple.InvokeStmt
import soot.jimple.internal.JSpecialInvokeExpr

/**
 * Filters out patterns that start with `<init>` invokes but do not have a new statement.
 */
class IncompleteInitPatternFilter : PatternFilter {
    override fun retain(pattern: List<Node>): Boolean {
        if (pattern.isEmpty()) return true

        val firstStatement = pattern[0] as? JimpleNode ?: return true
        val firstUnit = firstStatement.statement as? InvokeStmt ?: return true

        return firstUnit.invokeExpr !is JSpecialInvokeExpr || firstUnit.invokeExpr.method.name != "<init>"
    }
}
