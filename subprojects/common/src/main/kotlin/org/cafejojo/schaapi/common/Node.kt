package org.cafejojo.schaapi.common

/**
 * Represents a node.
 *
 * Contains references to the successor nodes.
 */
interface Node {
    val successors: MutableList<Node>
}
