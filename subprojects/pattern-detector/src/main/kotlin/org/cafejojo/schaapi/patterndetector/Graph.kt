package org.cafejojo.schaapi.patterndetector

data class Node(val id: Int, val outgoingEdges: Set<Edge>)

data class Edge(val from: Node, val to: Node, val label: Boolean?)

interface PathItem

data class NodeContent(val id: Int) : PathItem

data class EdgeLabel(val label: Boolean?) : PathItem

fun nodeToPathItem(node: Node): NodeContent {
    return NodeContent(node.id)
}
