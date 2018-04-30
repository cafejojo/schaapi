package org.cafejojo.schaapi.usagegraphgenerator

import org.objectweb.asm.tree.AbstractInsnNode

abstract class Node {
    var successors: List<Node> = emptyList()
}

abstract class InstructionsNode : Node() {
    val instructions: MutableList<AbstractInsnNode> = arrayListOf()
}

class EntryNode : Node()

class ExitNode : Node()

class StatementNode : InstructionsNode()

class BranchNode : InstructionsNode() {
    fun trueSuccessor() = successors[0]
    fun falseSuccessor() = successors[1]

    fun getSingleSuccessorCopy(successorIndex: Int, exitNode: ExitNode): BranchNode {
        val branchNode = BranchNode()

        if (successorIndex == 0) {
            branchNode.successors = listOf(successors[0], exitNode)
        } else {
            branchNode.successors = listOf(exitNode, successors[1])
        }

        return branchNode
    }
}
