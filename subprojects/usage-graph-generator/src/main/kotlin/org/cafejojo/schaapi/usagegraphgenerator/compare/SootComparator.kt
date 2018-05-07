package org.cafejojo.schaapi.usagegraphgenerator.compare

import soot.Value
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
 * Comparator of [Stmt]s by structure.
 */
class StmtComparator {
    /**
     * Returns true iff [left] and [right] have the same structure.
     *
     * @param left a [Stmt]
     * @param right a [Stmt]
     */
    fun areEqual(left: Stmt, right: Stmt) =
        when (left) {
            is ThrowStmt -> right is ThrowStmt && left.op.type == right.op.type
            is DefinitionStmt -> right is DefinitionStmt && left.leftOp.type == right.leftOp.type
                && left.rightOp.type == right.rightOp.type
            is IfStmt -> right is IfStmt && left.condition.type == right.condition.type
            is SwitchStmt -> right is SwitchStmt && left.key.type == right.key.type
            is InvokeStmt -> right is InvokeStmt && left.invokeExpr.method == right.invokeExpr.method
            is ReturnStmt -> right is ReturnStmt && left.op.type == right.op.type
            is GotoStmt -> true
            is ReturnVoidStmt -> true
            else -> false // todo
        }
}

/**
 * Comparator of [Stmt]s by abstractified [Value]s.
 */
class StmtTagginator {
    private val tagOrigins = HashMap<Tag, Stmt>()
    private val tags = HashMap<Value, Tag>()

    /**
     * Returns true iff [leftStmt] and [rightStmt] use the same abstractified [Value]s.
     * @param leftStmt a [Stmt]
     * @param rightStmt a [Stmt]
     */
    fun compare(leftStmt: Stmt, rightStmt: Stmt): Boolean {
        if (leftStmt != rightStmt) {
            return false
        }

        val leftValues = getValues(leftStmt)
        val rightValues = getValues(rightStmt)

        if (leftValues.size != rightValues.size) {
            return false
        }

        leftValues.forEachIndexed { index, value ->
            val leftTag = tags[value]
            val rightTag = tags[rightValues[index]]

            if (leftTag == null) {
                if (rightTag == null) {
                    // Good
                    val newTag = Tag()
                    tags[value] = newTag
                    tags[rightValues[index]] = newTag
                    tagOrigins[newTag] = leftStmt
                } else {
                    // Bad
                    return false
                }
            } else if (tagOrigins[leftTag] !== leftStmt) {
                // Left has a tag from a previous run
                if (rightTag == null) {
                    // Bad
                    return false
                } else {
                    // Maybe bad
                    if (leftTag !== rightTag) {
                        return false
                    }
                }
            } else {
                // Left has a tag from this run
                if (rightTag == null) {
                    // Good
                    tags[rightValues[index]] = leftTag
                } else {
                    // Maybe bad
                    if (leftTag !== rightTag) {
                        return false
                    }
                }
            }
        }

        return true
    }

    private fun getValues(stmt: Stmt) =
        when (stmt) {
            is ThrowStmt -> listOf(stmt.op)
            is DefinitionStmt -> listOf(stmt.leftOp, stmt.rightOp)
            is IfStmt -> listOf(stmt.condition)
            is SwitchStmt -> listOf(stmt.key)
            is InvokeStmt -> listOf(stmt.invokeExpr)
            is ReturnStmt -> listOf(stmt.op)
            is GotoStmt -> emptyList()
            is ReturnVoidStmt -> emptyList()
            else -> emptyList()
        }
}

/**
 * A tag.
 */
class Tag
