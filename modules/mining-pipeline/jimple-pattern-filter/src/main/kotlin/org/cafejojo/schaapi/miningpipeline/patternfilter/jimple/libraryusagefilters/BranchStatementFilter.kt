package org.cafejojo.schaapi.miningpipeline.patternfilter.jimple.libraryusagefilters

import org.cafejojo.schaapi.models.project.JavaProject
import soot.Body
import soot.Unit
import soot.Value
import soot.jimple.GotoStmt
import soot.jimple.IfStmt
import soot.jimple.ReturnStmt
import soot.jimple.ReturnVoidStmt
import soot.jimple.SwitchStmt
import soot.jimple.ThrowStmt
import soot.toolkits.graph.BriefUnitGraph
import soot.toolkits.graph.UnitGraph

/**
 * Performs filtering of library-using branch statements.
 *
 * @param project library project
 */
class BranchStatementFilter(project: JavaProject) : Filter {
    internal val valueFilter = ValueFilter(project)

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
        branch.hasNonEmptyBranches || valueFilter.retain(getConditionValue(branch.statement))

    private companion object {
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

    private inner class BranchStatement(private val body: Body, val statement: Unit) {
        val cfg = BriefUnitGraph(body)

        var redundantStatements: List<Unit> = emptyList()

        var hasNonEmptyBranches: Boolean = true

        init {
            findBranchStatementEnd()?.let { end ->
                val branchTargets = cfg.getSuccsOf(statement)

                if (branchTargets.all { it === end || it is GotoStmt && it.target === end }) {
                    redundantStatements = branchTargets.filterIsInstance<GotoStmt>()
                    hasNonEmptyBranches = false
                }
            }

            cfg.getSuccsOf(statement).let { branchTargets ->
                if (branchesContainOnlyReturnOrThrowStatements(branchTargets)) {
                    redundantStatements = branchTargets
                    hasNonEmptyBranches = false
                }
            }
        }

        internal fun remove() {
            body.units.remove(statement)
            body.units.removeAll(redundantStatements)
        }

        private fun findBranchStatementEnd(): soot.Unit? {
            // Return type is fully classified because of false positives by static analysis tools

            val bodiesUntilMethodEnd = cfg.getSuccsOf(statement).map { collectSuccessors(cfg, it) }
            val intersectedBodies =
                bodiesUntilMethodEnd.fold(bodiesUntilMethodEnd[0], { acc, list -> acc.intersect(list).toMutableList() })

            if (intersectedBodies.isEmpty()) return null

            return intersectedBodies[0]
        }

        private fun branchesContainOnlyReturnOrThrowStatements(branchTargets: List<Unit>) = branchTargets.all {
            cfg.getSuccsOf(it).isEmpty() && when (it) {
                is ReturnStmt -> !valueFilter.retain(it.op)
                is ReturnVoidStmt -> true
                is ThrowStmt -> !valueFilter.retain(it.op)
                else -> false
            }
        }

        private fun collectSuccessors(
            cfg: UnitGraph,
            unit: Unit,
            collection: MutableList<Unit> = mutableListOf()
        ): MutableList<Unit> {
            if (collection.contains(unit)) return collection

            collection.add(unit)
            cfg.getSuccsOf(unit).forEach { collectSuccessors(cfg, it, collection) }

            return collection
        }
    }
}
