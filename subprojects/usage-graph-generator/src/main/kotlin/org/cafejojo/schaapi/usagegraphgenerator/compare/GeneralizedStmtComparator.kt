package org.cafejojo.schaapi.usagegraphgenerator.compare

import org.cafejojo.schaapi.common.Node
import org.cafejojo.schaapi.usagegraphgenerator.SootNode
import soot.Scene
import soot.Type
import soot.Unit
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
 * Comparator of [Stmt]s by structure and generalized values.
 *
 * This comparator is stateful and is sensitive to the order in which methods are called. Refer to the documentation of
 * [satisfies].
 */
class GeneralizedStmtComparator : GeneralizedNodeComparator {
    /**
     * Maps [Value]s to tags.
     */
    private val valueTags = HashMap<Value, Int>()
    /**
     * Denotes the [Stmt] to which a tag was first assigned.
     */
    private val tagOrigins = HashMap<Int, Unit>()

    override fun satisfies(template: Node, instance: Node) =
        structuresAreEqual(template, instance) && generalizedValuesAreEqual(template, instance)

    /**
     * Returns true iff [template] and [instance] have the same structure.
     *
     * The structures are said to be the same if both statements are of the same class and the [soot.Type]s of their
     * fields are the same.
     *
     * Unlike with [satisfies], this method has no side effects and does not use state. Furthermore, this method is
     * commutative: [template] and [instance] can be switched around without changing the outcome.
     *
     * @param template the template [Node]
     * @param instance the instance [Node]
     * @return true iff [template] and [instance] have the same structure
     */
    override fun structuresAreEqual(template: Node, instance: Node): Boolean {
        if (template !is SootNode || instance !is SootNode) {
            return false
        }
        if (template.unit::class != instance.unit::class) {
            return false
        }

        val templateTypes = getValues(template.unit).map { it.type }
        val instanceTypes = getValues(instance.unit).map { it.type }

        templateTypes.forEachIndexed { index, templateType ->
            val instanceType = instanceTypes[index]

            if (!templateType.isSubclassOf(instanceType) && !instanceType.isSubclassOf(templateType)) {
                return false
            }
        }

        return true
    }

    /**
     * Returns true iff [template] and [instance] have the same generalized values.
     *
     * An instance is said to satisfy the generalized values of the template if both [template] and [instance]
     * use [Value]s that either this comparator has not seen before or has seen before in two [Stmt]s such that those
     * were equal according to this method. If the [Stmt]s to be compared have more than one [Value], the above
     * procedure is applied to the respective [Value]s.
     *
     * @param template the template [Node]
     * @param instance the instance [Node]
     * @return true iff [template] and [instance] have the same generalized values
     */
    @SuppressWarnings("UnsafeCallOnNullableType") // The !! is implicitly avoided by checking `templateHasTag`
    override fun generalizedValuesAreEqual(template: Node, instance: Node): Boolean {
        if (template !is SootNode || instance !is SootNode) {
            return false
        }

        val templateUnit = template.unit
        val instanceUnit = instance.unit

        val templateValues = getValues(templateUnit)
        val instanceValues = getValues(instanceUnit)

        templateValues.forEachIndexed { index, templateValue ->
            val instanceValue = instanceValues[index]

            val templateHasTag = hasTag(templateValue)
            val instanceHasTag = hasTag(instanceValue)
            val templateIsFinalized = isDefinedIn(templateValue, templateUnit)

            val templateTag = valueTags[templateValue]
            val instanceTag = valueTags[instanceValue]

            when {
                !templateHasTag && !instanceHasTag -> {
                    val newTag = createNewTag()
                    valueTags[templateValue] = newTag
                    valueTags[instanceValue] = newTag
                    tagOrigins[newTag] = templateUnit
                }
                !templateHasTag && instanceHasTag -> return false

                templateHasTag && !instanceHasTag && templateIsFinalized -> valueTags[instanceValue] = templateTag!!
                templateHasTag && !instanceHasTag && !templateIsFinalized -> return false

                templateHasTag && instanceHasTag && templateTag != instanceTag -> return false
            }
        }

        return true
    }

    /**
     * Returns a list of the [Value]s contained in [stmt] as fields.
     *
     * @param stmt a [Stmt]
     * @return a list of the [Value]s contained in [stmt] as fields
     */
    private fun getValues(stmt: Unit) =
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

    private fun createNewTag() = tagOrigins.size

    private fun hasTag(value: Value) = valueTags.contains(value)

    private fun isDefinedIn(value: Value, stmt: Unit) = tagOrigins[valueTags[value]] === stmt
}

/**
 * Returns true iff [this] is a subclass of [that].
 *
 * @param that a [Type]
 * @return true iff [this] is a subclass of [that]
 */
@SuppressWarnings("TooGenericExceptionCaught") // Part of signature of Soot's [merge] method
fun Type.isSubclassOf(that: Type) =
    try {
        this.merge(that, Scene.v()) == that
    } catch (e: RuntimeException) {
        false
    }
