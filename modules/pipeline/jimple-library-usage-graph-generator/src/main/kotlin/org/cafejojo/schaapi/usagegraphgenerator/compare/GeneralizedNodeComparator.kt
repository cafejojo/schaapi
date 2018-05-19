package org.cafejojo.schaapi.usagegraphgenerator.compare

import org.cafejojo.schaapi.common.Node

/**
 * Comparator of [Node]s by structure and generalized values.
 *
 * Given a set of [Node]s that are expected to be equal to each other (called the "instances"), the "template" is the
 * instance such that the structure and generalized values of the other instances should match. The [satisfies] method
 * returns true if a given instance indeed matches the given template.
 *
 * While the template may be chosen arbitrarily from a set of instances, the selection should not change between
 * comparisons of instances.
 */
interface GeneralizedNodeComparator {
    /**
     * Returns true iff [instance] satisfies the structure and generalized values of [template].
     *
     * @param template the template [Node]
     * @param instance the instance [Node]
     * @return true iff [instance] satisfies the structure and generalized values of [template]
     */
    fun satisfies(template: Node, instance: Node): Boolean

    /**
     * Returns true iff [template] and [instance] have the same structure.
     *
     * @param template the template [Node]
     * @param instance the instance [Node]
     * @return true iff [template] and [instance] have the same structure
     */
    fun structuresAreEqual(template: Node, instance: Node): Boolean

    /**
     * Returns true iff [template] and [instance] have the same generalized values.
     *
     * @param template the template [Node]
     * @param instance the instance [Node]
     * @return true iff [template] and [instance] have the same generalized values
     */
    fun generalizedValuesAreEqual(template: Node, instance: Node): Boolean
}
