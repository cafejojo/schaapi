package org.cafejojo.schaapi.common

/**
 * Represents a node.
 *
 * Contains references to the successor nodes.
 */
interface Node : Iterable<Node> {
    val successors: MutableList<Node>

    override fun iterator() = DFSIterator(this)
}
