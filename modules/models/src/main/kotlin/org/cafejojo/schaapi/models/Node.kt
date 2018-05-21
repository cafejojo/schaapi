package org.cafejojo.schaapi.models

/**
 * A node.
 *
 * @property successors the successor [Node]s
 */
interface Node : Iterable<Node> {
    val successors: MutableList<Node>

    override fun iterator() = DfsIterator(this)
}
