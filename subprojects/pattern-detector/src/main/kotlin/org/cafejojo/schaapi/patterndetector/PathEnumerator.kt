package org.cafejojo.schaapi.patterndetector

import java.util.Stack

internal data class SearchState(val node: Node, val nextNeighbourIndex: Int)

fun enumeratePaths(source: Node, sink: Node): Set<List<PathItem>> {
    val paths = mutableSetOf<List<PathItem>>()
    val searchStack = Stack<SearchState>()
    val visited = mutableSetOf<Node>()
    val currentPath = mutableListOf<PathItem>()

    searchStack.push(SearchState(source, 0))
    visited.add(source)
    currentPath.add(nodeToPathItem(source))

    while (!searchStack.empty()) {
        val state = searchStack.peek()

        if (state.node == sink || state.nextNeighbourIndex == state.node.outgoingEdges.size) {
            if (state.node == sink) {
                paths.add(currentPath)
            }

            // Backtrack
            visited.remove(state.node)
            currentPath.removeAt(currentPath.size - 1)
            searchStack.pop()
        } else {
            val nextNode = state.node.outgoingEdges.elementAt(state.nextNeighbourIndex).to

            // Move on to next neighbour
            searchStack.pop()
            searchStack.add(SearchState(state.node, state.nextNeighbourIndex + 1))

            if (!visited.contains(nextNode)) {
                searchStack.push(SearchState(nextNode, 0))
                visited.add(nextNode)
                currentPath.add(nodeToPathItem(nextNode))
            }
        }
    }

    return paths.toSet()
}
