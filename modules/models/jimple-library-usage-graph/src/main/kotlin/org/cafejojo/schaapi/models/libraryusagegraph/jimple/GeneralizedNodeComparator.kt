package org.cafejojo.schaapi.models.libraryusagegraph.jimple

import org.cafejojo.schaapi.models.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.Node
import soot.Value
import soot.jimple.Stmt

/**
 * Comparator of [Stmt]s by structure and generalized values.
 *
 * This comparator is stateful and is sensitive to the order in which methods are called. Refer to the documentation of
 * [satisfies].
 */
class GeneralizedNodeComparator : GeneralizedNodeComparator<JimpleNode> {
    /**
     * Maps [Value]s to tags.
     */
    private val valueTags = HashMap<Value, Int>()
    /**
     * Denotes the [Stmt] to which a tag was first assigned.
     */
    private val tagOrigins = HashMap<Int, Stmt>()

    @Synchronized
    override fun satisfies(template: JimpleNode, instance: JimpleNode) =
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
    override fun structuresAreEqual(template: JimpleNode, instance: JimpleNode) = template.equivTo(instance)

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
    override fun generalizedValuesAreEqual(template: JimpleNode, instance: JimpleNode): Boolean {
        template.getValues().forEachIndexed { index, templateValue ->
            val instanceValue = instance.getValues()[index]

            val templateHasTag = hasTag(templateValue)
            val instanceHasTag = hasTag(instanceValue)

            if (templateHasTag) {
                if (!compareInstanceWithTemplate(template, templateValue, instanceValue)) return false
            } else if (!templateHasTag && instanceHasTag) {
                return false
            } else {
                createNewTag(template, templateValue, instanceValue)
            }
        }

        return true
    }

    @Suppress("UnsafeCallOnNullableType") // The !! is implicitly avoided by checking `hasTag`
    private fun compareInstanceWithTemplate(template: JimpleNode, templateValue: Value, instanceValue: Value): Boolean {
        val templateTag = valueTags[templateValue]
        val instanceTag = valueTags[instanceValue]

        if (hasTag(instanceValue)) {
            if (templateTag != instanceTag) return false
        } else {
            if (!isDefinedIn(templateValue, template.statement)) return false
            valueTags[instanceValue] = templateTag!!
        }

        return true
    }

    private fun generateNewTag() = tagOrigins.size

    private fun createNewTag(template: JimpleNode, templateValue: Value, instanceValue: Value) = generateNewTag().let {
        valueTags[templateValue] = it
        valueTags[instanceValue] = it
        tagOrigins[it] = template.statement
    }

    private fun hasTag(value: Value) = valueTags.contains(value)

    private fun isDefinedIn(value: Value, statement: Stmt) = tagOrigins[valueTags[value]] === statement
}
