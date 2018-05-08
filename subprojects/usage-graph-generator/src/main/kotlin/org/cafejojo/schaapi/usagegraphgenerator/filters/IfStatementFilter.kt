package org.cafejojo.schaapi.usagegraphgenerator.filters

import org.cafejojo.schaapi.common.JavaProject
import soot.Body
import soot.Unit
import soot.jimple.GotoStmt
import soot.jimple.IfStmt

/**
 * Performs filtering of library-using if statements.
 *
 * @param project library project
 * @property body method body
 */
class IfStatementFilter(project: JavaProject, private val body: Body) : Filter {
    private val valueFilter = ValueFilter(project)

    /**
     * Removes if statements if branches do not contain library usages.
     */
    override fun apply() {
        var changed = true

        while (changed) {
            changed = false

            body.units.snapshotIterator().asSequence()
                .filterIsInstance(IfStmt::class.java)
                .map { IfStatement(body, it) }
                .filter { !retain(it) }
                .forEach {
                    changed = true
                    body.units.remove(it.statement)
                    body.units.remove(it.trueBranchGoto)
                }
        }
    }

    private fun retain(ifStatement: IfStatement) =
        ifStatement.hasNonEmptyBranches() || valueFilter.retain(ifStatement.statement.condition)
}

private class IfStatement(body: Body, val statement: IfStmt) {
    val trueBranchWithGoto = body.units.dropWhile { it !== statement }.drop(1).takeWhile { it !== statement.target }

    val trueBranchGoto: GotoStmt = trueBranchWithGoto.findLast { it is GotoStmt }.let {
        if (it !is GotoStmt) throw TrueBranchOfIfHasNoGotoException()
        it
    }

    val ifEnd: Unit = trueBranchGoto.target

    val trueBranch = trueBranchWithGoto.takeWhile { it !== trueBranchGoto }

    val falseBranch = body.units.dropWhile { it !== statement.target }.takeWhile { it !== ifEnd }

    fun hasNonEmptyBranches() = trueBranch.isNotEmpty() || falseBranch.isNotEmpty()
}

/**
 * Exception for ifs without goto in the true branch.
 */
class TrueBranchOfIfHasNoGotoException :
    Exception("The true branch of the if statement has no goto statement, so the end of the if cannot be determined.")
