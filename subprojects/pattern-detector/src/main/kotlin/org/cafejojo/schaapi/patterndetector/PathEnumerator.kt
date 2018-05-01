package org.cafejojo.schaapi.patterndetector

import org.cafejojo.schaapi.usagegraphgenerator.BranchNode
import org.cafejojo.schaapi.usagegraphgenerator.EntryNode
import org.cafejojo.schaapi.usagegraphgenerator.ExitNode
import org.cafejojo.schaapi.usagegraphgenerator.Node
import java.util.Stack

internal fun BranchNode.getSingleSuccessorCopy(condition: Boolean, exitNode: ExitNode): BranchNode {
    val branchNode = BranchNode()

    branchNode.successors = when (condition) {
        true -> listOf(successors[0], exitNode)
        false -> listOf(exitNode, successors[1])
    }

    return branchNode
}

class PathEnumerator(
    entryNode: EntryNode,
    private val exitNode: ExitNode
) {
    private val paths = mutableListOf<List<Node>>()
    private val visited = Stack<Node>()

    init {
        visited.push(entryNode)
    }

    fun enumerate(): List<List<Node>> {
        recursivelyEnumerate()
        return paths.toList()
    }

    private fun recursivelyEnumerate() {
        checkIfExitNodeIsReached()
        visitSuccessors()
    }

    private fun checkIfExitNodeIsReached() {
        val unvisitedSuccessors = visited.peek().successors.filter { it !in visited }

        for (successor in unvisitedSuccessors) {
            if (successor == exitNode) {
                visited.push(successor)
                paths.add(pruneBranchNodes(visited.toMutableList()))
                visited.pop()
                break
            }
        }
    }

    private fun visitSuccessors() {
        val successors = visited.peek().successors

        successors
            .filter { it !in visited && it != exitNode }
            .forEach {
                visited.push(it)
                recursivelyEnumerate()
                visited.pop()
            }
    }

    private fun pruneBranchNodes(nodes: MutableList<Node>): List<Node> {
        nodes.forEachIndexed { index: Int, node: Node ->
            if (node is BranchNode) {
                val chosenSuccessorIsTrue = node.trueSuccessor() === nodes[index + 1]
                nodes[index] = node.getSingleSuccessorCopy(chosenSuccessorIsTrue, exitNode)
            }
        }
        return nodes.toList()
    }
}
