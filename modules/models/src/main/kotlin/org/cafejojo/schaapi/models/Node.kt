package org.cafejojo.schaapi.models

/**
 * A node.
 *
 * @property successors the successor [Node]s
 */
interface Node : Iterable<Node> {
    val successors: MutableList<Node>

    override fun iterator() = DfsIterator(this)

    // TODO add documentation here
    fun equivTo(other: Node?): Boolean

    fun equivHashCode(): Int
}

/**
 * A simple implementation of [Node].
 */
open class SimpleNode(override val successors: MutableList<Node> = mutableListOf()) : Node {
    override fun equivTo(other: Node?) = this === other

    override fun equivHashCode() = super.hashCode()
}
