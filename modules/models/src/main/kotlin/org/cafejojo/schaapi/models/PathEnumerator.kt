package org.cafejojo.schaapi.models

import java.util.Stack

/**
 * Enumerates all paths in a control flow graph.
 *
 * Enumeration is done through a depth-first search of the directed graph. All simple paths from entry to exit are
 * identified in a recursive manner. Additionally, all paths that have at most two occurrences of each node are found
 * (such paths are not necessarily simple paths), to execute loop bodies once and twice.
 *
 * @property entryNode the entry node of the method graph
 * @property maximumPathLength the maximum length a path output by this enumerator should have
 */
class PathEnumerator<N : Node>(
    private val entryNode: N,
    private val maximumPathLength: Int
) {
    private val allPaths = mutableListOf<List<Node>>()
    private val currentPath = Stack<Node>()
    private val exitNode = ExitNode()

    init {
        currentPath.push(entryNode)
        entryNode.connectLeavesTo(exitNode)
    }

    /**
     * Enumerates all paths of the control flow graph.
     *
     * @return list of found paths, with all [Node]s guaranteed to be of type [N]
     */
    @Synchronized
    fun enumerate(): List<List<N>> {
        recursivelyEnumerate(entryNode)
        cleanUp()

        return allPaths.filterIsInstance<List<N>>()
    }

    private fun recursivelyEnumerate(node: Node) {
        if (exitNode in node.successors) allPaths.add(currentPath.toList())
        if (currentPath.size >= maximumPathLength) return

        node.successors
            .filter { it !in currentPath && it != exitNode }
            .forEach {
                currentPath.push(it)
                recursivelyEnumerate(it)
                currentPath.pop()
            }
    }

    private fun cleanUp() = entryNode.removeExitNodes()
}

/**
 * Connects all nodes that are connected to this node and have no successors to [node]. If there is no such node, the
 * last node connected to this node is connected to [node] instead.
 *
 * @param node the [Node] to connect leaves to
 */
internal fun Node.connectLeavesTo(node: Node) {
    val allNodes = iterator().asSequence().toList()
    val leaves = allNodes
        .filter { it.successors.isEmpty() }
        .let { if (it.isEmpty()) allNodes.takeLast(1) else it }

    leaves.forEach { it.successors.add(node) }
}

/**
 * Removes all [ExitNode]s from the graph of nodes connected to this node.
 */
internal fun Node.removeExitNodes() =
    iterator().asSequence().toList().forEach { it.successors.removeIf { it is ExitNode } }

/**
 * The sink of a control flow graph.
 */
class ExitNode(successors: MutableList<Node> = mutableListOf()) : SimpleNode(successors)
