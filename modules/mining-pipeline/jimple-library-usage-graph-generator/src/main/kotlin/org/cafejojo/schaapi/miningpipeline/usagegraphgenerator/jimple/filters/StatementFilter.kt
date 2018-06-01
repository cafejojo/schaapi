package org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.filters

import org.cafejojo.schaapi.models.project.JavaProject
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
 *
 * @param project library project
 */
internal class StatementFilter(project: JavaProject) : Filter {
    private val valueFilter = ValueFilter(project)

    override fun apply(body: Body) = body.units.snapshotIterator().forEach { if (!retain(it)) body.units.remove(it) }

    /**
     * Filters out non library-using statements.
     *
     * @param unit a statement.
     * @return whether or not the statement should be kept.
     */
    fun retain(unit: Unit) = when (unit) {
        is ThrowStmt -> valueFilter.retain(unit.op)
        is DefinitionStmt -> valueFilter.retain(unit.rightOp)
        is IfStmt -> true // defer to BranchStatementFilter
        is SwitchStmt -> true // defer to BranchStatementFilter
        is InvokeStmt -> valueFilter.retain(unit.invokeExpr)
        is ReturnStmt -> true
        is GotoStmt -> true
        is ReturnVoidStmt -> true
        else -> false
    }
}
