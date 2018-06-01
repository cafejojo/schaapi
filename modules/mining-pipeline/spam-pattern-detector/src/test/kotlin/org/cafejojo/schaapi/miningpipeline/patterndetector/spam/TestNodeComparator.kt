package org.cafejojo.schaapi.miningpipeline.patterndetector.spam

import org.cafejojo.schaapi.models.GeneralizedNodeComparator
import org.cafejojo.schaapi.models.Node

/**
 * A subclass of [GeneralizedNodeComparator] for testing purposes.
 *
 * Two [Node]s are considered equal if they have the same reference.
 */
internal class TestNodeComparator<N : Node> : GeneralizedNodeComparator<N> {
    override fun structuresAreEqual(template: N, instance: N) = template === instance
    override fun generalizedValuesAreEqual(template: N, instance: N) = template === instance
    override fun satisfies(template: N, instance: N) = template === instance
}
