package org.cafejojo.schaapi.patterndetector

import org.cafejojo.schaapi.common.Node
import org.cafejojo.schaapi.usagegraphgenerator.compare.GeneralizedNodeComparator

class TestNodeComparator : GeneralizedNodeComparator {
    override fun structuresAreEqual(template: Node, instance: Node) = template === instance
    override fun generalizedValuesAreEqual(template: Node, instance: Node) = template === instance
    override fun satisfies(template: Node, instance: Node) = template === instance
}
