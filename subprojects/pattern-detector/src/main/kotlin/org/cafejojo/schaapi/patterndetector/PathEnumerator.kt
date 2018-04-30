package org.cafejojo.schaapi.patterndetector

import org.cafejojo.schaapi.usagegraphgenerator.BranchNode
import org.cafejojo.schaapi.usagegraphgenerator.EntryNode
import org.cafejojo.schaapi.usagegraphgenerator.ExitNode
import org.cafejojo.schaapi.usagegraphgenerator.Node
import java.util.Stack

internal data class SearchState(val node: Node, val nextNeighbourIndex: Int)

internal fun Node.toSingleSuccessor(successorIndex: Int, exitNode: ExitNode): Node = when (this) {
    is BranchNode -> this.getSingleSuccessorCopy(successorIndex, exitNode)
    else -> this
}

fun enumeratePaths(entryNode: EntryNode, exitNode: ExitNode): Set<List<Node>> {
    val paths = mutableSetOf<List<Node>>()
    val searchStack = Stack<SearchState>()
    val visited = mutableSetOf<Node>()
    val currentPath = mutableListOf<Node>()

    searchStack.push(SearchState(entryNode, 0))
    visited.add(entryNode)
    currentPath.add(entryNode.toSingleSuccessor(0, exitNode))

    while (!searchStack.empty()) {
        val state = searchStack.peek()

        if (state.node == exitNode || state.nextNeighbourIndex == state.node.successors.size) {
            if (state.node == exitNode) {
                paths.add(currentPath.toList())
            }

            // Backtrack
            visited.remove(state.node)
            currentPath.removeAt(currentPath.size - 1)
            searchStack.pop()
        } else {
            val nextNode = state.node.successors[state.nextNeighbourIndex]

            // Move on to next neighbour
            searchStack.pop()
            searchStack.add(SearchState(state.node, state.nextNeighbourIndex + 1))

            if (!visited.contains(nextNode)) {
                searchStack.push(SearchState(nextNode, 0))
                visited.add(nextNode)
                currentPath.add(nextNode.toSingleSuccessor(0, exitNode))
            }
        }
    }

    return paths.toSet()
}
