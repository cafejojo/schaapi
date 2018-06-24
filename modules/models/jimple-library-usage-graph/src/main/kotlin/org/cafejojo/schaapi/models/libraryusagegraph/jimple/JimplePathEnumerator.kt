package org.cafejojo.schaapi.models.libraryusagegraph.jimple

import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.PathEnumerator
import soot.jimple.EqExpr
import soot.jimple.GeExpr
import soot.jimple.GotoStmt
import soot.jimple.GtExpr
import soot.jimple.IfStmt
import soot.jimple.Jimple
import soot.jimple.LeExpr
import soot.jimple.LtExpr
import soot.jimple.NeExpr
import soot.jimple.SwitchStmt

/**
 * A [PathEnumerator] that performs some post-processing to ensure that branches are correct.
 *
 * Given the following Jimple pseudo-code:
 * ```
 * a()
 * IF condition GOTO c()
 * b()
 * GOTO d()
 * c()
 * d()
 * ```
 *
 * the default behaviour of the path enumerator will result in the following two paths:
 * ```
 * a()
 * IF condition GOTO c()
 * b()
 * GOTO d()
 * d()
 * ```
 * ```
 * a()
 * IF condition GOTO c()
 * c()
 * d()
 * ```
 * The first path contains a GOTO to a statement that is not in the path, which is not valid. The solution is to make
 * the IF jump to `d()` instead.
 * The second path contains a GOTO to the next statement, which means that the next statement will always be `c()`
 * regardless of the condition. The solution is to make the IF jump to `d()` instead. Additionally, the condition should
 * be flipped because it would execute what was the else branch in the original code.
 *
 * Similarly, the targets of a switch statement should be redirected to point to after the branch.
 */
class JimplePathEnumerator(entryNode: JimpleNode, maximumPathLength: Int) :
    PathEnumerator<JimpleNode>(entryNode, maximumPathLength) {
    override fun postProcess(paths: List<List<JimpleNode>>) =
        paths.map { it.toMutableList() }
            .also { it.forEach(::processPath) }

    private fun processPath(path: MutableList<JimpleNode>) {
        path.toList()
            .filter { it.statement.branches() }
            .forEach { node ->
                val statement = node.statement
                when (statement) {
                    is IfStmt -> handleIf(path, node, statement)
                    is SwitchStmt -> handleSwitch(path, node)
                    is GotoStmt -> {
                        // Do nothing
                    }
                    else -> throw IllegalArgumentException("Unrecognized branching statement.")
                }
            }
    }

    private fun handleIf(path: MutableList<JimpleNode>, node: JimpleNode, statement: IfStmt) {
        val pathStatements = path.map { it.statement }

        val nodeCopy = node.copy()
        path.replaceAndUpdateTargets(node, nodeCopy)

        val statementCopy = nodeCopy.statement as? IfStmt
            ?: throw IllegalStateException("IfStmt changed class after copy.")
        statementCopy.setTarget(findCoercionTargetFor(node.successors[0], node.successors[1], path))

        if (statement.target in pathStatements) statementCopy.flipCondition()
    }

    private fun handleSwitch(path: MutableList<JimpleNode>, node: JimpleNode) {
        val nodeCopy = node.copy()
        path.replaceAndUpdateTargets(node, nodeCopy)

        val statementCopy = nodeCopy.statement as? SwitchStmt
            ?: throw IllegalStateException("SwitchStmt changed class after copy.")
        val defaultTargetNode = node.successors
            .filterIsInstance<JimpleNode>()
            .first { it.statement === statementCopy.defaultTarget }

        val currentBranchNode = node.successors.first { it in path }
        (0 until node.successors.size)
            .filterNot { node.successors[it] === currentBranchNode }
            .filterNot { node.successors[it] === defaultTargetNode }
            .forEach {
                statementCopy.setTarget(it,
                    findCoercionTargetFor(currentBranchNode, node.successors[it], path))
            }

        statementCopy.defaultTarget = findCoercionTargetFor(currentBranchNode, defaultTargetNode, path)
    }

    /**
     * Returns the first common node in the (recursive) successors of [nodeA] and [nodeB], or the last element of [path]
     * if there is no such element.
     *
     * @param nodeA a node
     * @param nodeB a node
     * @param path the path from which to return the last node if there is no common node
     * @return the first common node in the (recursive) successors of [nodeA] and [nodeB], or the last element of [path]
     * if there is no such element
     */
    private fun findCoercionTargetFor(nodeA: Node, nodeB: Node, path: List<Node>) =
        ((nodeA.findNextCommonNodeWithOrNull(nodeB) ?: path.last()) as? JimpleNode
            ?: throw IllegalStateException("JimpleNode should not have non-Jimple successor during path enumeration."))
            .statement
}

/**
 * Flips the statement's condition.
 */
private fun IfStmt.flipCondition() =
    this.condition.let { condition ->
        this.condition = when (condition) {
            is EqExpr -> Jimple.v().newNeExpr(condition.op1, condition.op2)
            is NeExpr -> Jimple.v().newEqExpr(condition.op1, condition.op2)
            is GeExpr -> Jimple.v().newLtExpr(condition.op1, condition.op2)
            is LtExpr -> Jimple.v().newGeExpr(condition.op1, condition.op2)
            is GtExpr -> Jimple.v().newLeExpr(condition.op1, condition.op2)
            is LeExpr -> Jimple.v().newGtExpr(condition.op1, condition.op2)
            else -> throw IllegalStateException("Cannot invert a condition of an unknown type.")
        }
    }

/**
 * Replaces [old] with [new] and replaces all jumps to [old] in this path with jumps to [new].
 *
 * @param old the node to be replaced by [new]
 * @param new the node to replace [old] with
 */
private fun MutableList<JimpleNode>.replaceAndUpdateTargets(old: JimpleNode, new: JimpleNode) {
    set(indexOf(old), new)
    this.map { it.statement.unitBoxes }
        .mapNotNull { it.firstOrNull { it.unit == old.statement } }
        .forEach { it.unit = new.statement }
}

private fun Node.findNextCommonNodeWithOrNull(node: Node) = this.toList().intersect(node.toList()).firstOrNull()
