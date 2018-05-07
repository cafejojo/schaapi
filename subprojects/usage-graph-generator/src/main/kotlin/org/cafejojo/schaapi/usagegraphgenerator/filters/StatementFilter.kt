package org.cafejojo.schaapi.usagegraphgenerator.filters

import soot.Body
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
 * Performs filtering of library-using statements.
 */
class StatementFilter(val body: Body) {
    /**
     * Filters out non library-using statements.
     *
     * @param unit a statement.
     * @return whether or not the statement should be kept.
     */
    fun retain(unit: Unit) = when (unit) {
        is ThrowStmt -> ValueFilter.retain(unit.op)
        is DefinitionStmt -> ValueFilter.retain(unit.rightOp)
        is IfStmt -> true // defer to JumpFilter
        is SwitchStmt -> throw UnsupportedOperationException("Switch statements are not supported at this time") // todo
        is InvokeStmt -> ValueFilter.retain(unit.invokeExpr)
        is ReturnStmt -> true
        is GotoStmt -> true
        is ReturnVoidStmt -> true
        else -> false
    }
}
