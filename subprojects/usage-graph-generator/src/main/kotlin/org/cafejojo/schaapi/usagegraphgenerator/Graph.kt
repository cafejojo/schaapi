package org.cafejojo.schaapi.usagegraphgenerator

import org.objectweb.asm.tree.AbstractInsnNode
import java.util.UUID

abstract class Node {
    var successors: List<Node> = emptyList()
    var id = UUID.randomUUID()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Node

        if (successors != other.successors) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
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
}
