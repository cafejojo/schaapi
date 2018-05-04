package org.cafejojo.schaapi.usagegraphgenerator.filters

import soot.Unit
import soot.jimple.DefinitionStmt
import soot.jimple.GotoStmt
import soot.jimple.IfStmt
import soot.jimple.InvokeStmt
import soot.jimple.ReturnStmt
import soot.jimple.ReturnVoidStmt
import soot.jimple.SwitchStmt
import soot.jimple.ThrowStmt

/**
 * Performs filtering of library using statements.
 */
object StatementFilter {
    /**
     * Filters out non library using statements.
     *
     * @param unit a statement.
     * @return whether or not the statement should be kept.
     */
    fun retain(unit: Unit) = when (unit) {
        is ThrowStmt -> ValueFilter.retain(unit.op)
        is DefinitionStmt -> ValueFilter.retain(unit.rightOp)
        is IfStmt -> throw UnsupportedOperationException("If statements are not supported at this time") // todo
        is SwitchStmt -> throw UnsupportedOperationException("Switch statements are not supported at this time") // todo
        is InvokeStmt -> ValueFilter.retain(unit.invokeExpr)
        is ReturnStmt -> ValueFilter.retain(unit.op)
        is GotoStmt -> true
        is ReturnVoidStmt -> true
        else -> false
    }
}
