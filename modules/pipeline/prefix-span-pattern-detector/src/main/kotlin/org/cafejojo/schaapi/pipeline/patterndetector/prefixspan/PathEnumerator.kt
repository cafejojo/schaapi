package org.cafejojo.schaapi.pipeline.patterndetector.prefixspan

import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.SimpleNode
import java.util.Stack

/**
 * Enumerates all paths in a control flow graph.
 *
 * Enumeration is done through a depth-first search of the directed graph. All simple paths from entry to exit are
 * identified in a recursive manner. Additionally, all paths that have at most two occurrences of each node are found
 * (such paths are not necessarily simple paths), to execute loop bodies once and twice.
 *
 * @property entryNode the entry node of the method graph.
 */
class PathEnumerator(private val entryNode: Node) {
    private val allPaths = mutableListOf<List<Node>>()
    private val visited = Stack<Node>()
    private val exitNode = entryNode.connectLeavesToExitNode()

    init {
        visited.push(entryNode)
    }

    /**
     * Enumerates all paths of the control flow graph.
     */
    fun enumerate(): List<List<Node>> {
        recursivelyEnumerate(entryNode)
        entryNode.removeExitNodes()
        return allPaths.toList()
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
                visited.push(it)
                recursivelyEnumerate(visited.peek())
                visited.pop()
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
internal fun Node.connectLeavesToExitNode() = ExitNode().also { exitNode ->
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
