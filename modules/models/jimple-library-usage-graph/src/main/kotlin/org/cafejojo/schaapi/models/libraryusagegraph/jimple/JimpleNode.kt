package org.cafejojo.schaapi.models.libraryusagegraph.jimple

import org.cafejojo.schaapi.models.Node
import soot.jimple.DefinitionStmt
import soot.jimple.GotoStmt
import soot.jimple.IfStmt
import soot.jimple.InvokeStmt
import soot.jimple.ReturnStmt
import soot.jimple.ReturnVoidStmt
import soot.jimple.Stmt
import soot.jimple.SwitchStmt
import soot.jimple.ThrowStmt

/**
 * Represents a statement node.
 *
 * Contains references to the successor nodes.
 */
class JimpleNode(val statement: Stmt, override val successors: MutableList<Node> = arrayListOf()) : Node {
    override fun toString() = statement.toString()

    /**
     * Returns all [soot.Value]s used as fields in [statement].
     *
     * @return all [soot.Value]s used as fields in [statement]
     */
    fun getTopLevelValues() =
        when (statement) {
            is ThrowStmt -> listOf(statement.op)
            is DefinitionStmt -> listOf(statement.leftOp, statement.rightOp)
            is IfStmt -> listOf(statement.condition)
            is SwitchStmt -> listOf(statement.key)
            is InvokeStmt -> listOf(statement.invokeExpr)
            is ReturnStmt -> listOf(statement.op)
            is GotoStmt -> emptyList()
            is ReturnVoidStmt -> emptyList()
            else -> emptyList()
        }

    /**
     * Returns all [soot.Value]s recursively contained in [statement].
     *
     * @return all [soot.Value]s recursively contained in [statement]
     */
    fun getValues() = getTopLevelValues().union(getTopLevelValues().flatMap { it.useBoxes.map { it.value } }).toList()

    /**
     * A [JimpleNode] equals another [JimpleNode] if the [statement] is of the same type, they have the same amount of
     * values, and each value at their respective positions has the same type.
     *
     * @return true iff the [statement] is of same type and the values are in the same order and of the same type
     */
    override fun equivTo(other: Node?) =
        if (other !is JimpleNode || this.statement::class != other.statement::class) false
        else this.getTopLevelValues().zip(other.getTopLevelValues()).all { it.first.equivTo(it.second) }

    /**
     * Generates a hashcode based on the values of the contained [Stmt] and their order.
     *
     * @return a hashcode based on the hashcodes of the values contained in the contained [Stmt]
     */
    override fun equivHashCode(): Int {
        var hash = 0
        getTopLevelValues().forEachIndexed { index, value -> hash += (index + 1) * value.equivHashCode() }
        return hash
    }

    override fun copy() = JimpleNode(
        statement.clone() as? Stmt ?: throw IllegalStateException("Stmt::clone did not return a Stmt."),
        successors.toMutableList()
    )
}
