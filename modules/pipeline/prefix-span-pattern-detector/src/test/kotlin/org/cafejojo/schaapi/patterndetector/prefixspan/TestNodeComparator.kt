package org.cafejojo.schaapi.patterndetector.prefixspan

import org.cafejojo.schaapi.common.GeneralizedNodeComparator
import org.cafejojo.schaapi.common.Node

/**
 * A subclass of [GeneralizedNodeComparator] for testing purposes.
 *
 * Two [Node]s are considered equal if they have the same reference.
 */
class TestNodeComparator : GeneralizedNodeComparator {
    override fun structuresAreEqual(template: Node, instance: Node) = template === instance
    override fun generalizedValuesAreEqual(template: Node, instance: Node) = template === instance
    override fun satisfies(template: Node, instance: Node) = template === instance
}
