package org.cafejojo.schaapi.usagegraphgenerator

import org.cafejojo.schaapi.common.Node
import soot.Unit

/**
 * Represents a statement node.
 *
 * Contains references to the successor nodes.
 */
data class SootNode(val unit: Unit, override val successors: MutableList<Node> = arrayListOf()) : Node {
    override fun toString() = unit.toString()
}
