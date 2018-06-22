package org.cafejojo.schaapi.models.libraryusagegraph.jimple

import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.PathEnumerator
import soot.jimple.EqExpr
import soot.jimple.GeExpr
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
            .also { it.forEach(this::processPath) }

    private fun processPath(path: MutableList<JimpleNode>) {
        path.toList()
            .filter { it.statement.branches() }
            .forEach { node ->
                val statement = node.statement
                when (statement) {
                    is IfStmt -> handleIf(path, node, statement)
                    is SwitchStmt -> handleSwitch(path, node)
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
            .singleEquiv { it.statement === statementCopy.defaultTarget }

        val currentBranchNode = node.successors.singleEquiv { it in path }
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
     * Returns the first common node in the (recursive) successors of [base] and [target], or the last element of [path]
     * if there is no such element.
     *
     * @param base // TODO
     * @param target // TODO
     * @param path // TODO
     * @return the first common node in the (recursive) successors of [base] and [target], or the last element of [path]
     * if there is no such element
     */
    private fun findCoercionTargetFor(base: Node, target: Node, path: List<Node>) =
        ((base.findNextCommonNodeWithOrNull(target) ?: path.last()) as? JimpleNode
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

/**
 * Returns the first element for which [predicate] is true iff all elements for which [predicate] is true are
 * equivalent; otherwise an [IllegalArgumentException] is thrown.
 *
 * @param N the type of [Node] contained in this [Iterable]
 * @param predicate the predicate to check for
 * @return the first element for which [predicate] is true iff all elements for which [predicate] is true are
 * equivalent
 */
private fun <N : Node> Iterable<N>.singleEquiv(predicate: (N) -> Boolean): N {
    val candidates = this.toList().filter(predicate)
    require(candidates.isNotEmpty()) { "No elements match the predicate." }
    require(candidates.all { it.equivTo(candidates.first()) }) {
        "Not all elements matching the predicate are equivalent."
    }
    return candidates.first()
}
