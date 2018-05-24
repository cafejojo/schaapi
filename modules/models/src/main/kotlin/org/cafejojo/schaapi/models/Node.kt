package org.cafejojo.schaapi.models

/**
 * A node.
 *
 * @property successors the successor [Node]s
 */
interface Node : Iterable<Node> {
    val successors: MutableList<Node>

    override fun iterator() = DfsIterator(this)

    /**
     * Returns true iff this node is equivalent to [other].
     *
     * @param other a [Node]
     * @return true iff this node is equivalent to [other]
     */
    fun equivTo(other: Node?): Boolean

    /**
     * Behaves to [equivTo] like [hashCode] behaves to [equals].
     *
     * @return the equivalency hash code
     */
    fun equivHashCode(): Int
}

/**
 * A simple implementation of [Node].
 */
open class SimpleNode(override val successors: MutableList<Node> = mutableListOf()) : Node {
    override fun equivTo(other: Node?) = this === other

    override fun equivHashCode() = super.hashCode()
}
