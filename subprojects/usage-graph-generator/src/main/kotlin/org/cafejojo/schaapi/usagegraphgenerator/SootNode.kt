package org.cafejojo.schaapi.usagegraphgenerator

import org.cafejojo.schaapi.common.Node
import org.cafejojo.schaapi.usagegraphgenerator.compare.isSubclassOf
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
class SootNode(val unit: Unit, override val successors: MutableList<Node> = arrayListOf()) : Node {
    override fun toString() = unit.toString()

    override fun equals(other: Any?): Boolean {
        if (other !is SootNode || this.unit::class != other.unit::class) return false

        val thisTypes = this.getValues().map { it.type }
        val otherTypes = other.getValues().map { it.type }

        otherTypes.forEachIndexed { index, templateType ->
            val instanceType = thisTypes[index]
            if (!templateType.isSubclassOf(instanceType) && !instanceType.isSubclassOf(templateType)) return false
        }

        return true
    }

    override fun hashCode(): Int = getValues().sumBy { it.hashCode() }

    private fun getValues() =
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
}
