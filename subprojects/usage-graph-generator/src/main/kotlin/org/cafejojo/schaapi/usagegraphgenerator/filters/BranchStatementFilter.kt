package org.cafejojo.schaapi.usagegraphgenerator.filters

import org.cafejojo.schaapi.common.JavaProject
import soot.Body
import soot.Unit
import soot.Value
import soot.jimple.GotoStmt
import soot.jimple.IfStmt
import soot.jimple.SwitchStmt
import soot.toolkits.graph.BriefUnitGraph
import soot.toolkits.graph.UnitGraph

/**
 * Performs filtering of library-using branch statements.
 *
 * @param project library project
 */
class BranchStatementFilter(project: JavaProject) : Filter {
    private val valueFilter = ValueFilter(project)

    override fun apply(body: Body) {
        var changed = true

        while (changed) {
            changed = false

            body.units.snapshotIterator().asSequence()
                .filter(BranchStatements::isBranchStatement)
                .map { BranchingStatement(body, it) }
                .filter { !retain(it) }
                .forEach {
                    changed = true
                    body.units.remove(it.statement)
                    body.units.removeAll(it.redundantGoToStatements)
                }
        }
    }

    private fun retain(branch: BranchingStatement) =
        branch.nonEmptyBranches || valueFilter.retain(BranchStatements.getConditionValue(branch.statement))
}

private object BranchStatements {
    internal fun isBranchStatement(statement: Unit) = when (statement) {
        is IfStmt -> true
        is SwitchStmt -> true
        else -> false
    }

    internal fun getConditionValue(statement: Unit): Value = when (statement) {
        is IfStmt -> statement.condition
        is SwitchStmt -> statement.key
        else -> throw IllegalStateException("Cannot get value of unsupported statement.")
    }
}

private class BranchingStatement(body: Body, val statement: Unit) {
    val cfg = BriefUnitGraph(body)

    val redundantGoToStatements: List<GotoStmt>

    val nonEmptyBranches: Boolean

    init {
        val end = findBranchStatementEnd()

        val targets = cfg.getSuccsOf(statement)

        if (targets.all { it === end || it is GotoStmt && it.target === end }) {
            redundantGoToStatements = targets.filterIsInstance<GotoStmt>()
            nonEmptyBranches = false
        } else {
            redundantGoToStatements = emptyList()
            nonEmptyBranches = true
        }
    }

    private fun findBranchStatementEnd(): soot.Unit {
        // Return type is fully classified because of false positives by static analysis tools

        val bodiesTillMethodEnd = cfg.getSuccsOf(statement).map { collectSuccessors(cfg, it) }
        val intersectedBodies =
            bodiesTillMethodEnd.fold(bodiesTillMethodEnd[0], { acc, list -> acc.intersect(list).toMutableList() })

        if (intersectedBodies.isEmpty()) throw IllegalStateException("No common end statement found")

        return intersectedBodies[0]
    }

    private fun collectSuccessors(
        cfg: UnitGraph, unit: Unit, collection: MutableList<Unit> = mutableListOf()
    ): MutableList<Unit> {
        collection.add(unit)
        cfg.getSuccsOf(unit).forEach { collectSuccessors(cfg, it, collection) }

        return collection
    }
}
