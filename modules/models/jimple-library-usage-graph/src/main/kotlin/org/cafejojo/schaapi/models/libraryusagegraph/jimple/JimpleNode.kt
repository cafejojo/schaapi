package org.cafejojo.schaapi.models.libraryusagegraph.jimple

import org.cafejojo.schaapi.common.Node
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.compare.isSubclassOf
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
     * Extract the values from the contained [Stmt] based on its type.
     *
     * @return all values of the contained [Stmt] based on the type of the [Stmt]
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
     * A [JimpleNode] equals another [JimpleNode] if the [statement] is of the same type, they have the same amount of
     * values, and each value at their respective positions has the same type.
     *
     * @return true iff the [statement] is of same type and the values are in the same order and of the same type
     */
    override fun equals(other: Any?): Boolean {
        if (other !is JimpleNode || this.statement::class != other.statement::class) return false

        val thisTypes = this.getTopLevelValues().map { it.type }
        val otherTypes = other.getTopLevelValues().map { it.type }

        thisTypes.forEachIndexed { index, thisType ->
            val otherType = otherTypes[index]
            if (thisType != otherType &&
                !thisType.isSubclassOf(otherType) &&
                !otherType.isSubclassOf(thisType)
            ) {
                return false
            }
        }

        return true
    }

    /**
     * Generates a hashcode based on the values of the contained [Stmt], and their order.
     *
     * @return hashcode based on the hashcodes of the values contained in the contained [Stmt]
     */
    override fun hashCode(): Int {
        var hash = 0
        getTopLevelValues().forEachIndexed { index, value -> hash += (index + 1) * value.type.hashCode() }
        return hash
    }
}