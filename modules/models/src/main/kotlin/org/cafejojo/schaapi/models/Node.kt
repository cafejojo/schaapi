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

    /**
     * Constructs an identical copy of this [Node] such that it is [equivTo] this.
     *
     * @return an identical copy of this [Node] such that it is [equivTo] this
     */
    fun copy(): Node

    companion object {
        /**
         * Returns true iff [left] and [right] are [Node]s and [equivTo] returns true.
         *
         * @param left a [Node]
         * @param right a [Node]
         * @return true iff [left] and [right] are [Node]s and [equivTo] returns true
         */
        fun equiv(left: Any?, right: Any?) = left is Node && right is Node && left.equivTo(right)
    }
}

/**
 * A simple implementation of [Node].
 */
open class SimpleNode(override val successors: MutableList<Node> = mutableListOf()) : Node {
    override fun equivTo(other: Node?) = this === other

    override fun equivHashCode() = super.hashCode()

    override fun copy() = SimpleNode(successors.toMutableList())
}
