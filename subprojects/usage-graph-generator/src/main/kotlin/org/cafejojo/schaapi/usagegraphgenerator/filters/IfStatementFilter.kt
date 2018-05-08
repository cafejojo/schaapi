package org.cafejojo.schaapi.usagegraphgenerator.filters

import soot.Body
import soot.Unit
import soot.jimple.GotoStmt
import soot.jimple.IfStmt

/**
 * Performs filtering of library-using if statements.
 */
object IfStatementFilter {
    private val toDelete = HashSet<Unit>()

    /**
     * Removes if statements if branches do not contain library usages.
     *
     * @param body method body
     */
    fun apply(body: Body) {
        var changed = false

        body.units.snapshotIterator().forEach {
            if (it is IfStmt && !retain(body, it)) {
                changed = true
                body.units.remove(it)
                toDelete.forEach { body.units.remove(it) }
                toDelete.clear()
            }
        }

        if (changed) apply(body)
    }

    private fun retain(body: Body, jump: IfStmt) = hasNonEmptyBranches(body, jump) || ValueFilter.retain(jump.condition)

    private fun hasNonEmptyBranches(body: Body, jump: IfStmt): Boolean {
        val trueBranchWithGoto = body.units.dropWhile { it !== jump }.drop(1).takeWhile { it !== jump.target }

        val trueBranchGoto: GotoStmt = trueBranchWithGoto.findLast { it is GotoStmt }.let {
            if (it !is GotoStmt) throw TrueBranchOfIfHasNoGotoException()
            toDelete.add(it)
            it
        }

        val ifEnd: Unit = trueBranchGoto.target

        val trueBranch = trueBranchWithGoto.takeWhile { it !== trueBranchGoto }

        val falseBranch = body.units.dropWhile { it !== jump.target }.takeWhile { it !== ifEnd }

        return trueBranch.isNotEmpty() || falseBranch.isNotEmpty()
    }
}

/**
 * Exception for ifs without goto in the true branch.
 */
class TrueBranchOfIfHasNoGotoException :
    Exception("The true branch of the if statement has no goto statement, so the end of the if cannot be determined.")
