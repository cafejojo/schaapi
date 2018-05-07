package org.cafejojo.schaapi.usagegraphgenerator.filters

import soot.Body
import soot.Unit
import soot.jimple.GotoStmt
import soot.jimple.IfStmt

class JumpFilter(private val body: Body) {
    private val toDelete = HashSet<Unit>()

    fun apply() {
        var changed = true

        while (changed) {
            changed = false

            body.units.snapshotIterator().forEach {
                if (it is IfStmt && !retain(it)) {
                    changed = true
                    body.units.remove(it)
                    toDelete.forEach {
                        body.units.remove(it)
                        toDelete.remove(it)
                    }
                }
            }
        }
    }

    private fun retain(jump: IfStmt) = hasNonEmptyBranches(body, jump) || ValueFilter.retain(jump.condition)

    private fun hasNonEmptyBranches(body: Body, jump: IfStmt): Boolean {
        val trueBranchWithGoto = body.units.dropWhile { it !== jump }.drop(1).takeWhile { it !== jump.target }

        val ifEnd: Unit = trueBranchWithGoto.findLast { it is GotoStmt }.let {
            if (it !is GotoStmt) {
                throw Exception("If end not found")
            }
            toDelete.add(it)
            it.target
        }

        val trueBranch = trueBranchWithGoto.dropLast(1)

        val falseBranch = body.units.dropWhile { it !== jump.target }.takeWhile { it !== ifEnd }

        return trueBranch.isNotEmpty() || falseBranch.isNotEmpty()
    }
}
