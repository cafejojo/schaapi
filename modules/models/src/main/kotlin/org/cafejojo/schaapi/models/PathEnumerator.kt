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
    private val visited = Stack<Node>()
    private val exitNode = entryNode.connectLeavesToExitNode()

    init {
        visited.push(entryNode)
    }

    /**
     * Enumerates all paths of the control flow graph.
     *
     * @return list of found paths, with all [Node]s guaranteed to be of type [N]
     */
    @Suppress("UnsafeCast") // We assume that all artificial Nodes are removed
    fun enumerate(): List<List<N>> {
        recursivelyEnumerate(entryNode)
        entryNode.removeExitNodes()

        return allPaths as List<List<N>>
    }

    private fun recursivelyEnumerate(node: Node) {
        checkIfExitNodeIsReached(node)
        visitSuccessors(node)
    }

    private fun checkIfExitNodeIsReached(node: Node) =
        node.successors.filter { hasBeenVisitedAtMostOnce(it) }
            .find { it == exitNode }
            ?.let { allPaths.add(visited.toMutableList()) }

    private fun visitSuccessors(node: Node) =
        node.successors
            .filter { hasBeenVisitedAtMostOnce(it) && it != exitNode }
            .forEach {
                if (visited.size < maximumPathLength) {
                    visited.push(it)
                    recursivelyEnumerate(visited.peek())
                    visited.pop()
                }
            }

    private fun hasBeenVisitedAtMostOnce(node: Node) = visited.count { it == node } <= 1
}

/**
 * The sink of a control flow graph.
 */
class ExitNode(successors: MutableList<Node> = mutableListOf()) : SimpleNode(successors)

/**
 * Connects all nodes with no successors (connected to this node) with a single [ExitNode].
 */
internal fun Node.connectLeavesToExitNode() =
    ExitNode().also { exitNode ->
        iterator().asSequence().toList().let { allNodes ->
            allNodes.filter { it.successors.isEmpty() }
                .let { if (it.isEmpty()) allNodes.takeLast(1) else it }
                .forEach { it.successors.add(exitNode) }
        }
    }

/**
 * Removes all [ExitNode]s from the graph of nodes connected to this node.
 */
internal fun Node.removeExitNodes() =
    iterator().asSequence().toList().forEach { it.successors.removeIf { it is ExitNode } }
