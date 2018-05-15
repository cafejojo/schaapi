package org.cafejojo.schaapi.patternfilter

import org.cafejojo.schaapi.common.Node
import org.cafejojo.schaapi.common.PatternFilter
import org.cafejojo.schaapi.usagegraphgenerator.SootNode
import soot.jimple.InvokeStmt
import soot.jimple.internal.JSpecialInvokeExpr

/**
 * Filters out patterns that start with `<init>` invokes but do not have a new statement.
 */
object IncompleteInitPatternFilter : PatternFilter {
    override fun retain(pattern: List<Node>): Boolean {
        if (pattern.isEmpty()) return true

        val firstStatement = pattern[0] as? SootNode ?: return true
        val firstUnit = firstStatement.unit as? InvokeStmt ?: return true

        return firstUnit.invokeExpr !is JSpecialInvokeExpr || firstUnit.invokeExpr.method.name != "<init>"
    }
}
