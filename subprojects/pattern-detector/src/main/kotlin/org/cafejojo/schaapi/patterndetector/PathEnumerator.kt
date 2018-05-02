package org.cafejojo.schaapi.patterndetector

import org.cafejojo.schaapi.usagegraphgenerator.BranchNode
import org.cafejojo.schaapi.usagegraphgenerator.EntryNode
import org.cafejojo.schaapi.usagegraphgenerator.ExitNode
import org.cafejojo.schaapi.usagegraphgenerator.Node
import java.util.Stack

/**
 * Creates a copy of this [BranchNode] with one branch omitted.
 *
 * The branch of the node labeled [condition] is preserved, while the other branch is replaced with a reference to the
 * given [exitNode].
 *
 * @param condition the label of the branch that should be preserved.
 * @param exitNode the [ExitNode] of the method control flow graph.
 * @return a [BranchNode] with the described properties.
 */
internal fun BranchNode.getSingleSuccessorCopy(condition: Boolean, exitNode: ExitNode): BranchNode {
    val branchNode = BranchNode(id = id)

    branchNode.successors.add(if (condition) exitNode else successors[0])
    branchNode.successors.add(if (condition) successors[1] else exitNode)

    return branchNode
}

/**
 * Enumerates all paths in a control flow graph.
 *
 * Enumeration is done through a depth-first search of the directed graph. All simple paths from entry to exit are
 * identified in a recursive manner. Additionally, all paths that have at most two occurrences of each node are found
 * (such paths are not necessarily simple paths), to execute loop bodies at once and twice.
 *
 * @property entryNode the entry node of the method graph.
 * @property exitNode the exit node of the method graph.
 */
class PathEnumerator(
    private val entryNode: EntryNode,
    private val exitNode: ExitNode
) {
    private val allPaths = mutableListOf<List<Node>>()
    private val visited = Stack<Node>()

    init {
        visited.push(entryNode)
    }

    /**
     * Enumerates all paths of the control flow graph.
     */
    fun enumerate(): List<List<Node>> {
        recursivelyEnumerate(entryNode)
        return allPaths.toList()
    }

    private fun recursivelyEnumerate(node: Node) {
        checkIfExitNodeIsReached(node)
        visitSuccessors(node)
    }

    private fun checkIfExitNodeIsReached(node: Node) {
        val unvisitedSuccessors = node.successors.filter { hasBeenVisitedAtMostOnce(it) }

        for (successor in unvisitedSuccessors) {
            if (successor == exitNode) {
                visited.push(successor)
                allPaths.add(pruneBranchNodes(visited.toMutableList()))
                visited.pop()
                break
            }
        }
    }

    private fun visitSuccessors(node: Node) {
        node.successors
            .filter { hasBeenVisitedAtMostOnce(it) && it != exitNode }
            .forEach {
                visited.push(it)
                recursivelyEnumerate(visited.peek())
                visited.pop()
            }
    }

    private fun hasBeenVisitedAtMostOnce(node: Node) = visited.count { it == node } <= 1

    /**
     * Replaces all branch paths on the given [path] with single-branch alternatives.
     *
     * @param path the path to be processed.
     * @return the pruned list of nodes.
     */
    private fun pruneBranchNodes(path: MutableList<Node>): List<Node> {
        path.forEachIndexed { index: Int, node: Node ->
            if (node is BranchNode) {
                val chosenSuccessorIsTrue = node.trueSuccessor() === path[index + 1]
                path[index] = node.getSingleSuccessorCopy(chosenSuccessorIsTrue, exitNode)
            }
        }
        return path.toList()
    }
}
