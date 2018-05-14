package org.cafejojo.schaapi.usagegraphgenerator.compare

import soot.Scene
import soot.Type
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
class GeneralizedStmtComparator {
    /**
     * Maps [Value]s to tags.
     */
    private val valueTags = HashMap<Value, Int>()
    /**
     * Denotes the [Stmt] to which a tag was first assigned.
     */
    private val tagOrigins = HashMap<Int, Stmt>()

    /**
     * Returns true iff [instance] satisfies the structure and generalized values of [template].
     *
     * The [template] is special in that all [Stmt]s that are suspected to be equal to each other (called "instances")
     * must be compared to the template. This method will fail if arbitrary instances are compared to each other. The
     * selection of the template before any comparison occurs can be arbitrary, however.
     *
     * An instance is said to satisfy the <i>structures</i> of the template of [structuresAreEqual] returns true.
     *
     * An instance is said to satisfy the <i>generalized values</i> of the template if both [template] and [instance]
     * use [Value]s that either this comparator has not seen before or has seen before in two [Stmt]s such that those
     * were equal according to this method. If the [Stmt]s to be compared have more than one [Value], the above
     * procedure is applied to the respective [Value]s.
     *
     * While the order in which instances are checked against the template is not important, care should be taken that
     * sequential [Stmt]s should be processed in the order they appear in the original code. Not doing so may result in
     * false positives.
     *
     * @param template the template [Stmt]
     * @param instance the instance [Stmt]
     * @return true iff [instance] satisfies the structure and generalized values of [template]
     */
    fun satisfies(template: Stmt, instance: Stmt) =
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
     * @param template the template [Stmt]
     * @param instance the instance [Stmt]
     * @return true iff [template] and [instance] have the same structure
     */
    fun structuresAreEqual(template: Stmt, instance: Stmt): Boolean {
        if (template::class != instance::class) {
            return false
        }

        val templateTypes = getValues(template).map { it.type }
        val instanceTypes = getValues(instance).map { it.type }

        templateTypes.forEachIndexed { index, templateType ->
            val instanceType = instanceTypes[index]

            if (!templateType.isSubclassOf(instanceType) && !instanceType.isSubclassOf(templateType)) {
                return false
            }
        }

        return true
    }

    @SuppressWarnings("UnsafeCallOnNullableType") // The !! is implicitly avoided by checking `templateHasTag`
    private fun generalizedValuesAreEqual(templateStmt: Stmt, instanceStmt: Stmt): Boolean {
        val templateValues = getValues(templateStmt)
        val instanceValues = getValues(instanceStmt)

        templateValues.forEachIndexed { index, templateValue ->
            val instanceValue = instanceValues[index]

            val templateHasTag = hasTag(templateValue)
            val instanceHasTag = hasTag(instanceValue)
            val templateIsFinalized = isDefinedIn(templateValue, templateStmt)

            val templateTag = valueTags[templateValue]
            val instanceTag = valueTags[instanceValue]

            when {
                !templateHasTag && !instanceHasTag -> {
                    val newTag = createNewTag()
                    valueTags[templateValue] = newTag
                    valueTags[instanceValue] = newTag
                    tagOrigins[newTag] = templateStmt
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

    private fun createNewTag() = tagOrigins.size

    private fun hasTag(value: Value) = valueTags.contains(value)

    private fun isDefinedIn(value: Value, stmt: Stmt) = tagOrigins[valueTags[value]] === stmt
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
