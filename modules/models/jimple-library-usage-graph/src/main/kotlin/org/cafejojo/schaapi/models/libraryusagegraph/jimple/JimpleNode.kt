package org.cafejojo.schaapi.models.libraryusagegraph.jimple

import org.cafejojo.schaapi.common.Node
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.compare.isSubclassOf
import soot.Unit
import soot.jimple.DefinitionStmt
import soot.jimple.GotoStmt
import soot.jimple.IfStmt
import soot.jimple.InvokeStmt
import soot.jimple.ReturnStmt
import soot.jimple.ReturnVoidStmt
import soot.jimple.SwitchStmt
import soot.jimple.ThrowStmt

/**
 * Represents a statement node.
 *
 * Contains references to the successor nodes.
 */
class JimpleNode(val unit: Unit, override val successors: MutableList<Node> = arrayListOf()) : Node {
    override fun toString() = unit.toString()

    /**
     * Extract the values from the contained [Unit] based on its type.
     *
     * @return all values of the contained [Unit] based on the type of the [Unit]
     */
    fun getValues() =
        when (unit) {
            is ThrowStmt -> listOf(unit.op)
            is DefinitionStmt -> listOf(unit.leftOp, unit.rightOp)
            is IfStmt -> listOf(unit.condition)
            is SwitchStmt -> listOf(unit.key)
            is InvokeStmt -> listOf(unit.invokeExpr)
            is ReturnStmt -> listOf(unit.op)
            is GotoStmt -> emptyList()
            is ReturnVoidStmt -> emptyList()
            else -> emptyList()
        }

    /**
     * A [JimpleNode] equals another [JimpleNode] if the [unit] is of the same type, they have the same amount of
     * values, and each value at their respective positions has the same type.
     *
     * @return true iff the [unit] is of same type and the values are in the same order and of the same type
     */
    override fun equals(other: Any?): Boolean {
        if (other !is JimpleNode || this.unit::class != other.unit::class) return false

        val thisTypes = this.getValues().map { it.type }
        val otherTypes = other.getValues().map { it.type }

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
     * Generates a hashcode based on the values of the contained [Unit], and their order.
     *
     * @return hashcode based on the hashcodes of the values contained in the contained [Unit]
     */
    override fun hashCode(): Int {
        var hash = 0
        getValues().forEachIndexed { index, value -> hash += (index + 1) * value.type.hashCode() }
        return hash
    }
}