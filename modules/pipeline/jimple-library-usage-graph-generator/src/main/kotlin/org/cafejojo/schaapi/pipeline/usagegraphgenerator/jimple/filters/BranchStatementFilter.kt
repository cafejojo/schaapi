package org.cafejojo.schaapi.pipeline.usagegraphgenerator.jimple.filters

import org.cafejojo.schaapi.models.project.java.JavaProject
import org.cafejojo.schaapi.pipeline.usagegraphgenerator.jimple.filters.BranchStatement.Companion.isBranchStatement
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
                .filter(::isBranchStatement)
                .map { BranchStatement(body, it) }
                .filter { !retain(it) }
                .forEach {
                    changed = true
                    it.remove()
                }
        }
    }

    private fun retain(branch: BranchStatement) =
        branch.hasNonEmptyBranches || valueFilter.retain(BranchStatement.getConditionValue(branch.statement))
}

private class BranchStatement(private val body: Body, val statement: Unit) {
    companion object {
        internal fun isBranchStatement(statement: Unit) = when (statement) {
            is IfStmt -> true
            is SwitchStmt -> true
            else -> false
        }

        internal fun getConditionValue(statement: Unit): Value = when (statement) {
            is IfStmt -> statement.condition
            is SwitchStmt -> statement.key
            else -> throw IllegalArgumentException("Cannot get value of unsupported statement.")
        }
    }

    val cfg = BriefUnitGraph(body)

    var redundantGoToStatements: List<GotoStmt> = emptyList()

    var hasNonEmptyBranches: Boolean = true

    init {
        findBranchStatementEnd()?.let { end ->
            val branchTargets = cfg.getSuccsOf(statement)

            if (branchTargets.all { it === end || it is GotoStmt && it.target === end }) {
                redundantGoToStatements = branchTargets.filterIsInstance<GotoStmt>()
                hasNonEmptyBranches = false
            }
        }
    }

    internal fun remove() {
        body.units.remove(statement)
        body.units.removeAll(redundantGoToStatements)
    }

    private fun findBranchStatementEnd(): soot.Unit? {
        // Return type is fully classified because of false positives by static analysis tools

        val bodiesTillMethodEnd = cfg.getSuccsOf(statement).map { collectSuccessors(cfg, it) }
        val intersectedBodies =
            bodiesTillMethodEnd.fold(bodiesTillMethodEnd[0], { acc, list -> acc.intersect(list).toMutableList() })

        if (intersectedBodies.isEmpty()) return null

        return intersectedBodies[0]
    }

    private fun collectSuccessors(
        cfg: UnitGraph, unit: Unit, collection: MutableList<Unit> = mutableListOf()
    ): MutableList<Unit> {
        if (collection.contains(unit)) return collection

        collection.add(unit)
        cfg.getSuccsOf(unit).forEach { collectSuccessors(cfg, it, collection) }

        return collection
    }
}
