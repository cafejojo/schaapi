package org.cafejojo.schaapi.models

import java.util.NoSuchElementException
import java.util.Stack

/**
 * Depth-First Search iterator for directed [Node] graphs.
 */
class DfsIterator(entryNode: Node) : Iterator<Node> {
    private val visited = mutableSetOf<Node>()
    private val successorStack = Stack<Iterator<Node>>()
    private var nextNode: Node? = entryNode

    init {
        successorStack.push(entryNode.successors.iterator())
    }

    override fun hasNext() = nextNode != null

    override fun next(): Node {
        val node = nextNode ?: throw NoSuchElementException()
        visited.add(node)
        advance()
        return node
    }

    private fun advance() {
        var neighbors = successorStack.peek()
        do {
            while (!neighbors.hasNext()) {
                successorStack.pop()
                if (successorStack.isEmpty()) {
                    nextNode = null
                    return
                }
                neighbors = successorStack.peek()
            }

            nextNode = neighbors?.next()
        } while (visited.any { it === nextNode })

        successorStack.push(nextNode?.successors?.iterator())
    }
}
